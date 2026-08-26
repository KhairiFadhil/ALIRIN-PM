package com.example.alirinmobile.data.repository

import com.example.alirinmobile.data.auth.AuthSession
import com.example.alirinmobile.data.auth.Role
import com.example.alirinmobile.data.auth.supabaseRoleFromString
import com.example.alirinmobile.data.local.AuthDataStore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.math.absoluteValue

private const val ROLE_REQUIRED_MESSAGE =
    "Akun belum memiliki role admin/petugas yang valid. Hubungi admin sistem."
private const val INVALID_CREDENTIALS_MESSAGE =
    "Email/password tidak valid atau akun belum dibuat di Supabase Auth."
private const val NETWORK_ERROR_MESSAGE =
    "Server autentikasi tidak dapat dihubungi. Periksa koneksi internet dan pastikan project Supabase masih aktif."

// Baca role dari JWT metadata; mirror C:\ALIRIN\app\src\services\authService.js:42-52
// Fallback ke app_metadata/user_metadata "role" atau "app_role".
private fun pickString(app: JsonObject?, user: JsonObject?, key: String): String? {
    val v = app?.get(key) ?: user?.get(key)
    return (v as? JsonPrimitive)?.content
}

private fun roleFrom(user: UserInfo): Role? {
    // HANYA app_metadata. user_metadata bisa ditulis pengguna sendiri lewat
    // updateUser, jadi tidak boleh menentukan peran -- sama seperti
    // alirin_user_role() di basis data. Menaruh peran dari user_metadata hanya
    // akan menampilkan layar staf ke orang yang setiap query-nya toh ditolak
    // RLS; lebih jujur tidak menampilkannya sama sekali.
    val app = user.appMetadata
    val raw = listOf("role", "app_role")
        .firstNotNullOfOrNull { pickString(app, null, it) }
    val role = supabaseRoleFromString(raw)
    return if (role == Role.Citizen) null else role
}

private fun UserInfo.toSession(role: Role): AuthSession {
    val emailValue = email.orEmpty()
    val meta = userMetadata
    val fullName = pickString(null, meta, "full_name")
    val name = pickString(null, meta, "name")
    val displayName = fullName ?: name ?: emailValue.substringBefore("@").ifBlank { "Staff" }
    return AuthSession(
        userId = id.hashCode().absoluteValue,
        supabaseUserId = id,
        username = emailValue,
        displayName = displayName,
        role = role,
        avatarUrl = pickString(null, meta, "avatar_url"),
        officerId = pickString(null, meta, "officerId") ?: pickString(null, meta, "officer_id"),
    )
}

class AuthRepository(
    private val supabase: SupabaseClient,
    private val store: AuthDataStore,
) {

    val session: Flow<AuthSession?> = supabase.auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> status.session.user?.let { user ->
                roleFrom(user)?.let { user.toSession(it) }
            }
            else -> null
        }
    }

    val anonChosen: Flow<Boolean> = store.anonChosen
    val onboardingDone: Flow<Boolean> = store.onboardingDone

    suspend fun markOnboardingDone() = store.markOnboardingDone()

    suspend fun chooseAnonymous() {
        // Sesi anonim nyata, bukan sekadar penanda. Dari sinilah reporter_id
        // laporan berasal (P-8). Bila gagal -- mis. anonymous sign-in belum
        // diaktifkan di project -- warga tetap bisa memakai aplikasi tanpa
        // sesi, hanya kehilangan "Laporan saya" dan rate limit per perangkat.
        ensureCitizenSession()
        store.setAnonymous()
    }

    // Memastikan ada sesi sebelum warga mengirim laporan. Tidak menyentuh sesi
    // staf yang sudah ada. Mengembalikan true bila pada akhirnya ada sesi.
    suspend fun ensureCitizenSession(): Boolean {
        if (supabase.auth.currentUserOrNull() != null) return true
        return runCatching {
            supabase.auth.signInAnonymously()
            supabase.auth.currentUserOrNull() != null
        }.getOrDefault(false)
    }

    fun currentUserId(): String? = supabase.auth.currentUserOrNull()?.id

    suspend fun logout() {
        runCatching { supabase.auth.signOut() }
        store.clearAnonymous()
    }

    suspend fun login(email: String, password: String): AuthSession {
        try {
            supabase.auth.signInWith(Email) {
                this.email = email.trim()
                this.password = password
            }
        } catch (t: Throwable) {
            throw AuthException(mapLoginError(t))
        }
        val user = supabase.auth.currentUserOrNull()
            ?: throw AuthException(INVALID_CREDENTIALS_MESSAGE)
        val role = roleFrom(user) ?: run {
            runCatching { supabase.auth.signOut() }
            throw AuthException(ROLE_REQUIRED_MESSAGE)
        }
        store.clearAnonymous()
        return user.toSession(role)
    }

    suspend fun isStaff(): Boolean {
        val user = supabase.auth.currentUserOrNull() ?: return false
        return roleFrom(user) != null
    }

    class AuthException(message: String) : RuntimeException(message)

    private fun mapLoginError(t: Throwable): String {
        val msg = (t.message ?: "").lowercase()
        return when {
            "invalid login credentials" in msg -> INVALID_CREDENTIALS_MESSAGE
            "failed to fetch" in msg || "network" in msg || "unreachable" in msg ||
                "unresolved" in msg || "timeout" in msg -> NETWORK_ERROR_MESSAGE
            else -> t.message?.takeIf { it.isNotBlank() } ?: "Login gagal. Coba lagi."
        }
    }
}
