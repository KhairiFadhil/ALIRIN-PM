package com.example.alirinmobile.data.repository

import com.example.alirinmobile.data.HistoryEntry
import com.example.alirinmobile.data.Hotspot
import com.example.alirinmobile.data.HotspotSource
import com.example.alirinmobile.data.Kategoris
import com.example.alirinmobile.data.NearbyTeaser
import com.example.alirinmobile.data.Photo
import com.example.alirinmobile.data.Report
import com.example.alirinmobile.data.ReportMode
import com.example.alirinmobile.data.ReportStatus
import com.example.alirinmobile.data.RiskLevel
import com.example.alirinmobile.feature.lapor.LaporForm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class ReportRepository {

    private val _reports = MutableStateFlow<List<Report>>(seed())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    fun observeReports() = reports
    fun observeReport(id: String): kotlinx.coroutines.flow.Flow<Report?> =
        reports.map { list -> list.find { it.id == id } }
    fun snapshot(id: String): Report? = _reports.value.find { it.id == id }

    private val _draft = MutableStateFlow<LaporForm?>(null)
    val draft: StateFlow<LaporForm?> = _draft.asStateFlow()

    fun saveDraft(form: LaporForm) { _draft.value = form }
    fun clearDraft() { _draft.value = null }

    private val codeSeq = AtomicInteger(4220)

    data class SubmitResult(val id: String, val code: String)

    fun submitReport(form: LaporForm, mode: ReportMode): SubmitResult {
        val id = "r_" + UUID.randomUUID().toString().take(8)
        val n = codeSeq.getAndIncrement()
        val code = "ALR-2026-0$n"
        val now = nowLabel()

        val sevWeight = when (form.severity) {
            "ringan" -> 20; "sedang" -> 45; "parah" -> 65; "kritis" -> 85
            else -> 30
        }
        val multiplier = if (mode == ReportMode.Lengkap) 1.0 else 0.7
        val score = (sevWeight * multiplier).toInt().coerceIn(0, 100)
        val risk = when {
            score >= 80 -> RiskLevel.Kritis
            score >= 60 -> RiskLevel.Tinggi
            score >= 40 -> RiskLevel.Waspada
            else -> RiskLevel.Normal
        }
        val category = Kategoris.find { it.id == form.kategori }?.label ?: "Drainase"

        val report = Report(
            id = id, code = code, mode = mode,
            status = ReportStatus.Pending,
            risk = risk, score = score, category = category,
            kelurahan = form.kelurahan.ifBlank { "Tidak diketahui" },
            kecamatan = form.kecamatan.ifBlank { "Bandar Lampung" },
            address = form.alamat.takeIf { it.isNotBlank() },
            createdAt = now, updatedAt = now,
            description = form.deskripsi,
            photos = form.photos.size,
            history = listOf(HistoryEntry(ReportStatus.Pending, now, "Laporan masuk dari aplikasi.")),
        )
        _reports.value = listOf(report) + _reports.value
        clearDraft()
        return SubmitResult(id, code)
    }

    fun updateReportStatus(
        reportId: String,
        newStatus: ReportStatus,
        note: String? = null,
        actorLabel: String? = null,
    ) {
        val current = _reports.value
        val report = current.find { it.id == reportId } ?: return
        val now = nowLabel()
        val combinedNote = listOfNotNull(actorLabel?.let { "[$it]" }, note?.takeIf { it.isNotBlank() })
            .joinToString(" ").takeIf { it.isNotBlank() }
        val updated = report.copy(
            status = newStatus,
            updatedAt = now,
            history = report.history + HistoryEntry(newStatus, now, combinedNote),
        )
        _reports.value = current.map { if (it.id == reportId) updated else it }
    }

    fun hotspots(): List<Hotspot> = HotspotSeed
    fun nearbyTeasers(): List<NearbyTeaser> = NearbyTeaserSeed

    private fun nowLabel(): String =
        SimpleDateFormat("d MMM · HH:mm", Locale("id")).format(Date())

    private fun seed(): List<Report> = ReportSeed
}

