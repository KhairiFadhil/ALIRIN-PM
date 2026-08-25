package com.example.alirinmobile.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(val role: String, val content: String)

@Serializable
data class ChatCompletionRequest(
    val model: String = com.example.alirinmobile.BuildConfig.GROQ_MODEL,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.2,
    // Model gpt-oss memancarkan bidang "reasoning" yang ikut memakan anggaran
    // token sebelum JSON-nya selesai ditulis. Dengan effort "low", pemakaian
    // turun dari sekitar 1050 ke 240 token dan jawabannya tetap lengkap.
    // Diuji 26 Agustus 2026: pada effort bawaan, max_tokens 512 gagal dengan
    // "Failed to validate JSON" karena keluarannya terpotong di tengah.
    // Dikosongkan lewat GROQ_REASONING_EFFORT= bila modelnya menolak parameter ini.
    @SerialName("reasoning_effort") val reasoningEffort: String? =
        com.example.alirinmobile.BuildConfig.GROQ_REASONING_EFFORT.ifBlank { null },
    @SerialName("max_tokens") val maxTokens: Int = 1024,
    @SerialName("response_format") val responseFormat: ResponseFormat? = ResponseFormat("json_object"),
)

@Serializable
data class ResponseFormat(val type: String)

@Serializable
data class ChatCompletionResponse(
    val id: String? = null,
    val model: String? = null,
    val choices: List<Choice> = emptyList(),
)

@Serializable
data class Choice(
    val index: Int = 0,
    val message: ChatMessage = ChatMessage("assistant", ""),
    @SerialName("finish_reason") val finishReason: String? = null,
)

@Serializable
data class AiForecast(
    @SerialName("kondisi_udara")  val kondisiUdara: String,
    @SerialName("suhu_celsius")   val suhuCelsius: Double,
    @SerialName("curah_hujan_mm") val curahHujanMm: Double,
    @SerialName("debit_air_ms")   val debitAirMs: Double,
    val ringkasan: String,

    val rekomendasi: List<String> = emptyList(),
)
