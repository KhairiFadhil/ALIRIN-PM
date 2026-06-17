package com.example.alirinmobile.data.auth

enum class Role { Citizen, Staff, Admin }

fun mapDummyUsernameToRole(username: String): Role = when (username.lowercase()) {
    "emilys" -> Role.Staff
    "michaelw" -> Role.Admin
    else -> Role.Citizen
}

data class AuthSession(
    val userId: Int,
    val username: String,
    val displayName: String,
    val role: Role,
    val token: String,
    val refreshToken: String? = null,
    val avatarUrl: String? = null,
)
