package com.example.alirinmobile.data.network.dto

import com.example.alirinmobile.data.local.FieldNoteJson
import com.example.alirinmobile.data.local.PhotoRefJson
import com.example.alirinmobile.data.local.ReportEntity
import com.example.alirinmobile.data.local.RiskBreakdownJson
import com.example.alirinmobile.data.local.StatusHistoryJson
import com.example.alirinmobile.data.local.decodeStatusHistoryRaw
import com.example.alirinmobile.data.local.jsonCodec
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

// Parse a Supabase timestamptz (e.g. "2026-08-13T05:12:34.567Z" or "2026-08-13T05:12:34+00:00")
// to epoch millis. Uses SimpleDateFormat so it works on API 24+ without java.time desugaring.
private fun parseIsoMillis(raw: String?): Long? {
    if (raw.isNullOrBlank()) return null
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
    )
    for (p in patterns) {
        val fmt = java.text.SimpleDateFormat(p, java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
        val d = runCatching { fmt.parse(raw) }.getOrNull()
        if (d != null) return d.time
    }
    return null
}

private val photoJsonSerializer = ListSerializer(PhotoRefJson.serializer())
private val breakdownJsonSerializer = ListSerializer(RiskBreakdownJson.serializer())
private val fieldNoteJsonSerializer = ListSerializer(FieldNoteJson.serializer())
private val historyJsonSerializer = ListSerializer(StatusHistoryJson.serializer())

private fun JsonElement?.arrayOrEmpty(): JsonArray = when (this) {
    null, JsonNull -> JsonArray(emptyList())
    is JsonArray -> this
    else -> JsonArray(emptyList())
}

private fun decodePhotoListRaw(element: JsonElement?): List<PhotoRefJson> = runCatching {
    val arr = element.arrayOrEmpty()
    if (arr.isEmpty()) emptyList() else jsonCodec.decodeFromJsonElement(photoJsonSerializer, arr)
}.getOrDefault(emptyList())

private val stringListSerializer = ListSerializer(String.serializer())

private fun decodeStringListRaw(element: JsonElement?): List<String> = runCatching {
    val arr = element as? JsonArray ?: return emptyList()
    if (arr.isEmpty()) emptyList() else jsonCodec.decodeFromJsonElement(stringListSerializer, arr)
}.getOrDefault(emptyList())

private fun decodeBreakdownListRaw(element: JsonElement?): List<RiskBreakdownJson> = runCatching {
    val arr = element.arrayOrEmpty()
    if (arr.isEmpty()) emptyList() else jsonCodec.decodeFromJsonElement(breakdownJsonSerializer, arr)
}.getOrDefault(emptyList())

private fun decodeFieldNoteListRaw(element: JsonElement?): List<FieldNoteJson> = runCatching {
    val arr = element.arrayOrEmpty()
    if (arr.isEmpty()) emptyList() else jsonCodec.decodeFromJsonElement(fieldNoteJsonSerializer, arr)
}.getOrDefault(emptyList())

private fun decodeHistoryListRaw(element: JsonElement?): List<StatusHistoryJson> = runCatching {
    val arr = element.arrayOrEmpty()
    if (arr.isEmpty()) emptyList() else jsonCodec.decodeFromJsonElement(historyJsonSerializer, arr)
}.getOrDefault(emptyList())

// ---- DTO (dari Supabase) → Room Entity ----

fun SupabaseReportDto.toEntity(
    existingSyncStatus: String = "synced",
    existingSyncAttempts: Int = 0,
    existingSyncError: String? = null,
    localOnly: Boolean = false,
): ReportEntity {
    val photosJson = jsonCodec.encodeToString(photoJsonSerializer, decodePhotoListRaw(reportPhotos))
    val completionPhotosJson = jsonCodec.encodeToString(
        photoJsonSerializer,
        decodePhotoListRaw(completionPhotos),
    )
    val breakdownJson = jsonCodec.encodeToString(
        breakdownJsonSerializer,
        decodeBreakdownListRaw(riskBreakdowns),
    )
    val fieldNotesJson = jsonCodec.encodeToString(
        fieldNoteJsonSerializer,
        decodeFieldNoteListRaw(fieldNotes),
    )
    val historyJson = jsonCodec.encodeToString(
        historyJsonSerializer,
        decodeHistoryListRaw(reportStatusHistory),
    )

    val updatedAtMs = parseIsoMillis(updatedAt)
        ?: parseIsoMillis(createdAt)
        ?: System.currentTimeMillis()

    return ReportEntity(
        id = id,
        code = code,
        publicTrackingToken = publicTrackingToken?.takeIf { it.isNotBlank() },
        category = category,
        severity = severity,
        status = status,
        riskLevel = riskLevel,
        riskScore = riskScore,
        description = description,
        address = address,
        lat = lat,
        lng = lng,
        kelurahan = kelurahan,
        kecamatan = kecamatan,
        reporterName = reporterName,
        reporterContact = reporterContact,
        assignedOfficerId = assignedOfficerId,
        assignedOfficerName = assignedOfficerName,
        blockedReason = blockedReason,
        archivedAt = archivedAt,
        submissionMode = submissionMode,
        rainfallMm = rainfallMm,
        createdAt = createdAt,
        updatedAt = updatedAt,
        updatedAtMs = updatedAtMs,
        photosJson = photosJson,
        completionPhotosJson = completionPhotosJson,
        riskBreakdownJson = breakdownJson,
        aiRiskScore = aiRiskScore,
        aiRiskReason = aiRiskReason,
        aiRecommendationsJson = jsonCodec.encodeToString(
            stringListSerializer,
            decodeStringListRaw(aiRecommendations),
        ),
        aiModel = aiModel,
        statusHistoryJson = historyJson,
        fieldNotesJson = fieldNotesJson,
        syncStatus = existingSyncStatus,
        syncAttempts = existingSyncAttempts,
        syncLastError = existingSyncError,
        localOnly = localOnly,
    )
}

// ---- Room Entity → Insert Payload untuk POST ke tabel reports ----

fun ReportEntity.toInsertPayload(): ReportInsertPayload {
    val fieldNotesEl = runCatching { jsonCodec.parseToJsonElement(fieldNotesJson) }.getOrNull()
    val completionEl = runCatching { jsonCodec.parseToJsonElement(completionPhotosJson) }.getOrNull()
    val token = publicTrackingToken
        ?: error("public_tracking_token wajib untuk insert (kolom NOT NULL di Supabase)")
    return ReportInsertPayload(
        id = id,
        code = code,
        publicTrackingToken = token,
        category = category,
        severity = severity,
        status = status,
        riskLevel = riskLevel,
        riskScore = riskScore,
        description = description,
        address = address,
        lat = lat,
        lng = lng,
        kelurahan = kelurahan,
        kecamatan = kecamatan,
        reporterName = reporterName,
        reporterContact = reporterContact,
        submissionMode = submissionMode,
        rainfallMm = rainfallMm,
        fieldNotes = fieldNotesEl,
        completionPhotos = completionEl,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

// Extract Storage path dari public URL (untuk rollback delete kalau row insert gagal).
// Format URL: https://<project>.supabase.co/storage/v1/object/public/reports/report-photos/<file>
fun extractStoragePath(publicUrl: String): String? {
    val marker = "/storage/v1/object/public/reports/"
    val idx = publicUrl.indexOf(marker)
    if (idx < 0) return null
    return publicUrl.substring(idx + marker.length)
}
