package com.example.alirinmobile.data.network

import com.example.alirinmobile.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.okhttp.OkHttp

object AlirinSupabase {
    val client: SupabaseClient by lazy {
        require(BuildConfig.SUPABASE_URL.isNotBlank()) {
            "SUPABASE_URL kosong. Tambahkan ke local.properties dan rebuild."
        }
        require(BuildConfig.SUPABASE_PUBLISHABLE_KEY.isNotBlank()) {
            "SUPABASE_PUBLISHABLE_KEY kosong. Tambahkan ke local.properties dan rebuild."
        }
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY,
        ) {
            install(Postgrest)
            install(Storage)
            install(Realtime)
            install(Auth) {
                alwaysAutoRefresh = true
                autoLoadFromStorage = true
            }
            httpEngine = OkHttp.create()
        }
    }
}
