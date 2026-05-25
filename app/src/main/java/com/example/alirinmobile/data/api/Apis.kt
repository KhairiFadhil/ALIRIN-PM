package com.example.alirinmobile.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/** https://dummyjson.com/docs/auth */
interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("auth/me")
    suspend fun me(@Header("Authorization") bearer: String): MeResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): RefreshResponse
}

/** https://api.bmkg.go.id/publik/prakiraan-cuaca?adm4=... */
interface BmkgApi {
    @GET("publik/prakiraan-cuaca")
    suspend fun forecast(@Query("adm4") adm4: String): BmkgForecastResponse
}

/**
 * Public map / hotspots feed. Right now there's no real backend, so we keep this as a
 * Retrofit interface that returns from the configured base URL — drop in the real URL
 * later and it'll start working without UI changes.
 */
interface PublicMapApi {
    @GET("public/hotspots")
    suspend fun hotspots(): List<HotspotDto>
}
