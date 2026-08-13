package com.example.alirinmobile.data.repository

import android.content.Context
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Kelurahan(
    val kecamatan: String,
    val kelurahan: String,
    val adm4: String,
)

@Serializable
private data class KelurahanFile(
    @SerialName("_note") val note: String? = null,
    val items: List<Kelurahan> = emptyList(),
)

class KelurahanRepository(private val appContext: Context) {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    val list: List<Kelurahan> by lazy {
        val raw = appContext.assets.open("bandar_lampung_kelurahan.json")
            .bufferedReader().use { it.readText() }
        json.decodeFromString<KelurahanFile>(raw).items
    }

    val default: Kelurahan
        get() = list.firstOrNull { it.adm4 == "18.71.13.1007" } ?: list.first()

    // Validasi pasangan kecamatan+kelurahan mirror C:\ALIRIN\app\src\services\reportsStore.js:134
    // ("Wilayah kecamatan dan kelurahan belum valid.") supaya nilai ngawur seperti
    // ALR-2026-9804 (kecamatan="C") tidak pernah sampai ke Supabase.
    fun isValidArea(kelurahan: String, kecamatan: String): Boolean {
        val kel = kelurahan.trim()
        val kec = kecamatan.trim()
        if (kel.isBlank() || kec.isBlank()) return false
        return list.any { it.kelurahan.equals(kel, ignoreCase = true) && it.kecamatan.equals(kec, ignoreCase = true) }
    }
}
