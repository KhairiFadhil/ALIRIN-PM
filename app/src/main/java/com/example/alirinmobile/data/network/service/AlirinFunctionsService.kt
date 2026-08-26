package com.example.alirinmobile.data.network.service

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

// P-1 - Edge Function ALIRIN.
//
// Kunci Groq tidak lagi ikut ke perangkat. Sebelumnya buildConfigField menaruh
// kunci sebagai string biasa di dalam DEX, dan siapa pun yang mengunduh APK bisa
// mengekstraknya (temuan D-4 laporan audit). Sekarang kuncinya hanya ada sebagai
// secret project, dan aplikasi cukup memanggil fungsinya.

@Serializable
data class WeatherBriefRequest(
    val adm4: String,
    val kelurahan: String,
)

@Serializable
data class WeatherBriefResponse(
    val source: String = "baseline",
    val model: String? = null,
    @SerialName("kondisi_udara") val kondisiUdara: String = "-",
    @SerialName("suhu_celsius") val suhuCelsius: Double = 28.0,
    @SerialName("curah_hujan_mm") val curahHujanMm: Double = 0.0,
    @SerialName("debit_air_ms") val debitAirMs: Double = 0.0,
    val ringkasan: String = "",
    val rekomendasi: List<String> = emptyList(),
    val error: String? = null,
)

@Serializable
data class AssessRiskRequest(
    @SerialName("report_id") val reportId: String,
)

@Serializable
data class AssessRiskResponse(
    val source: String = "baseline",
    @SerialName("baseline_score") val baselineScore: Int? = null,
    @SerialName("ai_risk_score") val aiRiskScore: Int? = null,
    val alasan: String? = null,
    val rekomendasi: List<String> = emptyList(),
    val model: String? = null,
    val error: String? = null,
)

interface AlirinFunctionsService {
    @POST("functions/v1/weather-brief")
    suspend fun weatherBrief(@Body body: WeatherBriefRequest): WeatherBriefResponse

    @POST("functions/v1/assess-risk")
    suspend fun assessRisk(@Body body: AssessRiskRequest): AssessRiskResponse
}
