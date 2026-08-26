package com.example.alirinmobile.data.repository

import android.util.Log
import com.example.alirinmobile.data.scoring.RiskEngine
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// P-3 - Konteks hulu-hilir.
//
// Proposal 1.4 mencatat warga Rajabasa mengenali hubungan banjir mereka dengan
// hujan deras di Kemiling, dan Kota Karang dengan kiriman dari Gunung Betung
// dan Batu Putu. Kelas ini yang menerjemahkan pengetahuan itu menjadi angka:
// sebelum laporan dikirim, prakiraan BMKG untuk kecamatan hulu ikut diambil dan
// disimpan, sehingga basis data punya bahan saat menilai risiko.
//
// Tanpa langkah ini tabel cuaca per kecamatan hanya terisi kebetulan, dan
// faktor hulu praktis tidak pernah menyala.

@Serializable
data class FlowRelationRow(
    @SerialName("kecamatan_hulu") val kecamatanHulu: String,
    @SerialName("kecamatan_hilir") val kecamatanHilir: String,
    val kekuatan: Int,
    val sumber: String = "",
)

@Serializable
private data class AreaWeatherRow(
    val kecamatan: String,
    @SerialName("rainfall_mm") val rainfallMm: Double,
    @SerialName("weather_desc") val weatherDesc: String? = null,
    @SerialName("observed_at") val observedAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

class UpstreamRepository(
    private val supabase: SupabaseClient,
    private val weatherRepo: WeatherRepository,
    private val kelurahanRepo: KelurahanRepository,
) {
    // Nama kecamatan ditulis berbeda-beda ("Teluk Betung Barat" di aplikasi,
    // "Telukbetung Barat" di BMKG). Pencocokan mengabaikan spasi dan tanda baca.
    private fun normalize(value: String?) =
        value.orEmpty().lowercase().filter { it.isLetterOrDigit() }

    private var cache: List<FlowRelationRow>? = null

    suspend fun relations(): List<FlowRelationRow> {
        cache?.let { return it }
        return runCatching {
            supabase.from("area_flow_relations")
                .select(Columns.list("kecamatan_hulu", "kecamatan_hilir", "kekuatan", "sumber")) {
                    filter { eq("active", true) }
                }
                .decodeList<FlowRelationRow>()
        }.onFailure {
            Log.w(TAG, "gagal membaca relasi hulu-hilir: ${it.message}")
        }.getOrNull()?.also { cache = it } ?: emptyList()
    }

    suspend fun upstreamOf(kecamatan: String): List<FlowRelationRow> {
        val target = normalize(kecamatan)
        if (target.isEmpty()) return emptyList()
        return relations()
            .filter { normalize(it.kecamatanHilir) == target }
            .sortedByDescending { it.kekuatan }
    }

    // Kegagalan sengaja didiamkan selain dicatat: ini pelengkap, bukan syarat
    // laporan bisa terkirim.
    suspend fun saveAreaWeather(kecamatan: String, rainfallMm: Double, desc: String? = null) {
        if (kecamatan.isBlank() || rainfallMm < 0 || rainfallMm > 500) return
        val now = java.time.Instant.now().toString()
        runCatching {
            supabase.from("area_weather").upsert(
                AreaWeatherRow(kecamatan.trim(), rainfallMm, desc, now, now)
            )
        }.onFailure { Log.w(TAG, "gagal menyimpan cuaca wilayah: ${it.message}") }
    }

    // Mengambil prakiraan untuk kecamatan hulu terkuat, menyimpannya, lalu
    // mengembalikan sumbangan terbesar. Dibatasi dua kecamatan supaya satu
    // pengiriman laporan tidak berubah menjadi belasan permintaan ke BMKG.
    suspend fun primeUpstream(kecamatan: String): RiskEngine.Upstream? {
        val upstream = upstreamOf(kecamatan).take(2)
        if (upstream.isEmpty()) return null

        val terkumpul = upstream.mapNotNull { relation ->
            val adm4 = kelurahanRepo.resolveAdm4(relation.kecamatanHulu, "")
            val rain = weatherRepo.rainfallMmFor(adm4) ?: return@mapNotNull null
            saveAreaWeather(relation.kecamatanHulu, rain)
            RiskEngine.Upstream(relation.kecamatanHulu, rain, relation.kekuatan)
        }

        // Yang dipakai adalah sumbangan terbesar, sama seperti alirin_rain_context.
        return terkumpul.maxByOrNull { (it.rainfallMm ?: 0.0) * (it.kekuatan / 3.0) }
    }

    private companion object {
        const val TAG = "AlirinUpstream"
    }
}
