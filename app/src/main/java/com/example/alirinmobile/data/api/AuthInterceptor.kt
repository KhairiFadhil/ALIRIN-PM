package com.example.alirinmobile.data.api

import com.example.alirinmobile.data.local.AuthDataStore
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import kotlinx.coroutines.runBlocking

private fun Request.isAuthRequest(): Boolean {
    val path = url.encodedPath
    return path.endsWith("/auth/login") || path.endsWith("/auth/refresh")
}

/** Attaches Bearer header (if a session exists) to every request except auth endpoints. */
class AuthInterceptor(private val store: AuthDataStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        if (original.isAuthRequest() || original.header("Authorization") != null) {
            return chain.proceed(original)
        }
        val token = store.snapshotBlocking()?.token ?: return chain.proceed(original)
        val authed = original.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(authed)
    }
}

/**
 * On 401: try POST /auth/refresh with the stored refreshToken. On success, persist new
 * tokens and retry the original request with the new Bearer. On failure, clear the
 * session so the app drops back to LoginScreen on next observation.
 */
class AuthAuthenticator(
    private val store: AuthDataStore,
    private val refreshApi: () -> AuthApi,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.isAuthRequest()) return null
        // Prevent infinite loop: only attempt one refresh per failed request chain.
        if (response.priorResponse != null) return null

        val session = store.snapshotBlocking() ?: return null
        val refreshToken = session.refreshToken ?: run {
            store.clearBlocking()
            return null
        }
        val refreshed = runCatching {
            runBlocking { refreshApi().refresh(RefreshRequest(refreshToken)) }
        }.getOrElse {
            store.clearBlocking()
            return null
        }

        store.updateTokensBlocking(refreshed.accessToken, refreshed.refreshToken)
        return response.request.newBuilder()
            .header("Authorization", "Bearer ${refreshed.accessToken}")
            .build()
    }
}
