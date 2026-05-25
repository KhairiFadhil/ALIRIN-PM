package com.example.alirinmobile.data.auth

import com.example.alirinmobile.data.api.LoginRequest
import com.example.alirinmobile.data.api.NetworkModule
import com.example.alirinmobile.data.local.AuthDataStore
import kotlinx.coroutines.flow.Flow

class AuthRepository(private val authStore: AuthDataStore) {

    val session: Flow<AuthSession?> = authStore.session
    val anonChosen: Flow<Boolean> = authStore.anonChosen
    val onboardingDone: Flow<Boolean> = authStore.onboardingDone

    suspend fun markOnboardingDone() = authStore.markOnboardingDone()

    /**
     * Login against dummyjson.com. Returns the resulting AuthSession on success;
     * throws on any HTTP / network error so the ViewModel can surface a message.
     */
    suspend fun login(username: String, password: String): AuthSession {
        val response = NetworkModule.authApi.login(LoginRequest(username, password))
        val role = mapDummyUsernameToRole(username)
        val displayName = listOf(response.firstName, response.lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { response.username }
        val session = AuthSession(
            userId = response.id,
            username = response.username,
            displayName = displayName,
            role = role,
            token = response.accessToken,
            refreshToken = response.refreshToken.takeIf { it.isNotBlank() },
            avatarUrl = response.image.takeIf { it.isNotBlank() },
        )
        authStore.save(session)
        return session
    }

    suspend fun chooseAnonymous() = authStore.chooseAnonymous()
    suspend fun logout() = authStore.clear()
}
