package com.example.alirinmobile.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(val role: String, val content: String)

@Serializable
data class ChatCompletionRequest(
    val model: String = "llama-3.1-8b-instant",
    val messages: List<ChatMessage>,
    val temperature: Double = 0.2,
    @SerialName("max_tokens") val maxTokens: Int = 512,
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

/**
 * Structured response we ask the LLM to produce. Stored separately from the LLM DTOs so
 * the repository can map raw JSON → domain model.
 */
@Serializable
data class AiForecast(
    @SerialName("kondisi_udara")  val kondisiUdara: String,   // e.g. "Berawan tebal"
    @SerialName("suhu_celsius")   val suhuCelsius: Double,    // current/next-hour
    @SerialName("curah_hujan_mm") val curahHujanMm: Double,   // 3h cumulative
    @SerialName("debit_air_ms")   val debitAirMs: Double,     // m³/s estimated water debit
    val ringkasan: String,                                    // 1-sentence summary
)
