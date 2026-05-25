package com.example.alirinmobile.data.ml

import com.example.alirinmobile.data.Kelurahan
import com.example.alirinmobile.data.Report
import com.example.alirinmobile.data.ReportStatus
import com.example.alirinmobile.data.RiskLevel
import com.example.alirinmobile.data.SampleData
import com.example.alirinmobile.data.api.BmkgForecastResponse

/**
 * Rule-based ALIRIN flood risk predictor — v1. Designed to be replaceable with a TFLite
 * model later (same input/output shape, only [score] computation changes).
 *
 * Inputs:
 *   - BMKG hourly forecast (next 3 slots used)
 *   - Active citizen reports in this kelurahan/kecamatan
 *   - Historical hotspot density (from bundled SampleData)
 *
 * Output: PredictionResult with score 0–100, RiskLevel, and contributing factor weights
 * so the UI can explain the score to the user.
 */
data class Factor(val label: String, val weight: Int, val rawValue: String)

data class PredictionResult(
    val score: Int,
    val level: RiskLevel,
    val areaLabel: String,
    val factors: List<Factor>,
    val windowHours: Int = 3,
)

object RiskPredictor {

    /** Component weights (must sum to ≤100, percentage of final score). */
    private const val W_PRECIP = 50
    private const val W_REPORTS = 30
    private const val W_HISTORY = 20

    /** Saturation thresholds for normalising raw inputs into 0..1. */
    private const val PRECIP_SAT_MM = 8.0          // 8 mm/h in next-3h window = max contribution
    private const val REPORTS_SAT_COUNT = 6        // 6 active reports nearby = max contribution
    private const val HISTORY_SAT_COUNT = 4        // 4 historical hotspots in kecamatan = max

    fun predict(
        forecast: BmkgForecastResponse?,
        reports: List<Report>,
        kelurahan: Kelurahan?,
    ): PredictionResult {
        val areaLabel = kelurahan?.let { "${it.kelurahan}, ${it.kecamatan}" } ?: "Bandar Lampung"

        // ── 1. Precipitation factor (BMKG, next 3 hours) ────────────────
        val nextHours = forecast?.data
            ?.firstOrNull()
            ?.cuaca
            ?.flatten()
            .orEmpty()
            .take(3)
        val totalPrecipMm = nextHours.sumOf { it.tp ?: 0.0 }
        val precipNorm = (totalPrecipMm / PRECIP_SAT_MM).coerceIn(0.0, 1.0)
        val precipPoints = (precipNorm * W_PRECIP).toInt()

        // ── 2. Active citizen reports in this area ──────────────────────
        val activeStatuses = setOf(
            ReportStatus.Pending, ReportStatus.Verified,
            ReportStatus.Scheduled, ReportStatus.InProgress,
        )
        val nearbyActive = reports.count { r ->
            r.status in activeStatuses && kelurahan != null && (
                r.kelurahan.equals(kelurahan.kelurahan, ignoreCase = true) ||
                r.kecamatan.equals(kelurahan.kecamatan, ignoreCase = true)
            )
        }
        val reportsNorm = (nearbyActive.toDouble() / REPORTS_SAT_COUNT).coerceIn(0.0, 1.0)
        val reportsPoints = (reportsNorm * W_REPORTS).toInt()

        // ── 3. Historical hotspots density in same kecamatan ────────────
        val historyCount = if (kelurahan == null) 0
            else SampleData.hotspots.count { it.kec.equals(kelurahan.kecamatan, ignoreCase = true) }
        val historyNorm = (historyCount.toDouble() / HISTORY_SAT_COUNT).coerceIn(0.0, 1.0)
        val historyPoints = (historyNorm * W_HISTORY).toInt()

        val score = (precipPoints + reportsPoints + historyPoints).coerceIn(0, 100)
        val level = when {
            score >= 80 -> RiskLevel.Kritis
            score >= 60 -> RiskLevel.Tinggi
            score >= 40 -> RiskLevel.Waspada
            else -> RiskLevel.Normal
        }

        return PredictionResult(
            score = score,
            level = level,
            areaLabel = areaLabel,
            factors = listOf(
                Factor(
                    label = "Curah hujan BMKG (3 jam)",
                    weight = precipPoints,
                    rawValue = "%.1f mm".format(totalPrecipMm),
                ),
                Factor(
                    label = "Laporan aktif sekitar",
                    weight = reportsPoints,
                    rawValue = "$nearbyActive laporan",
                ),
                Factor(
                    label = "Titik historis kecamatan",
                    weight = historyPoints,
                    rawValue = "$historyCount titik",
                ),
            ),
        )
    }
}
