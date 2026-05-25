package com.example.alirinmobile.data

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

/**
 * Loads the bundled curated Bandar Lampung kelurahan list once and exposes it.
 * No network call; the file lives in assets/.
 */
class KelurahanRepository(private val appContext: Context) {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    val list: List<Kelurahan> by lazy {
        val raw = appContext.assets.open("bandar_lampung_kelurahan.json").bufferedReader().use { it.readText() }
        json.decodeFromString<KelurahanFile>(raw).items
    }

    /** Default fallback if nothing selected yet. */
    val default: Kelurahan get() = list.firstOrNull { it.adm4 == "18.71.13.1007" }
        ?: list.first()
}
