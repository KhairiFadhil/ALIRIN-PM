package com.example.alirinmobile.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
    val expiresInMins: Int = 60,
)

@Serializable
data class LoginResponse(
    val id: Int,
    val username: String,
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val gender: String = "",
    val image: String = "",
    val accessToken: String,
    val refreshToken: String = "",
)

@Serializable
data class MeResponse(
    val id: Int,
    val username: String,
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val image: String = "",
    val role: String? = null,
)

@Serializable
data class RefreshRequest(val refreshToken: String, val expiresInMins: Int = 60)

@Serializable
data class RefreshResponse(val accessToken: String, val refreshToken: String)
