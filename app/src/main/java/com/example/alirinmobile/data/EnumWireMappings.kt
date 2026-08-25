package com.example.alirinmobile.data

// Konversi antara enum type-safe di UI (yang membawa Color badge dari theme)
// dan wire-value string yang persis dipakai Supabase schema + web reportsSupabaseRepository.js.
// Simpan wire-value verbatim di Room supaya sync tidak lossy.

fun ReportStatus.toWire(): String = when (this) {
    ReportStatus.Pending -> "masuk"
    ReportStatus.Verified -> "diverifikasi"
    ReportStatus.Scheduled -> "dijadwalkan"
    ReportStatus.InProgress -> "ditangani"
    ReportStatus.Completed -> "selesai"
    ReportStatus.Rejected -> "ditolak"
}

fun reportStatusFromWire(s: String?): ReportStatus = when (s?.lowercase()) {
    "masuk" -> ReportStatus.Pending
    "diverifikasi" -> ReportStatus.Verified
    "dijadwalkan" -> ReportStatus.Scheduled
    "ditangani" -> ReportStatus.InProgress
    "selesai" -> ReportStatus.Completed
    "ditolak" -> ReportStatus.Rejected
    else -> ReportStatus.Pending
}

// Kapital di awal. Web menulis 'Normal', mobile dulu menulis 'normal', sehingga
// satu kolom berisi tiga gaya penulisan. Constraint reports_risk_level_check
// sekarang hanya menerima Normal/Waspada/Tinggi/Kritis.
fun RiskLevel.toWire(): String = name

fun riskLevelFromWire(s: String?): RiskLevel =
    RiskLevel.entries.firstOrNull { it.name.equals(s, ignoreCase = true) } ?: RiskLevel.Normal

// Kategori ID di mobile (dipakai Kategoris list di Models.kt) beda dengan wire-value di Supabase.
// Web: sumbatan/genangan/aliran-lambat/drainase-rusak/bau/lainnya
// Mobile lama: sumbatan/genangan/lambat/rusak/bau/lain
private val CategoryMobileToWeb = mapOf(
    "sumbatan" to "sumbatan",
    "genangan" to "genangan",
    "lambat" to "aliran-lambat",
    "rusak" to "drainase-rusak",
    "bau" to "bau",
    "lain" to "lainnya",
)
private val CategoryWebToMobile = CategoryMobileToWeb.entries.associate { it.value to it.key }

fun categoryToWire(mobileId: String): String = CategoryMobileToWeb[mobileId] ?: "lainnya"

fun categoryFromWire(webId: String?): String = CategoryWebToMobile[webId?.lowercase()] ?: "lain"

fun categoryLabelOf(mobileId: String): String =
    Kategoris.find { it.id == mobileId }?.label ?: "Drainase"

fun ReportMode.toWire(): String = when (this) {
    ReportMode.Cepat -> "Cepat"
    ReportMode.Lengkap -> "Lengkap"
}

fun reportModeFromWire(s: String?): ReportMode = when (s) {
    "Lengkap" -> ReportMode.Lengkap
    else -> ReportMode.Cepat
}
