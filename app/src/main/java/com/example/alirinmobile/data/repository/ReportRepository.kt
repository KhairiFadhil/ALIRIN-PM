package com.example.alirinmobile.data.repository

import com.example.alirinmobile.data.Hotspot
import com.example.alirinmobile.data.HotspotSource
import com.example.alirinmobile.data.NearbyTeaser
import com.example.alirinmobile.data.PhotoRef
import com.example.alirinmobile.data.Report
import com.example.alirinmobile.data.ReportMode
import com.example.alirinmobile.data.ReportStatus
import com.example.alirinmobile.data.RiskLevel
import com.example.alirinmobile.data.categoryFromWire
import com.example.alirinmobile.data.categoryLabelOf
import com.example.alirinmobile.data.categoryToWire
import com.example.alirinmobile.data.local.ReportDao
import com.example.alirinmobile.data.local.ReportEntity
import com.example.alirinmobile.data.local.StatusHistoryJson
import com.example.alirinmobile.data.local.decodePhotos
import com.example.alirinmobile.data.local.decodeStatusHistoryRaw
import com.example.alirinmobile.data.local.encodePhotos
import com.example.alirinmobile.data.local.encodeStatusHistoryRaw
import com.example.alirinmobile.data.local.toDomain
import com.example.alirinmobile.data.network.PhotoUploader
import com.example.alirinmobile.data.network.dto.ReportPhotoInsertPayload
import com.example.alirinmobile.data.network.dto.StatusHistoryInsertPayload
import com.example.alirinmobile.data.network.dto.SupabaseReportDto
import com.example.alirinmobile.data.network.dto.TrackingTokenRpcParams
import com.example.alirinmobile.data.network.dto.extractStoragePath
import com.example.alirinmobile.data.network.dto.toEntity
import com.example.alirinmobile.data.network.dto.toInsertPayload
import com.example.alirinmobile.data.toWire
import com.example.alirinmobile.feature.lapor.LaporForm
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

