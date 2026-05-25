package com.example.alirinmobile.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.alirinmobile.data.db.AppDatabase
import com.example.alirinmobile.data.db.DraftEntity
import com.example.alirinmobile.data.db.PendingSubmissionEntity
import com.example.alirinmobile.data.db.toDomain
import com.example.alirinmobile.data.db.toEntity
import com.example.alirinmobile.data.db.toForm
import com.example.alirinmobile.feature.lapor.LaporForm
import com.example.alirinmobile.work.SubmitReportWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

/**
 * Single source of truth for citizen-side data. Room is canonical; remote sync (Supabase)
 * is delegated to SubmitReportWorker (a stub for now — replace with real Supabase calls).
 */
class ReportRepository(private val appContext: Context) {
    private val db = AppDatabase.get(appContext)
    private val reportDao = db.reportDao()
    private val draftDao = db.draftDao()
    private val pendingDao = db.pendingDao()

    private val codeSeq = AtomicInteger(4218)

    // ── Observations ─────────────────────────────────────────────
    fun observeReports(): Flow<List<Report>> =
        reportDao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeReport(id: String): Flow<Report?> =
        reportDao.observeById(id).map { it?.toDomain() }

    suspend fun getReport(id: String): Report? = reportDao.getById(id)?.toDomain()

    fun observeDraft(): Flow<LaporForm?> =
        draftDao.observe().map { it?.toForm() }

    fun observePendingCount(): Flow<Int> = pendingDao.observeCount()

    // ── Mutations ────────────────────────────────────────────────
    suspend fun saveDraft(form: LaporForm, mode: ReportMode?) {
        draftDao.upsert(
            DraftEntity(
                kategori = form.kategori, severity = form.severity,
                deskripsi = form.deskripsi, alamat = form.alamat,
                photos = form.photos, nama = form.nama, kontak = form.kontak,
                mode = mode?.name,
            )
        )
    }

    suspend fun clearDraft() = draftDao.clear()

    /**
     * Staff action: transition a report to a new status with an optional admin note.
     * Appends a HistoryEntry and bumps updatedAt.
     */
    suspend fun updateReportStatus(
        reportId: String,
        newStatus: ReportStatus,
        note: String? = null,
        actorLabel: String? = null,
    ) {
        val current = reportDao.getById(reportId)?.toDomain() ?: return
        val now = nowLabel()
        val combinedNote = listOfNotNull(actorLabel?.let { "[$it]" }, note?.takeIf { it.isNotBlank() })
            .joinToString(" ")
            .takeIf { it.isNotBlank() }
        val updated = current.copy(
            status = newStatus,
            updatedAt = now,
            history = current.history + HistoryEntry(newStatus, now, combinedNote),
        )
        reportDao.upsert(updated.toEntity())
    }

    /**
     * Persist a fresh report locally and enqueue a sync worker. Returns the new report id
     * and code.
     */
    suspend fun submitReport(form: LaporForm, mode: ReportMode): SubmitResult {
        val id = "r_" + UUID.randomUUID().toString().take(8)
        val n = codeSeq.getAndIncrement()
        val code = "ALR-2026-0$n"
        val now = nowLabel()

        // Risk score: simple heuristic based on severity + mode multiplier
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
            kelurahan = "Pinang Jaya", kecamatan = "Kemiling",
            address = form.alamat.takeIf { it.isNotBlank() },
            createdAt = now,
            updatedAt = now,
            description = form.deskripsi,
            photos = form.photos.size,
            history = listOf(
                HistoryEntry(ReportStatus.Pending, now, "Laporan masuk dari aplikasi."),
            ),
        )
        reportDao.upsert(report.toEntity())
        clearDraft()

        // Queue remote sync (no-op for now; real Supabase call lives in the worker)
        val pendingId = pendingDao.insert(PendingSubmissionEntity(reportId = id))
        enqueueSubmitWorker()
        return SubmitResult(id = id, code = code, pendingId = pendingId)
    }

    private fun enqueueSubmitWorker() {
        val req = OneTimeWorkRequestBuilder<SubmitReportWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(appContext).enqueue(req)
    }

    // ── Seeding ──────────────────────────────────────────────────
    suspend fun seedIfEmpty() {
        if (reportDao.count() > 0) return
        reportDao.upsertAll(SampleData.reports.map { it.toEntity() })
    }

    private fun nowLabel(): String {
        val fmt = SimpleDateFormat("d MMM · HH:mm", Locale("id"))
        return fmt.format(Date())
    }

    data class SubmitResult(val id: String, val code: String, val pendingId: Long)
}
