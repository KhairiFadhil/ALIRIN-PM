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
    // Kode adm4 BMKG untuk seluruh 126 kelurahan, untuk cuaca.
    val items: List<Kelurahan> = emptyList(),
)

class KelurahanRepository(private val appContext: Context) {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    // Acuan terakhir yang SELALU ada, walau aset gagal dibaca. Durian Payung,
    // Tanjung Karang Pusat -- pusat kota. Tanpa ini, list.first() pada aset
    // kosong melempar NoSuchElementException dan menjatuhkan seluruh alur lapor.
    private val hardcodedDefault = Kelurahan("Tanjung Karang Pusat", "Durian Payung", "18.71.06.1001")

    // Pemuatan aset dibungkus: berkas hilang atau rusak tidak boleh membuat
    // aplikasi crash saat pertama kali menyentuh data wilayah. Bila gagal,
    // master kosong -> validasi menolak semua dan pengguna diarahkan memilih,
    // bukan aplikasi tertutup.
    private val file: KelurahanFile by lazy {
        runCatching {
            val raw = appContext.assets.open("bandar_lampung_kelurahan.json")
                .bufferedReader().use { it.readText() }
            json.decodeFromString<KelurahanFile>(raw)
        }.getOrElse {
            android.util.Log.e("AlirinKelurahan", "gagal memuat master wilayah: ${it.message}")
            KelurahanFile()
        }
    }

    val list: List<Kelurahan> get() = file.items

    val areas: Map<String, List<String>> get() = file.areas

    val default: Kelurahan
        get() = list.firstOrNull { it.adm4 == "18.71.13.1007" }
            ?: list.firstOrNull()
            ?: hardcodedDefault

    // Kelurahan persis -> kelurahan lain di kecamatan yang sama -> acuan kota.
    // Seluruh 126 kelurahan kini punya kodenya sendiri, jadi jalur pencadangan
    // hanya terpakai bila nama wilayahnya tidak dikenali sama sekali.
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
    // / 126 kelurahan, sama dengan isKnownArea di reportsStore.js. Sebelumnya
    // validasi memakai daftar adm4 yang hanya 20 kelurahan, sehingga warga di
    // 106 kelurahan lain tidak bisa mengirim laporan sama sekali.
    fun isValidArea(kelurahan: String, kecamatan: String): Boolean =
        AreaMatcher.isValidArea(areas, kecamatan, kelurahan)

    val kecamatanList: List<String> get() = areas.keys.sorted()

    fun kelurahanOf(kecamatan: String): List<String> =
        areas.entries.firstOrNull { it.key.equals(kecamatan.trim(), ignoreCase = true) }?.value.orEmpty()

    fun matchArea(kecamatanRaw: String?, kelurahanRaw: String?): Pair<String, String>? =
        AreaMatcher.match(areas, kecamatanRaw, kelurahanRaw)
}