class ReportRepository(
    private val dao: ReportDao,
    private val supabase: SupabaseClient,
    private val uploader: PhotoUploader,
    private val applicationScope: CoroutineScope,
    private val authRepo: AuthRepository,
) {

    // ---- Draft state (in-memory only) ----
    private val _draft = MutableStateFlow<LaporForm?>(null)
    val draft: StateFlow<LaporForm?> = _draft.asStateFlow()
    fun saveDraft(form: LaporForm) { _draft.value = form }
    fun clearDraft() { _draft.value = null }

    // ---- Observation ----
    fun observeReports(): Flow<List<Report>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeReport(id: String): Flow<Report?> =
        dao.observe(id).map { it?.toDomain() }

    data class SubmitResult(val id: String, val code: String, val trackingToken: String)

    // ---- Submission (optimistic, offline-first) ----
    suspend fun submit(form: LaporForm, mode: ReportMode): Result<SubmitResult> = runCatching {
        val nowIso = ReportCodegen.nowIsoUtc()
        val nowMs = System.currentTimeMillis()
        val prefix = ReportCodegen.yearPrefix()
        val id = ReportCodegen.newId()
        val code = ReportCodegen.newCode(dao.latestCodeForYear(prefix))
        val token = ReportCodegen.newTrackingToken()
        val categoryWire = categoryToWire(form.kategori)
        val severity = form.severity.ifBlank { "sedang" }
        val (score, risk) = computeScore(severity, mode)

        val localPhotos = form.photos.mapNotNull { photo ->
            val uri = photo.uri ?: return@mapNotNull null
            val file = File(uri)
            PhotoRef(
                id = UUID.randomUUID().toString(),
                url = null,
                localUri = uri,
                name = if (file.exists()) file.name else "foto-${System.currentTimeMillis()}.jpg",
                type = "image/jpeg",
                size = if (file.exists()) file.length().toInt() else 0,
                kind = "report",
            )
        }

        val initialHistory = listOf(
            StatusHistoryJson(
                status = "masuk",
                actor = "Warga",
                note = "Laporan masuk dari aplikasi mobile.",
                at = nowIso,
            )
        )

        val entity = ReportEntity(
            id = id,
            code = code,
            publicTrackingToken = token,
            category = categoryWire,
            severity = severity,
            status = "masuk",
            riskLevel = risk.toWire(),
            riskScore = score,
            description = form.deskripsi,
            address = form.alamat.ifBlank { null },
            lat = form.lat ?: 0.0,
            lng = form.lng ?: 0.0,
            kelurahan = form.kelurahan.ifBlank { "Tidak diketahui" },
            kecamatan = form.kecamatan.ifBlank { "Bandar Lampung" },
            reporterName = form.nama.ifBlank { null },
            reporterContact = form.kontak.ifBlank { null },
            assignedOfficerId = null,
            assignedOfficerName = null,
            blockedReason = null,
            archivedAt = null,
            submissionMode = mode.toWire(),
            createdAt = nowIso,
            updatedAt = nowIso,
            updatedAtMs = nowMs,
            photosJson = encodePhotos(localPhotos),
            completionPhotosJson = "[]",
            riskBreakdownJson = "[]",
            statusHistoryJson = encodeStatusHistoryRaw(initialHistory),
            fieldNotesJson = "[]",
            syncStatus = "pending",
            syncAttempts = 0,
            syncLastError = null,
            localOnly = true,
        )
        dao.upsert(entity)
        clearDraft()
        applicationScope.launch { pushRemote(id) }
        SubmitResult(id = id, code = code, trackingToken = token)
    }

    // ---- Remote push (fire-and-forget background) ----
    @OptIn(SupabaseExperimental::class)
    private suspend fun pushRemote(id: String) {
        val initial = dao.get(id) ?: return
        dao.markSyncState(id, "syncing", null)
        val uploadedPaths = mutableListOf<String>()
        try {
            // (a) Upload photos yang belum ada url
            val currentPhotos = decodePhotos(initial.photosJson)
            val uploaded = currentPhotos.map { p ->
                if (!p.url.isNullOrBlank()) return@map p
                val local = p.localUri?.let(::File)
                if (local == null || !local.exists()) return@map p
                val pushed = uploader.upload(local)
                extractStoragePath(pushed.url!!)?.let { uploadedPaths += it }
                pushed
            }
            val afterUpload = dao.get(id)!!.copy(photosJson = encodePhotos(uploaded))
            dao.upsert(afterUpload)

            // (b) Insert reports row with 5x retry on unique code collision.
            // NOTE: supabase-kt default kirim "Prefer: return=representation" yang
            // memicu implicit SELECT dari row baru; RLS reports_staff_select memblok
            // anon → 42501. Kita paksa "return=minimal" via headers builder.
            var attempts = 0
            var rowToInsert = afterUpload
            while (true) {
                val err = runCatching {
                    supabase.from("reports").insert(rowToInsert.toInsertPayload()) {
                        headers.remove("Prefer")
                        headers.append("Prefer", "return=minimal")
                    }
                }.exceptionOrNull()
                if (err == null) break
                if (isCodeConflict(err) && ++attempts < 5) {
                    val latest = fetchLatestRemoteCodeForYear(rowToInsert.code.substringBeforeLast('-') + "-")
                    val newCode = ReportCodegen.newCode(latest)
                    rowToInsert = rowToInsert.copy(code = newCode)
                    dao.upsert(rowToInsert)
                    continue
                }
                throw err
            }

            // (c) Insert child rows: photos + status_history (risk_breakdowns kosong dari mobile)
            val photoRows = uploaded.filter { !it.url.isNullOrBlank() }.map { p ->
                ReportPhotoInsertPayload(
                    reportId = id,
                    url = p.url!!,
                    name = p.name,
                    type = p.type,
                    size = p.size,
                    kind = "report",
                )
            }
            if (photoRows.isNotEmpty()) {
                supabase.from("report_photos").insert(photoRows) {
                    headers.remove("Prefer")
                    headers.append("Prefer", "return=minimal")
                }
            }
            supabase.from("report_status_history").insert(
                StatusHistoryInsertPayload(
                    reportId = id,
                    status = "masuk",
                    actor = "Warga",
                    note = "Laporan masuk dari aplikasi mobile.",
                    at = rowToInsert.createdAt,
                ),
            ) {
                headers.remove("Prefer")
                headers.append("Prefer", "return=minimal")
            }

            dao.markSynced(id, s = "synced", localOnly = false)
        } catch (t: Throwable) {
            uploader.deleteMany(uploadedPaths)
            dao.markSyncState(id, "failed", t.message?.take(500))
        }
    }

    // ---- Sync remote → local ----
    // Untuk anon, view public_reports sengaja tidak membocorkan public_tracking_token,
    // reporter_name, reporter_contact (PII/kredensial). Kalau kita upsert mentah,
    // row lokal yang sudah punya token akan kehilangan token-nya. Fix: gabungkan
    // dgn baris Room yang sudah ada, pertahankan field lokal jika DTO kosong.
    suspend fun syncNow(): Result<Int> = runCatching {
        val isStaff = authRepo.isStaff()
        val dtos: List<SupabaseReportDto> = if (isStaff) {
            supabase.from("reports")
                .select { order("created_at", Order.DESCENDING) }
                .decodeList()
        } else {
            supabase.from("public_reports")
                .select { order("created_at", Order.DESCENDING) }
                .decodeList()
        }
        val localOnlyIds = dao.localOnlyIds().toSet()
        val merged = dtos
            .filter { it.id !in localOnlyIds }
            .map { dto ->
                val fresh = dto.toEntity()
                val existing = dao.get(dto.id)
                if (existing == null) fresh
                else fresh.copy(
                    publicTrackingToken = fresh.publicTrackingToken ?: existing.publicTrackingToken,
                    reporterName = fresh.reporterName ?: existing.reporterName,
                    reporterContact = fresh.reporterContact ?: existing.reporterContact,
                    // Preserve outbox metadata (should be synced/localOnly=false since not in localOnly filter)
                    syncStatus = existing.syncStatus.takeIf { it != "pending" && it != "syncing" && it != "failed" } ?: "synced",
                    syncAttempts = existing.syncAttempts,
                    syncLastError = null,
                    localOnly = false,
                )
            }
        dao.upsertAll(merged)
        merged.size
    }

    // ---- Outbox drain ----
    suspend fun retryPending(): Int {
        val outbox = dao.pendingOutbox()
        outbox.forEach { pushRemote(it.id) }
        return outbox.size
    }

    // ---- Track by tracking token (Room first, fallback RPC) ----
    suspend fun trackByToken(token: String): Report? {
        dao.findByToken(token)?.let { return it.toDomain() }
        val dto = runCatching {
            supabase.postgrest.rpc(
                "get_report_by_tracking_token",
                buildJsonObject { put("p_token", token) },
            ).decodeAs<SupabaseReportDto>()
        }.getOrNull() ?: return null
        val entity = dto.toEntity()
        dao.upsert(entity)
        return entity.toDomain()
    }

    // ---- Realtime signals ----
    @OptIn(FlowPreview::class)
    fun subscribeRealtime(): Flow<Unit> = callbackFlow {
        val channel = supabase.channel("public:reports")
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "reports"
        }
        channel.subscribe()
        val job = applicationScope.launch {
            changes.debounce(3.seconds).collect { trySend(Unit) }
        }
        awaitClose {
            job.cancel()
            applicationScope.launch { supabase.realtime.removeChannel(channel) }
        }
    }

    // ---- Staff status transitions ----
    fun updateReportStatus(
        reportId: String,
        newStatus: ReportStatus,
        note: String? = null,
        actorLabel: String? = null,
    ) {
        val nowIso = ReportCodegen.nowIsoUtc()
        val nowMs = System.currentTimeMillis()
        applicationScope.launch {
            val current = dao.get(reportId) ?: return@launch
            val newEntry = StatusHistoryJson(
                status = newStatus.toWire(),
                actor = actorLabel ?: "Petugas",
                note = note,
                at = nowIso,
            )
            val newHistory = decodeStatusHistoryRaw(current.statusHistoryJson) + newEntry
            val updated = current.copy(
                status = newStatus.toWire(),
                updatedAt = nowIso,
                updatedAtMs = nowMs,
                statusHistoryJson = encodeStatusHistoryRaw(newHistory),
            )
            dao.upsert(updated)
            runCatching {
                supabase.from("reports").update({
                    set("status", newStatus.toWire())
                    set("updated_at", nowIso)
                }) {
                    filter { eq("id", reportId) }
                }
                supabase.from("report_status_history").insert(
                    StatusHistoryInsertPayload(
                        reportId = reportId,
                        status = newStatus.toWire(),
                        actor = actorLabel ?: "Petugas",
                        note = note,
                        at = nowIso,
                    )
                )
            }
        }
    }

    // ---- Demo/preview data (in-memory, UI-only) ----
    fun hotspots(): List<Hotspot> = HotspotSeed
    fun nearbyTeasers(): List<NearbyTeaser> = NearbyTeaserSeed

    // ---- Helpers ----
    private fun computeScore(severity: String, mode: ReportMode): Pair<Int, RiskLevel> {
        val weight = when (severity) {
            "ringan" -> 20; "sedang" -> 45; "parah" -> 65; "kritis" -> 85
            else -> 30
        }
        val multiplier = if (mode == ReportMode.Lengkap) 1.0 else 0.7
        val score = (weight * multiplier).toInt().coerceIn(0, 100)
        val risk = when {
            score >= 80 -> RiskLevel.Kritis
            score >= 60 -> RiskLevel.Tinggi
            score >= 40 -> RiskLevel.Waspada
            else -> RiskLevel.Normal
        }
        return score to risk
    }

    private fun isCodeConflict(t: Throwable): Boolean {
        val msg = (t.message ?: "").lowercase()
        return msg.contains("23505") || msg.contains("reports_code_key") ||
            msg.contains("duplicate key") || msg.contains("unique constraint")
    }

    @kotlinx.serialization.Serializable
    private data class CodeOnly(val code: String)

    private suspend fun fetchLatestRemoteCodeForYear(prefix: String): String? = runCatching {
        supabase.from("reports")
            .select(columns = Columns.list("code")) {
                filter { like("code", "$prefix%") }
                order("code", Order.DESCENDING)
                limit(1)
            }
            .decodeList<CodeOnly>()
            .firstOrNull()?.code
    }.getOrNull()

    // For UI display only — do NOT use for persistence.
    fun nowLabel(): String =
        SimpleDateFormat("d MMM · HH:mm", Locale("id")).format(Date())
}

