package com.example.alirinmobile.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.authStore by preferencesDataStore("alirin_auth")

// DataStore ini hanya memegang flag lokal (anonymous flow warga & onboarding).
// Sesi Supabase (token, refresh, user) di-manage penuh oleh supabase-kt SDK
// via internal SessionManager — tidak duplikasi di sini.
class AuthDataStore(private val appContext: Context) {
    private object K {
        val anonChosen = intPreferencesKey("anon_chosen")
        val onboardingDone = intPreferencesKey("onboarding_done")
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

    suspend fun setAnonymous() {
        appContext.authStore.edit { prefs -> prefs[K.anonChosen] = 1 }
    }

    suspend fun clearAnonymous() {
        appContext.authStore.edit { prefs -> prefs.remove(K.anonChosen) }
    }
}
