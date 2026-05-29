package com.example.alirinmobile.data.network

import com.example.alirinmobile.BuildConfig
import com.example.alirinmobile.data.local.AuthDataStore
import com.example.alirinmobile.data.network.service.AuthService
import com.example.alirinmobile.data.network.service.BmkgService
import com.example.alirinmobile.data.network.service.GroqService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Central HTTP client: shared OkHttp + JSON converter, plus per-API Retrofit instances.
 *
 * Each backend has its own base URL because they live on different hosts:
 *  - dummyjson.com → AuthService
 *  - api.bmkg.go.id → BmkgService
 *  - api.groq.com → GroqService
 *
 * Headers (Bearer token for dummyjson, Authorization for GROQ) are added per service so
 * unrelated calls (BMKG public) stay clean.
 */
class ApiClient(private val authStore: AuthDataStore) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }
    private val converter = json.asConverterFactory("application/json".toMediaType())

    // ── Shared OkHttp building blocks ─────────────────────────────
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    /** Base OkHttp for endpoints that don't need any auth. */
    private val baseClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /** Auth-aware client: injects dummyjson Bearer + handles 401 refresh. */
    private val authedClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(AuthInterceptor(authStore))
        .authenticator(AuthAuthenticator(authStore) { bareAuthService })
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /** Standalone client for GROQ — adds static Bearer from BuildConfig. */
    private val groqClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val req = chain.request().newBuilder()
                .header("Authorization", "Bearer ${BuildConfig.GROQ_API_KEY}")
                .header("Content-Type", "application/json")
                .build()
            chain.proceed(req)
        }
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)   // LLM responses can be slow
        .build()

    private fun retrofit(baseUrl: String, client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(converter)
        .build()

    // ── Service handles (lazy singletons) ─────────────────────────
    /** Bare AuthService (no Bearer) — used by the Authenticator's refresh call. */
    private val bareAuthService: AuthService by lazy {
        retrofit("https://dummyjson.com/", baseClient).create(AuthService::class.java)
    }

    val authService: AuthService by lazy {
        retrofit("https://dummyjson.com/", authedClient).create(AuthService::class.java)
    }
    val bmkgService: BmkgService by lazy {
        retrofit("https://api.bmkg.go.id/", baseClient).create(BmkgService::class.java)
    }
    val groqService: GroqService by lazy {
        retrofit("https://api.groq.com/", groqClient).create(GroqService::class.java)
    }

    /** True only when local.properties supplies a GROQ_API_KEY. */
    val groqConfigured: Boolean get() = BuildConfig.GROQ_API_KEY.isNotBlank()
}
