package com.example.alirinmobile.data.network.dto

// Bentuk keluaran prakiraan. DTO permintaan Groq sudah dihapus: sejak P-1
// panggilan model berjalan di Edge Function, bukan di perangkat.

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AiForecast(
    @SerialName("kondisi_udara")  val kondisiUdara: String,
    @SerialName("suhu_celsius")   val suhuCelsius: Double,
    @SerialName("curah_hujan_mm") val curahHujanMm: Double,
    @SerialName("debit_air_ms")   val debitAirMs: Double,
    val ringkasan: String,

    val rekomendasi: List<String> = emptyList(),
)