// ---- Demo data used by BerandaScreen, PersetujuanDetailScreen, PetaScreen ----
// Kept as internal top-level vals so existing UI imports keep working.
// These are in-memory fixtures unrelated to Supabase sync.

internal val ReportSeed: List<Report> = listOf(
    Report(
        id = "r1", code = "ALR-2026-04217",
        publicTrackingToken = "trk_demo_r1",
        mode = ReportMode.Lengkap, status = ReportStatus.InProgress,
        risk = RiskLevel.Tinggi, score = 72,
        categoryId = "sumbatan", category = categoryLabelOf("sumbatan"),
        severity = "parah",
        kelurahan = "Pinang Jaya", kecamatan = "Kemiling",
        address = "Jl. Imam Bonjol depan SD 2",
        createdAt = "2026-05-12T14:30:00Z", updatedAt = "2026-05-13T09:15:00Z",
        description = "Sampah plastik & daun menyumbat got, air mulai menggenang ke jalan.",
        lat = -5.3826, lng = 105.2589,
    ),
    Report(
        id = "r2", code = "ALR-2026-04201",
        publicTrackingToken = "trk_demo_r2",
        mode = ReportMode.Cepat, status = ReportStatus.Verified,
        risk = RiskLevel.Waspada, score = 48,
        categoryId = "lambat", category = categoryLabelOf("lambat"),
        severity = "sedang",
        kelurahan = "Way Halim Permai", kecamatan = "Way Halim",
        createdAt = "2026-05-11T18:42:00Z",
        lat = -5.3864, lng = 105.3072,
    ),
    Report(
        id = "r3", code = "ALR-2026-04088",
        publicTrackingToken = "trk_demo_r3",
        mode = ReportMode.Lengkap, status = ReportStatus.Completed,
        risk = RiskLevel.Normal, score = 22,
        categoryId = "rusak", category = categoryLabelOf("rusak"),
        severity = "ringan",
        kelurahan = "Kemiling Permai", kecamatan = "Kemiling",
        createdAt = "2026-05-05T09:14:00Z",
        lat = -5.3750, lng = 105.2780,
        description = "Tutup got pecah, anak2 hampir jatuh.",
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
