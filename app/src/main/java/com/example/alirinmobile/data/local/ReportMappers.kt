package com.example.alirinmobile.data.local

import com.example.alirinmobile.data.FieldNote
import com.example.alirinmobile.data.HistoryEntry
import com.example.alirinmobile.data.PhotoRef
import com.example.alirinmobile.data.Report
import com.example.alirinmobile.data.RiskBreakdownItem
import com.example.alirinmobile.data.SyncStatus
import com.example.alirinmobile.data.categoryFromWire
import com.example.alirinmobile.data.categoryLabelOf
import com.example.alirinmobile.data.reportModeFromWire
import com.example.alirinmobile.data.reportStatusFromWire
import com.example.alirinmobile.data.riskLevelFromWire
import com.example.alirinmobile.data.toWire
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

// DTO internal untuk serialisasi JSON di kolom Room. Bentuknya sengaja identik
// dengan payload yang dikirim ke Supabase table report_photos/risk_breakdowns/
// report_status_history (snake_case bila perlu) supaya konversi mapper cepat.

@Serializable
data class PhotoRefJson(
    val id: String,
    val url: String? = null,
    val localUri: String? = null,
    val name: String = "foto.jpg",
    val type: String = "image/jpeg",
    val size: Int = 0,
    val kind: String = "report",
)

@Serializable
data class RiskBreakdownJson(
    val id: String,
    // Kunci faktor ada di kolom factor. Kolom id bertipe uuid di project live,
    // jadi tidak bisa dipakai mencocokkan 'severity', 'weather', dan seterusnya.
    // View publik sudah memancarkan factor sebagai id; pembacaan tabel mentah
    // oleh staff tidak, sehingga keduanya diseragamkan lewat factorKey().
    val factor: String? = null,
    val label: String,
    val points: Int = 0,
    val weight: Int = 0,
    val detail: String? = null,
) {
    fun factorKey(): String = factor?.takeIf { it.isNotBlank() } ?: id
}

@Serializable
data class FieldNoteJson(
    val at: String,
    val actor: String,
    val note: String,
)

@Serializable
data class StatusHistoryJson(
    val status: String,
    val actor: String = "Sistem",
    val note: String? = null,
    val at: String,
)

val jsonCodec: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    explicitNulls = false
}

private val photoSerializer = ListSerializer(PhotoRefJson.serializer())
private val breakdownSerializer = ListSerializer(RiskBreakdownJson.serializer())
private val fieldNoteSerializer = ListSerializer(FieldNoteJson.serializer())
private val historySerializer = ListSerializer(StatusHistoryJson.serializer())

fun encodePhotos(photos: List<PhotoRef>): String =
    jsonCodec.encodeToString(photoSerializer, photos.map {
        PhotoRefJson(it.id, it.url, it.localUri, it.name, it.type, it.size, it.kind)
    })

fun decodePhotos(raw: String?): List<PhotoRef> = runCatching {
    if (raw.isNullOrBlank()) return emptyList()
    jsonCodec.decodeFromString(photoSerializer, raw).map {
        PhotoRef(it.id, it.url, it.localUri, it.name, it.type, it.size, it.kind)
    }
}.getOrDefault(emptyList())

fun encodeRiskBreakdown(items: List<RiskBreakdownItem>): String =
    jsonCodec.encodeToString(breakdownSerializer, items.map {
        RiskBreakdownJson(id = it.id, factor = it.id, label = it.label, points = it.points, weight = it.weight, detail = it.detail)
    })

fun decodeRiskBreakdown(raw: String?): List<RiskBreakdownItem> = runCatching {
    if (raw.isNullOrBlank()) return emptyList()
    jsonCodec.decodeFromString(breakdownSerializer, raw).map {
        RiskBreakdownItem(it.factorKey(), it.label, it.points, it.weight, it.detail)
    }
}.getOrDefault(emptyList())

fun encodeFieldNotes(items: List<FieldNote>): String =
    jsonCodec.encodeToString(fieldNoteSerializer, items.map {
        FieldNoteJson(it.at, it.actor, it.note)
    })

fun decodeFieldNotes(raw: String?): List<FieldNote> = runCatching {
    if (raw.isNullOrBlank()) return emptyList()
    jsonCodec.decodeFromString(fieldNoteSerializer, raw).map {
        FieldNote(it.at, it.actor, it.note)
    }
}.getOrDefault(emptyList())

fun encodeStatusHistory(items: List<HistoryEntry>): String {
    val mapped = items.map {
        StatusHistoryJson(
            status = it.status.toWire(),
            actor = "Sistem",
            note = it.note,
            at = it.when_,
        )
    }
    return jsonCodec.encodeToString(historySerializer, mapped)
}

fun encodeStatusHistoryRaw(items: List<StatusHistoryJson>): String =
    jsonCodec.encodeToString(historySerializer, items)

fun decodeStatusHistory(raw: String?): List<HistoryEntry> = runCatching {
    if (raw.isNullOrBlank()) return emptyList()
    jsonCodec.decodeFromString(historySerializer, raw).map {
        HistoryEntry(
            status = reportStatusFromWire(it.status),
            when_ = it.at,
            note = it.note,
            live = false,
        )
    }
}.getOrDefault(emptyList())

fun decodeStatusHistoryRaw(raw: String?): List<StatusHistoryJson> = runCatching {
    if (raw.isNullOrBlank()) return emptyList()
    jsonCodec.decodeFromString(historySerializer, raw)
}.getOrDefault(emptyList())

// ---- Entity ↔ Domain ----

fun ReportEntity.toDomain(): Report {
    val mobileCategoryId = categoryFromWire(category)
    return Report(
        id = id,
        code = code,
        publicTrackingToken = publicTrackingToken.orEmpty(),
        mode = reportModeFromWire(submissionMode),
        status = reportStatusFromWire(status),
        risk = riskLevelFromWire(riskLevel),
        score = riskScore,
        categoryId = mobileCategoryId,
        category = categoryLabelOf(mobileCategoryId),
        severity = severity,
        kelurahan = kelurahan,
        kecamatan = kecamatan,
        address = address,
        createdAt = createdAt,
        updatedAt = updatedAt,
        description = description,
        reporterName = reporterName,
        reporterContact = reporterContact,
        assignedOfficerId = assignedOfficerId,
        assignedOfficerName = assignedOfficerName,
        blockedReason = blockedReason,
        archivedAt = archivedAt,
        photos = decodePhotos(photosJson),
        completionPhotos = decodePhotos(completionPhotosJson),
        riskBreakdown = decodeRiskBreakdown(riskBreakdownJson),
        fieldNotes = decodeFieldNotes(fieldNotesJson),
        history = decodeStatusHistory(statusHistoryJson),
        lat = lat,
        lng = lng,
        syncStatus = when (syncStatus) {
            "pending" -> SyncStatus.Pending
            "syncing" -> SyncStatus.Syncing
            "failed" -> SyncStatus.Failed
            else -> SyncStatus.Synced
        },
    )
}
