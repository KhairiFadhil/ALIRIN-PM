package com.example.alirinmobile.data.repository

import com.example.alirinmobile.data.network.ApiClient
import com.example.alirinmobile.data.network.dto.AiForecast
import com.example.alirinmobile.data.network.dto.BmkgForecastResponse
import com.example.alirinmobile.data.network.dto.ChatCompletionRequest
import com.example.alirinmobile.data.network.dto.ChatMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.transformLatest
import kotlinx.serialization.json.Json

data class PredictionUiModel(
    val source: Source,
    val ai: AiForecast,
) {
    enum class Source { Groq, Fallback, Loading }
}

/**
 * Turns the raw BMKG forecast into a 4-field forecast (udara, suhu, curah hujan, debit
 * air) using GROQ when GROQ_API_KEY is configured. Falls back to a rule-based estimate
 * when not — keeps the UI alive without a key during dev.
 *
 * Combines the WeatherRepository's selected kelurahan + forecast state, so any change
 * upstream re-triggers a new prediction.
 */
class PredictionRepository(
    private val api: ApiClient,
    private val weather: WeatherRepository,
) {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(): Flow<PredictionUiModel?> = combine(
        weather.selected,
        weather.state,
    ) { kel, state -> kel to state }.transformLatest { (kel, state) ->
        val forecast = (state as? WeatherState.Loaded)?.data
        if (kel == null || forecast == null) {
            emit(null); return@transformLatest
        }
        // Emit a non-null loading placeholder while we wait on GROQ.
        emit(PredictionUiModel(PredictionUiModel.Source.Loading, fallback(forecast)))
        val result = predict(kel, forecast)
        emit(result)
    }

    /** One-shot prediction (used outside the reactive flow, e.g. pull-to-refresh). */
    suspend fun predict(kelurahan: Kelurahan, forecast: BmkgForecastResponse): PredictionUiModel {
        if (!api.groqConfigured) return PredictionUiModel(PredictionUiModel.Source.Fallback, fallback(forecast))
        return runCatching { callGroq(kelurahan, forecast) }
            .map { PredictionUiModel(PredictionUiModel.Source.Groq, it) }
            .getOrElse { PredictionUiModel(PredictionUiModel.Source.Fallback, fallback(forecast)) }
    }

    // ── GROQ ─────────────────────────────────────────────────────
    private suspend fun callGroq(kelurahan: Kelurahan, forecast: BmkgForecastResponse): AiForecast {
        val hours = forecast.data.firstOrNull()?.cuaca?.flatten().orEmpty().take(3)
        val precipSum = hours.sumOf { it.tp ?: 0.0 }
        val avgTemp = hours.mapNotNull { it.t }.average().takeIf { !it.isNaN() } ?: 28.0
        val avgHumidity = hours.mapNotNull { it.hu }.average().takeIf { !it.isNaN() } ?: 80.0
        val descs = hours.mapNotNull { it.weatherDesc }.distinct()

        val systemPrompt = """
            Kamu asisten prediksi cuaca + risiko banjir untuk aplikasi drainase kota Bandar Lampung.
            Output WAJIB JSON valid persis dengan kunci:
            kondisi_udara (string ringkas), suhu_celsius (number), curah_hujan_mm (number, 3 jam ke depan),
            debit_air_ms (number — perkiraan debit air drainase mikro dalam m³/s berdasarkan curah hujan),
            ringkasan (string ≤ 140 char, Bahasa Indonesia, jelas + actionable).
            Estimasi debit pakai rumus sederhana: debit_air_ms ≈ curah_hujan_mm × 0.0015 × faktor_area.
            Jika curah_hujan_mm ≥ 5 ringkasan harus mengandung peringatan genangan.
        """.trimIndent()
        val userPrompt = """
            Wilayah: ${kelurahan.kelurahan}, ${kelurahan.kecamatan}, Bandar Lampung.
            3 jam ke depan dari BMKG:
            - cuaca: ${descs.joinToString(", ").ifBlank { "tidak ada" }}
            - suhu rata-rata: ${"%.1f".format(avgTemp)} °C
            - kelembaban rata-rata: ${"%.0f".format(avgHumidity)} %
            - curah hujan kumulatif: ${"%.1f".format(precipSum)} mm
            Tugas: kembalikan prediksi JSON sesuai schema.
        """.trimIndent()

        val res = api.groqService.chat(
            ChatCompletionRequest(
                messages = listOf(
                    ChatMessage("system", systemPrompt),
                    ChatMessage("user", userPrompt),
                ),
            )
        )
        val raw = res.choices.firstOrNull()?.message?.content
            ?: error("GROQ returned empty content")
        return json.decodeFromString(AiForecast.serializer(), raw)
    }

    // ── Rule-based fallback ──────────────────────────────────────
    private fun fallback(forecast: BmkgForecastResponse): AiForecast {
        val hours = forecast.data.firstOrNull()?.cuaca?.flatten().orEmpty().take(3)
        val precip = hours.sumOf { it.tp ?: 0.0 }
        val temp = hours.firstOrNull()?.t?.toDouble() ?: 28.0
        val desc = hours.firstOrNull()?.weatherDesc?.takeIf { it.isNotBlank() } ?: "Berawan"
        val debit = precip * 0.0015 * 1.2     // crude
        val summary = when {
            precip >= 10 -> "Hujan deras 3 jam ke depan — risiko genangan tinggi di drainase mikro."
            precip >= 5  -> "Hujan sedang 3 jam ke depan — pantau titik rawan."
            precip > 0   -> "Hujan ringan — kondisi drainase masih aman."
            else         -> "Cerah/berawan — tidak ada peringatan banjir."
        }
        return AiForecast(
            kondisiUdara = desc,
            suhuCelsius = temp,
            curahHujanMm = precip,
            debitAirMs = debit,
            ringkasan = summary,
        )
    }
}
