package com.example.alirinmobile.data.scoring

import com.example.alirinmobile.data.RiskBreakdownItem
import com.example.alirinmobile.data.RiskLevel
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

// Implementasi rumus di C:\ALIRIN\docs\RISK-ENGINE.md, kembar dengan
// app/src/domain/scoring.js (web) dan trigger alirin_apply_risk (Supabase).
//
// Supabase yang otoritatif: trigger selalu menimpa skor yang dikirim klien.
// Mesin ini dipakai untuk pratinjau langsung dan saat laporan dibuat luring.
object RiskEngine {

    const val VERSION = 2

    private const val WEIGHT_SEVERITY = 35
    private const val WEIGHT_HISTORY = 25
    private const val WEIGHT_WEATHER = 25
    private const val WEIGHT_LOCATION = 15

    private const val HISTORY_RADIUS_KM = 0.35
    private const val HISTORY_WINDOW_MS = 180L * 24 * 60 * 60 * 1000
    private const val HISTORY_POINTS_PER_REPORT = 20
    private const val BOUNDING_BOX_DELTA = 0.0035

    data class Facility(val name: String, val lat: Double, val lng: Double)

    // Sama persis dengan seed public_facilities pada migrasi 20260826090000.
    val PUBLIC_FACILITIES = listOf(
        Facility("RSUD Abdul Moeloek", -5.3994, 105.2526),
        Facility("Stasiun Tanjung Karang", -5.4077, 105.2581),
        Facility("Pasar Bambu Kuning", -5.4128, 105.2586),
        Facility("Terminal Rajabasa", -5.3717, 105.2406),
        Facility("Universitas Lampung", -5.3648, 105.2438),
        Facility("Lapangan Saburai", -5.4238, 105.2588),
        Facility("Pelabuhan Panjang", -5.4729, 105.3182),
        Facility("Pasar Kangkung", -5.4395, 105.2674),
    )

    // Laporan tetangga yang sudah ada, dipakai untuk faktor Histori.
    data class NeighbourReport(
        val id: String,
        val lat: Double,
        val lng: Double,
        val createdAtMs: Long,
        val status: String,
    )

    data class Result(
        val score: Int,
        val level: RiskLevel,
        val breakdown: List<RiskBreakdownItem>,
    )

