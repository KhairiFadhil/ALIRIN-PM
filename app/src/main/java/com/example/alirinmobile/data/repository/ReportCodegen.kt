package com.example.alirinmobile.data.repository

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

// Port dari C:\ALIRIN\app\src\domain\reports.js — semua bentuk id/kode/token
// mengikuti web supaya row insert dari mobile lolos check constraint & unique
// index yang sudah ada, dan tracking token bisa dipakai lintas platform.
object ReportCodegen {

    private val jakartaTz: TimeZone = TimeZone.getTimeZone("Asia/Jakarta")

    fun newId(): String = UUID.randomUUID().toString()

    fun newTrackingToken(): String =
        "trk_" + UUID.randomUUID().toString().replace("-", "").take(32)

    fun newCode(latestForYear: String?, at: Date = Date()): String {
        val prefix = yearPrefix(at)
        val nextSeq = latestForYear?.removePrefix(prefix)?.toIntOrNull()?.plus(1) ?: 1
        return prefix + nextSeq.toString().padStart(4, '0')
    }

    fun yearPrefix(at: Date = Date()): String {
        val cal = Calendar.getInstance(jakartaTz).apply { time = at }
        return "ALR-${cal.get(Calendar.YEAR)}-"
    }

    // Kebalikan nowIsoUtc. Dipakai RiskEngine untuk jendela histori 180 hari.
    fun parseIsoMillis(raw: String?): Long? {
        if (raw.isNullOrBlank()) return null
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
        )
        for (pattern in patterns) {
            val fmt = SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            runCatching { fmt.parse(raw) }.getOrNull()?.let { return it.time }
        }
        return null
    }

    // ISO8601 UTC ("Z" suffix), same shape as JS `new Date().toISOString()`
    fun nowIsoUtc(at: Date = Date()): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return fmt.format(at)
    }
}
