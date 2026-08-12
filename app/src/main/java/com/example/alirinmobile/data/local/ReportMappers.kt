package com.example.alirinmobile.data.local

import com.example.alirinmobile.data.HistoryEntry
import com.example.alirinmobile.data.Report
import com.example.alirinmobile.data.ReportMode
import com.example.alirinmobile.data.ReportStatus
import com.example.alirinmobile.data.RiskLevel
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
private data class HistoryDto(
    val status: String,
    val whenLabel: String,
    val note: String? = null,
    val live: Boolean = false,
)

private val json = Json { ignoreUnknownKeys = true }
private val historySerializer = ListSerializer(HistoryDto.serializer())

fun Report.toEntity(updatedAtMs: Long): ReportEntity = ReportEntity(
    id = id,
    code = code,
    mode = mode.name,
    status = status.name,
    risk = risk.name,
    score = score,
    category = category,
    kelurahan = kelurahan,
    kecamatan = kecamatan,
    address = address,
    createdAt = createdAt,
    updatedAt = updatedAt,
    description = description,
    photos = photos,
    lat = lat,
    lng = lng,
    updatedAtMs = updatedAtMs,
    historyJson = json.encodeToString(
        historySerializer,
        history.map { HistoryDto(it.status.name, it.when_, it.note, it.live) },
    ),
)

fun ReportEntity.toDomain(): Report = Report(
    id = id,
    code = code,
    mode = ReportMode.valueOf(mode),
    status = ReportStatus.valueOf(status),
    risk = RiskLevel.valueOf(risk),
    score = score,
    category = category,
    kelurahan = kelurahan,
    kecamatan = kecamatan,
    address = address,
    createdAt = createdAt,
    updatedAt = updatedAt,
    description = description,
    photos = photos,
    lat = lat,
    lng = lng,
    history = runCatching {
        json.decodeFromString(historySerializer, historyJson).map {
            HistoryEntry(ReportStatus.valueOf(it.status), it.whenLabel, it.note, it.live)
        }
    }.getOrDefault(emptyList()),
)
