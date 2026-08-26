package com.example.alirinmobile.data.repository

import com.example.alirinmobile.data.PhotoRef
import com.example.alirinmobile.data.Report
import com.example.alirinmobile.data.ReportMode
import com.example.alirinmobile.data.ReportStatus
import com.example.alirinmobile.data.RiskLevel
import com.example.alirinmobile.data.StatusMachine
import com.example.alirinmobile.data.categoryToWire
import com.example.alirinmobile.data.reportStatusFromWire
import com.example.alirinmobile.data.scoring.RiskEngine
import com.example.alirinmobile.data.local.ReportDao
import com.example.alirinmobile.data.local.ReportEntity
import com.example.alirinmobile.data.local.StatusHistoryJson
import com.example.alirinmobile.data.local.decodePhotos
import com.example.alirinmobile.data.local.decodeStatusHistoryRaw
import com.example.alirinmobile.data.local.encodePhotos
import com.example.alirinmobile.data.local.encodeRiskBreakdown
import com.example.alirinmobile.data.local.encodeStatusHistoryRaw
import com.example.alirinmobile.data.local.jsonCodec
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
    private val kelurahanRepo: KelurahanRepository,
    private val weatherRepo: WeatherRepository,
) {

    // Batas Kota Bandar Lampung, sama dengan CITY_BOUNDS di web dan constraint
    // reports_coordinate_bounds_check di Supabase.
    private fun isInsideCity(lat: Double?, lng: Double?): Boolean =
        lat != null && lng != null &&
            lat >= -5.62 && lat <= -5.28 && lng >= 105.15 && lng <= 105.36

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

    // Laporan milik perangkat ini: yang dibuat di sini, plus yang pernah dilacak
    // lewat tracking token. Peta dan statistik tetap memakai observeReports().
    fun observeMyReports(): Flow<List<Report>> =
        dao.observeAll().map { list -> list.filter { it.createdLocally }.map { it.toDomain() } }

    data class SubmitResult(val id: String, val code: String, val trackingToken: String)

    // ---- Submission (optimistic, offline-first) ----
    suspend fun submit(form: LaporForm, mode: ReportMode): Result<SubmitResult> = runCatching {
        // Validasi wilayah sebelum apa-apa. Mirror reportsStore.js:134 di web supaya
        // nilai ngawur (mis. ALR-2026-9804 kecamatan="C") tidak sampai ke Supabase.
        require(kelurahanRepo.isValidArea(form.kelurahan, form.kecamatan)) {
            "Wilayah kecamatan dan kelurahan belum valid. Pilih dari daftar."
        }
        require(form.kategori.isNotBlank()) { "Kategori laporan wajib dipilih." }
        require(form.severity.isNotBlank()) { "Tingkat severity wajib dipilih." }
        require(form.lat != null && form.lng != null) { "Lokasi laporan wajib ditandai di peta." }
        require(isInsideCity(form.lat, form.lng)) {
            "Koordinat berada di luar wilayah Kota Bandar Lampung."
        }
        // Aturan per mode, identik dengan validateReportInput di web.
        if (mode == ReportMode.Lengkap) {
            require(form.deskripsi.trim().length >= 10) { "Deskripsi minimal 10 karakter." }
            require(form.photos.isNotEmpty()) { "Minimal 1 foto bukti wajib pada Lapor Lengkap." }
        } else {
            require(form.deskripsi.isBlank() || form.deskripsi.trim().length >= 10) {
                "Deskripsi minimal 10 karakter, atau kosongkan saja."
            }
        }

        val nowIso = ReportCodegen.nowIsoUtc()
        val nowMs = System.currentTimeMillis()
        val prefix = ReportCodegen.yearPrefix()
        val id = ReportCodegen.newId()
        val code = ReportCodegen.newCode(dao.latestCodeForYear(prefix))
        val token = ReportCodegen.newTrackingToken()
        val categoryWire = categoryToWire(form.kategori)
        val severity = form.severity

        // Curah hujan 3 jam BMKG dibekukan di laporan sebagai masukan faktor
        // Cuaca. Gagal ambil -> null, artinya "tidak diketahui", bukan "0 mm".
        val rainfallMm = weatherRepo.rainfallMmFor(
            kelurahanRepo.resolveAdm4(form.kecamatan, form.kelurahan)
        )

        val risk = computeRisk(
            id = id,
            severity = severity,
            lat = form.lat,
            lng = form.lng,
            createdAtMs = nowMs,
            rainfallMm = rainfallMm,
            photoCount = form.photos.size,
            description = form.deskripsi,
        )

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
            riskLevel = risk.level.toWire(),
            riskScore = risk.score,
            description = form.deskripsi,
            address = form.alamat.ifBlank { null },
            lat = form.lat!!,
            lng = form.lng!!,
            kelurahan = form.kelurahan.trim(),
            kecamatan = form.kecamatan.trim(),
            reporterName = form.nama.ifBlank { null },
            reporterContact = form.kontak.ifBlank { null },
            assignedOfficerId = null,
            assignedOfficerName = null,
            blockedReason = null,
            archivedAt = null,
            submissionMode = mode.toWire(),
            rainfallMm = rainfallMm,
            createdAt = nowIso,
            updatedAt = nowIso,
            updatedAtMs = nowMs,
            photosJson = encodePhotos(localPhotos),
            completionPhotosJson = "[]",
            riskBreakdownJson = encodeRiskBreakdown(risk.breakdown),
            statusHistoryJson = encodeStatusHistoryRaw(initialHistory),
            fieldNotesJson = "[]",
            syncStatus = "pending",
            syncAttempts = 0,
            syncLastError = null,
            localOnly = true,
            createdLocally = true,
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
            // (a) Insert baris reports LEBIH DULU, baru unggah foto.
            //
            // Urutan lama membalik keduanya: foto diunggah dulu, dan bila insert
            // gagal, rollback memanggil Storage delete sebagai anon. Policy hapus
            // hanya untuk staff, kegagalannya ditelan runCatching, dan berkasnya
            // tertinggal selamanya tanpa baris yang merujuknya. Dengan urutan ini
            // kegagalan insert tidak pernah meninggalkan berkas yatim.
            //
            // NOTE: supabase-kt default kirim "Prefer: return=representation" yang
            // memicu implicit SELECT dari row baru; RLS reports_staff_select memblok
            // anon → 42501. Kita paksa "return=minimal" via headers builder.
            var attempts = 0
            var rowToInsert = initial
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

            // (b) Riwayat status awal. Ditulis sebelum foto karena policy
            // report_status_history_public_insert mensyaratkan laporan masih
            // berstatus 'masuk'.
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

            // (c) Unggah foto lalu tautkan. Kalau tahap ini gagal, laporannya
            // sudah aman tersimpan; outbox tinggal mengulang unggahannya.
            val uploaded = decodePhotos(rowToInsert.photosJson).map { photo ->
                if (!photo.url.isNullOrBlank()) return@map photo
                val local = photo.localUri?.let(::File)
                if (local == null || !local.exists()) return@map photo
                uploader.upload(local)
            }
            dao.upsert(dao.get(id)!!.copy(photosJson = encodePhotos(uploaded)))

            val photoRows = uploaded.filter { !it.url.isNullOrBlank() }.map { photo ->
                ReportPhotoInsertPayload(
                    reportId = id,
                    url = photo.url!!,
                    name = photo.name,
                    type = photo.type,
                    size = photo.size,
                    kind = "report",
                )
            }
            if (photoRows.isNotEmpty()) {
                supabase.from("report_photos").insert(photoRows) {
                    headers.remove("Prefer")
                    headers.append("Prefer", "return=minimal")
                }
            }

            dao.markSynced(id, s = "synced", localOnly = false)
        } catch (t: Throwable) {
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
            // Tabel mentah tidak membawa tabel anak; relasinya harus diminta
            // eksplisit. Tanpa ini petugas kehilangan foto, rincian skor, dan
            // riwayat status -- view public_reports sudah menyertakan ketiganya.
            supabase.from("reports")
                .select(
                    Columns.raw("*, report_photos(*), risk_breakdowns(*), report_status_history(*)")
                ) { order("created_at", Order.DESCENDING) }
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
                    // Penanda kepemilikan perangkat tidak ada di server; harus
                    // dipertahankan agar layar Status tetap tahu laporan mana
                    // yang dibuat dari sini.
                    createdLocally = existing.createdLocally,
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
        dao.findByToken(token)?.let {
            if (!it.createdLocally) dao.upsert(it.copy(createdLocally = true))
            return it.toDomain()
        }
        val dto = runCatching {
            supabase.postgrest.rpc(
                "get_report_by_tracking_token",
                buildJsonObject { put("p_token", token) },
            ).decodeAs<SupabaseReportDto>()
        }.getOrNull() ?: return null
        // Pemegang token adalah pelapornya, jadi laporan ini masuk daftar
        // "laporan saya" di perangkat ini.
        val entity = dto.toEntity().copy(createdLocally = true)
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
    // Mengembalikan pesan kesalahan bila transisi ditolak, null bila diterima.
    // Pemeriksaan di sini mencegah aksi yang pasti ditolak trigger Supabase
    // sampai ke jaringan, sekaligus menjaga cache Room tetap konsisten.
    fun updateReportStatus(
        reportId: String,
        newStatus: ReportStatus,
        note: String? = null,
        actorLabel: String? = null,
        onError: (String) -> Unit = {},
    ) {
        val nowIso = ReportCodegen.nowIsoUtc()
        val nowMs = System.currentTimeMillis()
        applicationScope.launch {
            val current = dao.get(reportId) ?: return@launch
            val from = reportStatusFromWire(current.status)

            if (!StatusMachine.canTransition(from, newStatus)) {
                onError(StatusMachine.rejectionReason(from, newStatus))
                return@launch
            }
            if (StatusMachine.requiresCompletionPhoto(newStatus) &&
                decodePhotos(current.completionPhotosJson).isEmpty()
            ) {
                onError("Unggah foto bukti penyelesaian sebelum menutup laporan.")
                return@launch
            }

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
                // Arsip mengikuti status, sama seperti web dan trigger Supabase.
                archivedAt = if (StatusMachine.isFinal(newStatus)) {
                    current.archivedAt ?: nowIso
                } else null,
                statusHistoryJson = encodeStatusHistoryRaw(newHistory),
            )
            dao.upsert(updated)
            runCatching {
                supabase.from("reports").update({
                    set("status", newStatus.toWire())
                    set("updated_at", nowIso)
                    if (StatusMachine.isFinal(newStatus)) set("archived_at", updated.archivedAt)
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

    // ---- Petugas verifikasi on-site: upload foto bukti lalu status -> Verified ----
    fun verifyWithPhoto(
        reportId: String,
        photoLocalPath: String,
        note: String? = null,
        actorLabel: String? = null,
    ) {
        val nowIso = ReportCodegen.nowIsoUtc()
        val nowMs = System.currentTimeMillis()
        applicationScope.launch {
            val current = dao.get(reportId) ?: return@launch
            val from = reportStatusFromWire(current.status)
            if (!StatusMachine.canTransition(from, ReportStatus.Verified)) return@launch
            runCatching {
                val ref = uploader.upload(File(photoLocalPath)).copy(kind = "completion")
                val noteFinal = note ?: "Diverifikasi di lokasi dengan foto bukti."
                val newCompletion = decodePhotos(current.completionPhotosJson) + ref
                val newHistory = decodeStatusHistoryRaw(current.statusHistoryJson) + StatusHistoryJson(
                    status = ReportStatus.Verified.toWire(),
                    actor = actorLabel ?: "Petugas",
                    note = noteFinal,
                    at = nowIso,
                )
                dao.upsert(
                    current.copy(
                        status = ReportStatus.Verified.toWire(),
                        completionPhotosJson = encodePhotos(newCompletion),
                        statusHistoryJson = encodeStatusHistoryRaw(newHistory),
                        updatedAt = nowIso,
                        updatedAtMs = nowMs,
                    )
                )
                supabase.from("report_photos").insert(
                    ReportPhotoInsertPayload(
                        reportId = reportId, url = ref.url!!, name = ref.name,
                        type = ref.type, size = ref.size, kind = "completion",
                    )
                )
                // Kolom completion_photos WAJIB ikut diperbarui. Web membaca
                // bukti penyelesaian dari kolom jsonb ini, bukan dari tabel
                // report_photos, sehingga sebelum perbaikan ini foto verifikasi
                // on-site tidak pernah muncul di dashboard admin.
                supabase.from("reports").update({
                    set("status", ReportStatus.Verified.toWire())
                    set("updated_at", nowIso)
                    set("completion_photos", jsonCodec.parseToJsonElement(encodePhotos(newCompletion)))
                }) { filter { eq("id", reportId) } }
                supabase.from("report_status_history").insert(
                    StatusHistoryInsertPayload(
                        reportId = reportId, status = ReportStatus.Verified.toWire(),
                        actor = actorLabel ?: "Petugas", note = noteFinal, at = nowIso,
                    )
                )
            }
        }
    }

    // ---- Helpers ----
    // Skor memakai RiskEngine bersama (bobot Proposal 4.4). Rumus lama di sini
    // hanya membaca severity lalu mengalikannya 0,7 untuk mode Cepat, sehingga
    // laporan banjir kritis yang dikirim cepat justru turun ke kelas Waspada.
    // Mode laporan sekarang tidak memengaruhi skor sama sekali.
    private suspend fun computeRisk(
        id: String,
        severity: String,
        lat: Double,
        lng: Double,
        createdAtMs: Long,
        rainfallMm: Double?,
        photoCount: Int,
        description: String,
    ): RiskEngine.Result {
        val neighbours = runCatching {
            dao.neighbourRows().map {
                RiskEngine.NeighbourReport(
                    id = it.id,
                    lat = it.lat,
                    lng = it.lng,
                    createdAtMs = ReportCodegen.parseIsoMillis(it.createdAt) ?: it.updatedAtMs,
                    status = it.status,
                )
            }
        }.getOrDefault(emptyList())

        return RiskEngine.evaluate(
            id = id,
            severity = severity,
            lat = lat,
            lng = lng,
            createdAtMs = createdAtMs,
            rainfallMm = rainfallMm,
            photoCount = photoCount,
            description = description,
            neighbours = neighbours,
        )
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

