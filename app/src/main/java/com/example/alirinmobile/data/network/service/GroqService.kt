package com.example.alirinmobile.data.network.service

import com.example.alirinmobile.data.network.dto.ChatCompletionRequest
import com.example.alirinmobile.data.network.dto.ChatCompletionResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * GROQ Cloud — OpenAI-compatible chat completions for fast Llama / Mixtral inference.
 * Used by PredictionRepository to convert BMKG forecast → human-readable forecast +
 * water-debit prediction.
 */
interface GroqService {
    @POST("openai/v1/chat/completions")
    suspend fun chat(@Body body: ChatCompletionRequest): ChatCompletionResponse
}
