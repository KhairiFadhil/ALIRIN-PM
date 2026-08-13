package com.example.alirinmobile.data

import androidx.compose.ui.graphics.Color
import com.example.alirinmobile.ui.theme.*

enum class RiskLevel(val label: String, val bg: Color, val ink: Color, val dot: Color) {
    Normal ("Normal",  RiskNormalBg,  RiskNormalInk,  RiskNormalDot),
    Waspada("Waspada", RiskWaspadaBg, RiskWaspadaInk, RiskWaspadaDot),
    Tinggi ("Tinggi",  RiskTinggiBg,  RiskTinggiInk,  RiskTinggiDot),
    Kritis ("Kritis",  RiskKritisBg,  RiskKritisInk,  RiskKritisDot),
}

enum class ReportStatus(val label: String, val bg: Color, val ink: Color, val dot: Color) {
    Pending     ("Menunggu Verifikasi", StatusPendingBg,    StatusPendingInk,    StatusPendingDot),
    Verified    ("Sudah Diverifikasi",  StatusVerifiedBg,   StatusVerifiedInk,   StatusVerifiedDot),
    Scheduled   ("Dijadwalkan",         StatusScheduledBg,  StatusScheduledInk,  StatusScheduledDot),
    InProgress  ("Sedang Ditangani",    StatusInProgressBg, StatusInProgressInk, StatusInProgressDot),
    Completed   ("Selesai",             StatusCompletedBg,  StatusCompletedInk,  StatusCompletedDot),
    Rejected    ("Ditolak",             StatusRejectedBg,   StatusRejectedInk,   StatusRejectedDot),
}

enum class ReportMode { Cepat, Lengkap }

data class StatusStep(val status: ReportStatus, val iconName: String)
val StatusSteps = listOf(
    StatusStep(ReportStatus.Pending,    "clock"),
    StatusStep(ReportStatus.Verified,   "check"),
    StatusStep(ReportStatus.Scheduled,  "document"),
    StatusStep(ReportStatus.InProgress, "bolt"),
    StatusStep(ReportStatus.Completed,  "shield"),
)
fun ReportStatus.timelineIndex(): Int = StatusSteps.indexOfFirst { it.status == this }

data class Kategori(val id: String, val label: String, val iconName: String, val desc: String)

val Kategoris = listOf(
    Kategori("sumbatan", "Sumbatan sampah", "trash",    "Saluran tersumbat sampah/daun"),
    Kategori("genangan", "Genangan jalan",  "droplet",  "Air menggenang di jalan"),
    Kategori("lambat",   "Aliran lambat",   "history",  "Air mengalir tidak lancar"),
    Kategori("rusak",    "Drainase rusak",  "bolt",     "Got jebol, pecah, atau tutup hilang"),
    Kategori("bau",      "Bau tidak sedap", "sparkles", "Aroma got/sampah menyengat"),
    Kategori("lain",     "Lainnya",         "document", "Masalah lain di drainase"),
)

data class Severity(val id: String, val label: String, val desc: String, val bg: Color, val ink: Color)

val Severities = listOf(
    Severity("ringan", "Ringan", "Belum mengganggu",                 SeverityRinganBg, SeverityRinganInk),
    Severity("sedang", "Sedang", "Mulai mengganggu lalu lintas",     SeveritySedangBg, SeveritySedangInk),
    Severity("parah",  "Parah",  "Sulit dilewati",                   SeverityParahBg,  SeverityParahInk),
    Severity("kritis", "Kritis", "Banjir / tidak bisa dilewati",     SeverityKritisBg, SeverityKritisInk),
)

data class Photo(val id: Long, val kind: PhotoKind, val ts: Long, val uri: String? = null)
enum class PhotoKind { Camera, Gallery }

data class HistoryEntry(
    val status: ReportStatus,
    val when_: String,
    val note: String? = null,
    val live: Boolean = false,
)

enum class SyncStatus { Pending, Syncing, Synced, Failed }

data class PhotoRef(
    val id: String,
    val url: String?,
    val localUri: String? = null,
    val name: String,
    val type: String = "image/jpeg",
    val size: Int = 0,
    val kind: String = "report",
)

data class RiskBreakdownItem(
    val id: String,
    val label: String,
    val points: Int,
    val weight: Int,
    val detail: String? = null,
)

data class FieldNote(
    val at: String,
    val actor: String,
    val note: String,
)

data class Report(
    val id: String,
    val code: String,
    val publicTrackingToken: String = "",
    val mode: ReportMode,
    val status: ReportStatus,
    val risk: RiskLevel,
    val score: Int,
    val categoryId: String,
    val category: String,
    val severity: String = "",
    val kelurahan: String,
    val kecamatan: String,
    val address: String? = null,
    val createdAt: String,
    val updatedAt: String? = null,
    val description: String = "",
    val reporterName: String? = null,
    val reporterContact: String? = null,
    val assignedOfficerId: String? = null,
    val assignedOfficerName: String? = null,
    val blockedReason: String? = null,
    val archivedAt: String? = null,
    val photos: List<PhotoRef> = emptyList(),
    val completionPhotos: List<PhotoRef> = emptyList(),
    val riskBreakdown: List<RiskBreakdownItem> = emptyList(),
    val fieldNotes: List<FieldNote> = emptyList(),
    val history: List<HistoryEntry> = emptyList(),
    val lat: Double? = null,
    val lng: Double? = null,
    val syncStatus: SyncStatus = SyncStatus.Synced,
)

enum class HotspotSource(val label: String) {
    Warga("Laporan warga"), Historis("Data historis"), Cuaca("Alert BMKG"), Iot("Sensor IoT")
}

data class Hotspot(
    val id: Int,
    val risk: RiskLevel,
    val score: Int,
    val count: Int,

    val xFrac: Float,

    val yFrac: Float,

    val lat: Double,
    val lng: Double,
    val name: String,
    val kel: String,
    val kec: String,
    val cat: String,
    val status: ReportStatus,
    val src: HotspotSource,
    val dist: String,
    val when_: String,
)

data class NearbyTeaser(
    val id: Int,
    val label: String,
    val risk: RiskLevel,
    val score: Int,
    val dist: String,
    val count: Int,
    val kind: String,
)
