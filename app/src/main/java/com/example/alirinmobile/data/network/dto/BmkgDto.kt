package com.example.alirinmobile.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val cuaca: List<List<BmkgWeatherHour>> = emptyList(),
)

@Serializable
data class BmkgWeatherHour(
    @SerialName("datetime")        val datetime: String? = null,
    @SerialName("local_datetime")  val localDatetime: String? = null,
    val t: Int? = null,
    val tcc: Int? = null,
    val tp: Double? = null,
    val weather: Int? = null,
    @SerialName("weather_desc")    val weatherDesc: String? = null,
    @SerialName("weather_desc_en") val weatherDescEn: String? = null,
    val wd: String? = null,
    val ws: Double? = null,
    val hu: Int? = null,
    val vs: Int? = null,
    @SerialName("time_index")      val timeIndex: String? = null,
)
