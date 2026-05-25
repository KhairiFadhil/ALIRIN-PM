package com.example.alirinmobile.data.auth

enum class Role { Citizen, Staff, Admin }

/**
 * dummyjson.com's standard sample accounts. We map specific usernames to ALIRIN roles
 * so the user can test all three role gates without needing a real backend.
 *
 * Test accounts (from dummyjson):
 *   staff  → username: "emilys"   password: "emilyspass"   → mapped to Staff
 *   admin  → username: "michaelw" password: "michaelwpass" → mapped to Admin
 *   user   → username: "sophiab"  password: "sophiabpass"  → mapped to Citizen
 *   (anything else logs in as Citizen)
 */
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