internal val ReportSeed: List<Report> = listOf(
    Report(
        id = "r1", code = "ALR-2026-04217", mode = ReportMode.Lengkap,
        status = ReportStatus.InProgress, risk = RiskLevel.Tinggi, score = 72,
        category = "Sumbatan sampah",
        kelurahan = "Pinang Jaya", kecamatan = "Kemiling",
        address = "Jl. Imam Bonjol depan SD 2",
        createdAt = "12 Mei · 14:30", updatedAt = "13 Mei · 09:15",
        description = "Sampah plastik & daun menyumbat got, air mulai menggenang ke jalan.",
        photos = 2,
        history = listOf(
            HistoryEntry(ReportStatus.Pending,    "12 Mei · 14:30", "Laporan masuk dari aplikasi."),
            HistoryEntry(ReportStatus.Verified,   "12 Mei · 16:08", "Petugas verifikasi via foto & 3 laporan terdekat."),
            HistoryEntry(ReportStatus.Scheduled,  "13 Mei · 08:30", "Tim Kebersihan Kemiling dijadwalkan."),
            HistoryEntry(ReportStatus.InProgress, "13 Mei · 09:15", "Tim sudah di lokasi.", live = true),
        ),
    ),
    Report(
        id = "r2", code = "ALR-2026-04201", mode = ReportMode.Cepat,
        status = ReportStatus.Verified, risk = RiskLevel.Waspada, score = 48,
        category = "Aliran lambat",
        kelurahan = "Way Halim Permai", kecamatan = "Way Halim",
        createdAt = "11 Mei · 18:42",
        history = listOf(
            HistoryEntry(ReportStatus.Pending,  "11 Mei · 18:42", "Lapor Cepat masuk."),
            HistoryEntry(ReportStatus.Verified, "12 Mei · 10:15", "Diverifikasi gotong-royong (3 warga)."),
        ),
    ),
    Report(
        id = "r3", code = "ALR-2026-04088", mode = ReportMode.Lengkap,
        status = ReportStatus.Completed, risk = RiskLevel.Normal, score = 22,
        category = "Drainase rusak",
        kelurahan = "Kemiling Permai", kecamatan = "Kemiling",
        createdAt = "5 Mei · 09:14",
        description = "Tutup got pecah, anak2 hampir jatuh.",
        photos = 3,
        history = listOf(
            HistoryEntry(ReportStatus.Pending,    "5 Mei · 09:14"),
            HistoryEntry(ReportStatus.Verified,   "5 Mei · 11:00", "Admin verifikasi."),
            HistoryEntry(ReportStatus.Scheduled,  "5 Mei · 13:30", "Dijadwalkan 6 Mei pagi."),
            HistoryEntry(ReportStatus.InProgress, "6 Mei · 08:20", "Tim pasang tutup darurat."),
            HistoryEntry(ReportStatus.Completed,  "6 Mei · 11:45", "Tutup permanen terpasang."),
        ),
    ),
)

internal val HotspotSeed: List<Hotspot> = listOf(
    Hotspot(1, RiskLevel.Kritis,  86, 7, 0.24f, 0.30f, -5.3700, 105.2900, "Sukabumi Indah RT 03",     "Sukabumi",          "Sukabumi",                "Genangan jalan",  ReportStatus.Verified,   HotspotSource.Warga,    "1.8 km", "2 jam lalu"),
    Hotspot(2, RiskLevel.Tinggi,  72, 4, 0.40f, 0.52f, -5.3826, 105.2589, "Pinang Jaya, depan SD",   "Pinang Jaya",       "Kemiling",                "Sumbatan sampah", ReportStatus.InProgress, HotspotSource.Warga,    "0.6 km", "5 jam lalu"),
    Hotspot(3, RiskLevel.Waspada, 51, 2, 0.68f, 0.40f, -5.3864, 105.3072, "Way Halim Permai blok B", "Way Halim",         "Way Halim",               "Aliran lambat",   ReportStatus.Pending,    HotspotSource.Warga,    "1.1 km", "1 hari lalu"),
    Hotspot(4, RiskLevel.Tinggi,  65, 1, 0.30f, 0.70f, -5.4148, 105.2597, "Jalan Untung Suropati",   "Gunung Sari",       "Tanjung Karang Pusat",    "Drainase rusak",  ReportStatus.Verified,   HotspotSource.Historis, "2.3 km", "Historis"),
    Hotspot(5, RiskLevel.Normal,  30, 0, 0.78f, 0.20f, -5.3650, 105.2950, "Sensor S-Rajabasa-04",    "Rajabasa Raya",     "Rajabasa",                "Pantau IoT",      ReportStatus.Verified,   HotspotSource.Iot,      "3.0 km", "Real-time"),
    Hotspot(6, RiskLevel.Waspada, 48, 0, 0.55f, 0.15f, -5.3750, 105.2780, "Potensi hujan sore",      "Kemiling Permai",   "Kemiling",                "Alert BMKG",      ReportStatus.Pending,    HotspotSource.Cuaca,    "0.9 km", "Prediksi 16:00"),
)

internal val NearbyTeaserSeed: List<NearbyTeaser> = listOf(
    NearbyTeaser(1, "Pinang Jaya, Kemiling", RiskLevel.Tinggi,  72, "0.6 km", 4, "Genangan"),
    NearbyTeaser(2, "Way Halim Permai",      RiskLevel.Waspada, 51, "1.1 km", 2, "Sumbatan"),
    NearbyTeaser(3, "Sukabumi Indah",        RiskLevel.Kritis,  86, "1.8 km", 7, "Genangan"),
)
