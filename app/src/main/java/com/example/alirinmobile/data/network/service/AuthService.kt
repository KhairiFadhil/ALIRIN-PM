package com.example.alirinmobile.data.network.service

import com.example.alirinmobile.data.network.dto.LoginRequest
import com.example.alirinmobile.data.network.dto.LoginResponse
import com.example.alirinmobile.data.network.dto.MeResponse
import com.example.alirinmobile.data.network.dto.RefreshRequest
import com.example.alirinmobile.data.network.dto.RefreshResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthService {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("auth/me")
    suspend fun me(@Header("Authorization") bearer: String): MeResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): RefreshResponse
}