    fun distanceKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val h = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
        return earthRadiusKm * 2 * atan2(sqrt(h), sqrt(max(0.0, 1 - h)))
    }

    fun severityScore(severity: String): Int = when (severity) {
        "kritis" -> 100
        "parah" -> 80
        "sedang" -> 55
        else -> 25
    }

    // null berarti data BMKG tidak tersedia, bukan "tidak hujan".
    fun weatherScore(rainfallMm: Double?): Int? = when {
        rainfallMm == null || rainfallMm < 0 -> null
        rainfallMm == 0.0 -> 0
        rainfallMm < 1 -> 20
        rainfallMm < 5 -> 45
        rainfallMm < 10 -> 70
        rainfallMm < 20 -> 88
        else -> 100
    }

    fun describeRainfall(rainfallMm: Double?): String = when {
        rainfallMm == null || rainfallMm < 0 -> "Data BMKG belum tersedia"
        rainfallMm == 0.0 -> "Tidak hujan"
        rainfallMm < 1 -> "Gerimis"
        rainfallMm < 5 -> "Hujan ringan"
        rainfallMm < 10 -> "Hujan sedang"
        rainfallMm < 20 -> "Hujan lebat"
        else -> "Hujan sangat lebat"
    }

    fun historyCount(
        id: String?,
        lat: Double,
        lng: Double,
        createdAtMs: Long,
        neighbours: List<NeighbourReport>,
    ): Int = neighbours.count { other ->
        other.id != id &&
            other.status != "ditolak" &&
            other.createdAtMs <= createdAtMs &&
            other.createdAtMs >= createdAtMs - HISTORY_WINDOW_MS &&
            abs(other.lat - lat) <= BOUNDING_BOX_DELTA &&
            abs(other.lng - lng) <= BOUNDING_BOX_DELTA &&
            distanceKm(lat, lng, other.lat, other.lng) <= HISTORY_RADIUS_KM
    }

    fun historyScore(count: Int): Int = min(100, count * HISTORY_POINTS_PER_REPORT)

    fun nearestFacility(lat: Double, lng: Double): Pair<Facility, Double>? =
        PUBLIC_FACILITIES
            .map { it to distanceKm(lat, lng, it.lat, it.lng) }
            .minByOrNull { it.second }

    fun locationScore(lat: Double, lng: Double): Int {
        val distance = nearestFacility(lat, lng)?.second ?: return 10
        return when {
            distance <= 0.25 -> 100
            distance <= 0.5 -> 80
            distance <= 1.0 -> 58
            distance <= 2.0 -> 34
            else -> 10
        }
    }

    fun levelOf(score: Int): RiskLevel = when {
        score >= 80 -> RiskLevel.Kritis
        score >= 60 -> RiskLevel.Tinggi
        score >= 40 -> RiskLevel.Waspada
        else -> RiskLevel.Normal
    }

    // Cuaca null: bobot 25% dibagi ulang proporsional ke tiga faktor lain,
    // supaya laporan yang datanya kebetulan tidak terambil tidak terhukum.
    fun combine(severity: Int, history: Int, weather: Int?, location: Int): Int {
        val activeTotal = WEIGHT_SEVERITY + WEIGHT_HISTORY + WEIGHT_LOCATION +
            (if (weather == null) 0 else WEIGHT_WEATHER)
        val sum = severity * WEIGHT_SEVERITY +
            history * WEIGHT_HISTORY +
            (weather ?: 0) * WEIGHT_WEATHER +
            location * WEIGHT_LOCATION
        return (sum.toDouble() / activeTotal).roundToInt().coerceIn(0, 100)
    }

    fun evaluate(
        id: String?,
        severity: String,
        lat: Double,
        lng: Double,
        createdAtMs: Long,
        rainfallMm: Double?,
        photoCount: Int,
        description: String,
        neighbours: List<NeighbourReport> = emptyList(),
    ): Result {
        val severityRaw = severityScore(severity)
        val count = historyCount(id, lat, lng, createdAtMs, neighbours)
        val historyRaw = historyScore(count)
        val weatherRaw = weatherScore(rainfallMm)
        val locationRaw = locationScore(lat, lng)
        val score = combine(severityRaw, historyRaw, weatherRaw, locationRaw)

        val activeTotal = WEIGHT_SEVERITY + WEIGHT_HISTORY + WEIGHT_LOCATION +
            (if (weatherRaw == null) 0 else WEIGHT_WEATHER)
        fun points(raw: Int, weight: Int) = (raw.toDouble() * weight / activeTotal).roundToInt()
        fun effectiveWeight(weight: Int) = (weight.toDouble() * 100 / activeTotal).roundToInt()

        val nearest = nearestFacility(lat, lng)
        val evidenceFilled = listOf(photoCount > 0, description.trim().length >= 10, true).count { it }

        val breakdown = listOf(
            RiskBreakdownItem(
                id = "severity",
                label = "Keparahan laporan",
                points = points(severityRaw, WEIGHT_SEVERITY),
                weight = effectiveWeight(WEIGHT_SEVERITY),
                detail = "Tingkat $severity",
            ),
            RiskBreakdownItem(
                id = "history",
                label = "Histori kejadian",
                points = points(historyRaw, WEIGHT_HISTORY),
                weight = effectiveWeight(WEIGHT_HISTORY),
                detail = "$count laporan lain dalam radius 350 m, 180 hari terakhir",
            ),
            RiskBreakdownItem(
                id = "weather",
                label = "Cuaca",
                points = if (weatherRaw == null) 0 else points(weatherRaw, WEIGHT_WEATHER),
                weight = if (weatherRaw == null) 0 else effectiveWeight(WEIGHT_WEATHER),
                detail = if (weatherRaw == null) {
                    "Data BMKG tidak tersedia, bobot dialihkan ke faktor lain"
                } else {
                    "${describeRainfall(rainfallMm)}, ${"%.1f".format(rainfallMm)} mm dalam 3 jam (BMKG)"
                },
            ),
            RiskBreakdownItem(
                id = "location",
                label = "Dampak lokasi",
                points = points(locationRaw, WEIGHT_LOCATION),
                weight = effectiveWeight(WEIGHT_LOCATION),
                detail = nearest?.let { "${it.first.name}, ${"%.1f".format(it.second)} km" }
                    ?: "Tidak ada fasilitas publik terdekat",
            ),
            RiskBreakdownItem(
                id = "bukti",
                label = "Kelengkapan bukti",
                points = 0,
                weight = 0,
                detail = "$photoCount foto, kelengkapan ${(evidenceFilled * 100) / 3}% (belum dibobot)",
            ),
            RiskBreakdownItem(
                id = "sensor",
                label = "Sensor lapangan",
                points = 0,
                weight = 0,
                detail = "Menunggu integrasi IoT (roadmap Tahap 4)",
            ),
        )

        return Result(score = score, level = levelOf(score), breakdown = breakdown)
    }
}
