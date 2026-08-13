package com.example.alirinmobile.data.auth

enum class Role { Citizen, Staff, Admin }

// Peran Supabase (app_metadata.role / user_metadata.role) → mobile Role
fun supabaseRoleFromString(raw: String?): Role = when (raw?.trim()?.lowercase()) {
    "admin" -> Role.Admin
    "petugas" -> Role.Staff
    else -> Role.Citizen
}

data class AuthSession(
    val userId: Int,       // stable seed dari hash Supabase UUID, dipakai UI avatar
    val supabaseUserId: String,
    val username: String,  // email
    val displayName: String,
    val role: Role,
    val avatarUrl: String? = null,
)
