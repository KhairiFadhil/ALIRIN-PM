package com.example.alirinmobile.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.alirinmobile.data.auth.AuthSession
import com.example.alirinmobile.data.auth.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.authStore by preferencesDataStore("alirin_auth")

class AuthDataStore(private val appContext: Context) {
    private object K {
        val userId       = intPreferencesKey("user_id")
        val username     = stringPreferencesKey("username")
        val displayName  = stringPreferencesKey("display_name")
        val role         = stringPreferencesKey("role")
        val token        = stringPreferencesKey("token")
        val refreshToken = stringPreferencesKey("refresh_token")
        val avatar       = stringPreferencesKey("avatar")

        val anonChosen   = intPreferencesKey("anon_chosen")

        val onboardingDone = intPreferencesKey("onboarding_done")
    }

    val session: Flow<AuthSession?> = appContext.authStore.data.map { prefs ->
        prefs.toSession()
    }

    val anonChosen: Flow<Boolean> = appContext.authStore.data.map { prefs ->
        (prefs[K.anonChosen] ?: 0) == 1
    }

    val onboardingDone: Flow<Boolean> = appContext.authStore.data.map { prefs ->
        (prefs[K.onboardingDone] ?: 0) == 1
    }

    suspend fun markOnboardingDone() {
        appContext.authStore.edit { prefs -> prefs[K.onboardingDone] = 1 }
    }

    suspend fun save(session: AuthSession) {
        appContext.authStore.edit { prefs ->
            prefs[K.userId] = session.userId
            prefs[K.username] = session.username
            prefs[K.displayName] = session.displayName
            prefs[K.role] = session.role.name
            prefs[K.token] = session.token
            session.refreshToken?.let { prefs[K.refreshToken] = it } ?: prefs.remove(K.refreshToken)
            session.avatarUrl?.let { prefs[K.avatar] = it } ?: prefs.remove(K.avatar)
            prefs.remove(K.anonChosen)
        }
    }

    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        appContext.authStore.edit { prefs ->
            prefs[K.token] = accessToken
            prefs[K.refreshToken] = refreshToken
        }
    }

    fun snapshotBlocking(): AuthSession? = runBlocking { session.first() }

    fun updateTokensBlocking(accessToken: String, refreshToken: String) =
        runBlocking { updateTokens(accessToken, refreshToken) }

    fun clearBlocking() = runBlocking { clear() }

    suspend fun clear() {
        appContext.authStore.edit { prefs ->
            prefs.remove(K.userId)
            prefs.remove(K.username)
            prefs.remove(K.displayName)
            prefs.remove(K.role)
            prefs.remove(K.token)
            prefs.remove(K.refreshToken)
            prefs.remove(K.avatar)
            prefs.remove(K.anonChosen)

        }
    }

    suspend fun chooseAnonymous() {
        appContext.authStore.edit { prefs ->
            prefs.clear()
            prefs[K.anonChosen] = 1
        }
    }

    private fun Preferences.toSession(): AuthSession? {
        val token = this[K.token] ?: return null
        val id = this[K.userId] ?: return null
        val username = this[K.username] ?: return null
        val displayName = this[K.displayName] ?: username
        val roleName = this[K.role] ?: Role.Citizen.name
        val role = runCatching { Role.valueOf(roleName) }.getOrDefault(Role.Citizen)
        return AuthSession(
            userId = id,
            username = username,
            displayName = displayName,
            role = role,
            token = token,
            refreshToken = this[K.refreshToken],
            avatarUrl = this[K.avatar],
        )
    }
}
