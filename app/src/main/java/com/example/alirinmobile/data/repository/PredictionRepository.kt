package com.example.alirinmobile.data.repository

import android.util.Log
import com.example.alirinmobile.data.network.ApiClient
import com.example.alirinmobile.data.network.dto.AiForecast
import com.example.alirinmobile.data.network.dto.BmkgForecastResponse
import com.example.alirinmobile.data.network.service.WeatherBriefRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.transformLatest

data class PredictionUiModel(
    val source: Source,
    val ai: AiForecast,
) {
    enum class Source { Groq, Fallback, Loading }
}

class PredictionRepository(
    private val api: ApiClient,
    private val weather: WeatherRepository,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(): Flow<PredictionUiModel?> = combine(
        weather.selected,
        weather.state,
    ) { kel, state -> kel to state }.transformLatest { (kel, state) ->
        val forecast = (state as? WeatherState.Loaded)?.data
        if (kel == null || forecast == null) {
            emit(null); return@transformLatest
        }

        emit(PredictionUiModel(PredictionUiModel.Source.Loading, fallback(forecast)))
        val result = predict(kel, forecast)
        emit(result)
    }

    // P-1 - Panggilan AI pindah ke Edge Function weather-brief.
    //
    // Kunci Groq tidak lagi ada di perangkat. Fungsi itu juga sudah membawa
    // baseline berbasis aturannya sendiri, tetapi baseline lokal di bawah tetap
    // dipertahankan sebagai jaring terakhir: kalau jaringan mati, kartu tetap
    // menampilkan sesuatu yang benar dari data BMKG yang sudah terlanjur ada.
    suspend fun predict(kelurahan: Kelurahan, forecast: BmkgForecastResponse): PredictionUiModel {
        if (!api.functionsConfigured) {
            Log.i(TAG, "Supabase belum dikonfigurasi, memakai baseline berbasis aturan.")
            return PredictionUiModel(PredictionUiModel.Source.Fallback, fallback(forecast))
        }

        return runCatching {
            api.functionsService.weatherBrief(
                WeatherBriefRequest(adm4 = kelurahan.adm4, kelurahan = kelurahan.kelurahan)
            )
        }.map { brief ->
            val ai = AiForecast(
                kondisiUdara = brief.kondisiUdara,
                suhuCelsius = brief.suhuCelsius,
                curahHujanMm = brief.curahHujanMm,
                debitAirMs = brief.debitAirMs,
                ringkasan = brief.ringkasan,
                rekomendasi = brief.rekomendasi,
            )
            // Label sumber mengikuti apa yang benar-benar terjadi di server.
            // Kartu berlabel "Analisis AI" yang isinya if-else adalah hal yang
            // harus dihindari, bukan disamarkan.
            val source = if (brief.source == "ai") {
                PredictionUiModel.Source.Groq
            } else {
                PredictionUiModel.Source.Fallback
            }
            PredictionUiModel(source, ai)
        }.getOrElse { error ->
            Log.w(TAG, "Edge Function weather-brief gagal: ${error.message}. Beralih ke baseline lokal.")
            PredictionUiModel(PredictionUiModel.Source.Fallback, fallback(forecast))
        }
    }

    private companion object {
        const val TAG = "AlirinPrediction"
    }

    private fun fallback(forecast: BmkgForecastResponse): AiForecast {
        val hours = forecast.data.firstOrNull()?.cuaca?.flatten().orEmpty().take(1)
        val precip = hours.sumOf { it.tp ?: 0.0 }
        val temp = hours.firstOrNull()?.t?.toDouble() ?: 28.0
        val desc = hours.firstOrNull()?.weatherDesc?.takeIf { it.isNotBlank() } ?: "Berawan"
        val debit = precip * 0.0015 * 1.2
        val summary = when {
            precip >= 10 -> "Hujan deras 3 jam ke depan — risiko genangan tinggi di drainase mikro."
            precip >= 5  -> "Hujan sedang 3 jam ke depan — pantau titik rawan."
            precip > 0   -> "Hujan ringan — kondisi drainase masih aman."
            else         -> "Cerah/berawan — tidak ada peringatan banjir."
        }
        val recs = when {
            precip >= 10 -> listOf(
                "Bersihkan sampah di mulut got sekarang",
                "Hindari area cekungan & underpass",
                "Laporkan titik genangan via ALIRIN",
            )
            precip >= 5  -> listOf(
                "Pantau drainase dekat rumah",
                "Siapkan karung pasir bila perlu",
            )
            precip > 0   -> listOf("Cek saluran air tetap lancar")
            else         -> listOf("Manfaatkan cuaca cerah untuk bersihkan got")
        }
        return AiForecast(
            kondisiUdara = desc,
            suhuCelsius = temp,
            curahHujanMm = precip,
            debitAirMs = debit,
            ringkasan = summary,
            rekomendasi = recs,
        )
    }
}
