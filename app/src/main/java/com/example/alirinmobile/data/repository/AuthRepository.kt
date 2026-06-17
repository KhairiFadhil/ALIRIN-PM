package com.example.alirinmobile.data.repository

import com.example.alirinmobile.data.auth.AuthSession
import com.example.alirinmobile.data.auth.mapDummyUsernameToRole
import com.example.alirinmobile.data.local.AuthDataStore
import com.example.alirinmobile.data.network.ApiClient
import com.example.alirinmobile.data.network.dto.LoginRequest
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val api: ApiClient,
    private val authStore: AuthDataStore,
) {
    val session: Flow<AuthSession?> = authStore.session
    val anonChosen: Flow<Boolean> = authStore.anonChosen
    val onboardingDone: Flow<Boolean> = authStore.onboardingDone

    suspend fun login(username: String, password: String): AuthSession {
        val res = api.authService.login(LoginRequest(username, password))
        val role = mapDummyUsernameToRole(username)
        val displayName = listOf(res.firstName, res.lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { res.username }
        val session = AuthSession(
            userId = res.id,
            username = res.username,
            displayName = displayName,
            role = role,
            token = res.accessToken,
            refreshToken = res.refreshToken.takeIf { it.isNotBlank() },
            avatarUrl = res.image.takeIf { it.isNotBlank() },
        )
        authStore.save(session)
        return session
    }

    suspend fun chooseAnonymous() = authStore.chooseAnonymous()
    suspend fun logout() = authStore.clear()
    suspend fun markOnboardingDone() = authStore.markOnboardingDone()
}
