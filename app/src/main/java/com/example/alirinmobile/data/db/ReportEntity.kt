package com.example.alirinmobile.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.alirinmobile.data.HistoryEntry
import com.example.alirinmobile.data.Report
import com.example.alirinmobile.data.ReportMode
import com.example.alirinmobile.data.ReportStatus
import com.example.alirinmobile.data.RiskLevel

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val code: String,
    val mode: String,            // ReportMode name
    val status: String,          // ReportStatus name
    val risk: String,            // RiskLevel name
    val score: Int,
    val category: String,
    val kelurahan: String,
    val kecamatan: String,
    val address: String? = null,
    val createdAt: String,
    val updatedAt: String? = null,
    val description: String = "",
    val photos: Int = 0,
    val history: List<HistoryEntry> = emptyList(),
)

fun ReportEntity.toDomain(): Report = Report(
    id = id, code = code,
    mode = ReportMode.valueOf(mode),
    status = ReportStatus.valueOf(status),
    risk = RiskLevel.valueOf(risk),
    score = score, category = category,
    kelurahan = kelurahan, kecamatan = kecamatan,
    address = address, createdAt = createdAt, updatedAt = updatedAt,
    description = description, photos = photos, history = history,
)

fun Report.toEntity(): ReportEntity = ReportEntity(
    id = id, code = code, mode = mode.name, status = status.name,
    risk = risk.name, score = score, category = category,
    kelurahan = kelurahan, kecamatan = kecamatan, address = address,
    createdAt = createdAt, updatedAt = updatedAt,
    description = description, photos = photos, history = history,
)
