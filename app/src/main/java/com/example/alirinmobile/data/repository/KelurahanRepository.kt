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
    // Master lengkap kecamatan -> daftar kelurahan, untuk validasi wilayah.
    val areas: Map<String, List<String>> = emptyMap(),
    // Hanya kelurahan yang kode adm4 BMKG-nya terverifikasi, untuk cuaca.
    val items: List<Kelurahan> = emptyList(),
)

class KelurahanRepository(private val appContext: Context) {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    private val file: KelurahanFile by lazy {
        val raw = appContext.assets.open("bandar_lampung_kelurahan.json")
            .bufferedReader().use { it.readText() }
        json.decodeFromString<KelurahanFile>(raw)
    }

    val list: List<Kelurahan> get() = file.items

    val areas: Map<String, List<String>> get() = file.areas

    val default: Kelurahan
        get() = list.firstOrNull { it.adm4 == "18.71.13.1007" } ?: list.first()

    // Kelurahan persis -> kelurahan lain di kecamatan yang sama -> acuan kota.
    // Baru 20 dari 122 kelurahan punya kode adm4 terverifikasi. Hujan 3 jam BMKG
    // bersifat regional sehingga pencadangan tingkat kecamatan tetap bermakna,
    // dan yang dihindari adalah mengarang kode wilayah.
    // Kembar dengan resolveAdm4 di C:\ALIRIN\app\src\data\bandarLampungAreas.js
    fun resolveAdm4(kecamatan: String, kelurahan: String): String {
        val kec = kecamatan.trim()
        val kel = kelurahan.trim()
        list.firstOrNull { it.kecamatan.equals(kec, true) && it.kelurahan.equals(kel, true) }
            ?.let { return it.adm4 }
        list.firstOrNull { it.kecamatan.equals(kec, true) }?.let { return it.adm4 }
        return default.adm4
    }

    // Validasi pasangan kecamatan+kelurahan terhadap master lengkap 20 kecamatan
    // / 122 kelurahan, sama dengan isKnownArea di reportsStore.js. Sebelumnya
    // validasi memakai daftar adm4 yang hanya 20 kelurahan, sehingga warga di
    // 102 kelurahan lain tidak bisa mengirim laporan sama sekali.
    fun isValidArea(kelurahan: String, kecamatan: String): Boolean {
        val kel = kelurahan.trim()
        val kec = kecamatan.trim()
        if (kel.isBlank() || kec.isBlank()) return false
        val kelurahanList = areas.entries
            .firstOrNull { it.key.equals(kec, ignoreCase = true) }
            ?.value
            ?: return false
        return kelurahanList.any { it.equals(kel, ignoreCase = true) }
    }
}
