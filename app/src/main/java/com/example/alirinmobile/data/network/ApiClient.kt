package com.example.alirinmobile.data.network

import com.example.alirinmobile.BuildConfig
import com.example.alirinmobile.data.network.service.BmkgService
import com.example.alirinmobile.data.network.service.AlirinFunctionsService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

// Retrofit client untuk BMKG dan untuk Edge Function ALIRIN.
// Auth Supabase & CRUD reports pindah ke io.github.jan.supabase.SupabaseClient
// (lihat AlirinSupabase di file lain).
//
// Panggilan Groq TIDAK lagi ada di sini: kuncinya pindah ke Edge Function
// supaya tidak ikut tertanam di APK (temuan D-4 laporan audit).
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

    // Kunci yang dikirim adalah publishable key Supabase, bukan kunci Groq.
    // Kunci ini memang dirancang untuk ada di sisi klien.
    private val functionsClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val req = chain.request().newBuilder()
                .header("Authorization", "Bearer ${BuildConfig.SUPABASE_PUBLISHABLE_KEY}")
                .header("apikey", BuildConfig.SUPABASE_PUBLISHABLE_KEY)
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
    val functionsService: AlirinFunctionsService by lazy {
        retrofit(BuildConfig.SUPABASE_URL.trimEnd('/') + "/", functionsClient)
            .create(AlirinFunctionsService::class.java)
    }

    val functionsConfigured: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_PUBLISHABLE_KEY.isNotBlank()
}
