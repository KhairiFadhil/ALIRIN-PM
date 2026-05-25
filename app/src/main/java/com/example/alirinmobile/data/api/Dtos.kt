package com.example.alirinmobile.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── dummyjson.com /auth/login ───────────────────────────────────
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

/** POST /auth/refresh — body { refreshToken, expiresInMins } returns {accessToken, refreshToken}. */
@Serializable
data class RefreshRequest(val refreshToken: String, val expiresInMins: Int = 60)

@Serializable
data class RefreshResponse(val accessToken: String, val refreshToken: String)

@Serializable
data class MeResponse(
    val id: Int,
    val username: String,
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val image: String = "",
    /** Some dummyjson users include a "role" field; default to "user". */
    val role: String? = null,
)

// ── BMKG public forecast API ────────────────────────────────────
// GET https://api.bmkg.go.id/publik/prakiraan-cuaca?adm4=18.71.04.1001
@Serializable
data class BmkgForecastResponse(
    val lokasi: BmkgLokasi? = null,
    val data: List<BmkgDataBlock> = emptyList(),
)

@Serializable
data class BmkgLokasi(
    val adm1: String? = null,
    val adm2: String? = null,
    val adm3: String? = null,
    val adm4: String? = null,
    val provinsi: String? = null,
    val kotkab: String? = null,
    val kecamatan: String? = null,
    val desa: String? = null,
    val lon: Double? = null,
    val lat: Double? = null,
)

@Serializable
data class BmkgDataBlock(
    val lokasi: BmkgLokasi? = null,
    /** Nested 2D array of hourly forecast slots. */
    val cuaca: List<List<BmkgWeatherHour>> = emptyList(),
)

@Serializable
data class BmkgWeatherHour(
    @SerialName("datetime")  val datetime: String? = null,
    @SerialName("local_datetime") val localDatetime: String? = null,
    val t: Int? = null,         // temperature °C
    val tcc: Int? = null,       // total cloud cover %
    val tp: Double? = null,     // total precipitation mm
    val weather: Int? = null,   // weather code
    @SerialName("weather_desc") val weatherDesc: String? = null,
    @SerialName("weather_desc_en") val weatherDescEn: String? = null,
    val wd: String? = null,
    val ws: Double? = null,
    val hu: Int? = null,        // humidity %
    val vs: Int? = null,        // visibility
    @SerialName("time_index") val timeIndex: String? = null,
)

// ── Public map API (stub) ───────────────────────────────────────
@Serializable
data class HotspotDto(
    val id: Int,
    val risk: String,
    val score: Int,
    val count: Int,
    val lat: Double,
    val lng: Double,
    val name: String,
    val kel: String,
    val kec: String,
    val cat: String,
    val status: String,
    val src: String,
    val dist: String,
    @SerialName("when") val whenLabel: String,
)
