package com.example.alirinmobile.data.network.service

import com.example.alirinmobile.data.network.dto.BmkgForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface BmkgService {
    @GET("publik/prakiraan-cuaca")
    suspend fun forecast(@Query("adm4") adm4: String): BmkgForecastResponse
}
