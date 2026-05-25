package com.example.alirinmobile.data.api

import com.example.alirinmobile.data.local.AuthDataStore
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Holds singletons for all Retrofit-backed APIs. Three base URLs:
 *  - dummyjson.com → AuthApi
 *  - api.bmkg.go.id → BmkgApi
 *  - (configurable) → PublicMapApi
 *
 * Call init(store) once from AlirinApplication so the Authenticator can read/refresh
 * tokens without a circular dependency on the AuthRepository.
 */
object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    private val converter = json.asConverterFactory("application/json".toMediaType())

    private var authStore: AuthDataStore? = null

    /** Bare AuthApi used by the Authenticator's refresh path — no Bearer header attached. */
    private val bareAuthApi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://dummyjson.com/")
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()
            )
            .addConverterFactory(converter)
            .build()
            .create(AuthApi::class.java)
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .apply {
                val store = authStore
                if (store != null) {
                    addInterceptor(AuthInterceptor(store))
                    authenticator(AuthAuthenticator(store) { bareAuthApi })
                }
            }
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    fun init(store: AuthDataStore) {
        authStore = store
    }

    private fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(converter)
        .build()

    val authApi: AuthApi by lazy {
        retrofit("https://dummyjson.com/").create(AuthApi::class.java)
    }

    val bmkgApi: BmkgApi by lazy {
        retrofit("https://api.bmkg.go.id/").create(BmkgApi::class.java)
    }

    /**
     * Default public-map URL is a placeholder. Replace with the real endpoint when the
     * server is live; until then, callers fall back to local SampleData.
     */
    val publicMapApi: PublicMapApi by lazy {
        retrofit("https://example.invalid/").create(PublicMapApi::class.java)
    }
}
