package com.example.alirinmobile.data.network.service

import com.example.alirinmobile.data.network.dto.ChatCompletionRequest
import com.example.alirinmobile.data.network.dto.ChatCompletionResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface GroqService {
    @POST("openai/v1/chat/completions")
    suspend fun chat(@Body body: ChatCompletionRequest): ChatCompletionResponse
}
