package com.example.alirinmobile.data.network

import com.example.alirinmobile.BuildConfig
import com.example.alirinmobile.data.network.service.BmkgService
import com.example.alirinmobile.data.network.service.GroqService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

// Retrofit client hanya untuk service pihak-ketiga (BMKG cuaca + Groq LLM).
// Auth Supabase & CRUD reports pindah ke io.github.jan.supabase.SupabaseClient
// (lihat AlirinSupabase di file lain).
class ApiClient {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }
    private val converter = json.asConverterFactory("application/json".toMediaType())

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val baseClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

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
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun retrofit(baseUrl: String, client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(converter)
        .build()

    val bmkgService: BmkgService by lazy {
        retrofit("https://api.bmkg.go.id/", baseClient).create(BmkgService::class.java)
    }
    val groqService: GroqService by lazy {
        retrofit("https://api.groq.com/", groqClient).create(GroqService::class.java)
    }

    val groqConfigured: Boolean get() = BuildConfig.GROQ_API_KEY.isNotBlank()
}
