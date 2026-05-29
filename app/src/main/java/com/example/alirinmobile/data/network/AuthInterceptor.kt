package com.example.alirinmobile.data.network

import com.example.alirinmobile.data.local.AuthDataStore
import com.example.alirinmobile.data.network.dto.RefreshRequest
import com.example.alirinmobile.data.network.service.AuthService
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

private fun Request.isAuthRequest(): Boolean {
    val path = url.encodedPath
    return path.endsWith("/auth/login") || path.endsWith("/auth/refresh")
}

/** Adds Bearer header to every authed request that doesn't already have one. */
internal class AuthInterceptor(private val store: AuthDataStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        if (original.isAuthRequest() || original.header("Authorization") != null) {
            return chain.proceed(original)
        }
        val token = store.snapshotBlocking()?.token ?: return chain.proceed(original)
        return chain.proceed(
            original.newBuilder().header("Authorization", "Bearer $token").build()
        )
    }
}

/**
 * On 401: try /auth/refresh with the stored refreshToken. On success persist new tokens
 * and retry; on failure clear the session so the app drops back to LoginScreen.
 */
internal class AuthAuthenticator(
    private val store: AuthDataStore,
    private val refreshApi: () -> AuthService,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.isAuthRequest()) return null
        if (response.priorResponse != null) return null   // already tried once

        val session = store.snapshotBlocking() ?: return null
        val refreshToken = session.refreshToken ?: run { store.clearBlocking(); return null }

        val refreshed = runCatching {
            runBlocking { refreshApi().refresh(RefreshRequest(refreshToken)) }
        }.getOrElse { store.clearBlocking(); return null }

        store.updateTokensBlocking(refreshed.accessToken, refreshed.refreshToken)
        return response.request.newBuilder()
            .header("Authorization", "Bearer ${refreshed.accessToken}")
            .build()
    }
}
