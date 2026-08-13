package com.example.alirinmobile.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// Bentuknya matches baik tabel reports (staff SELECT) maupun view public_reports
// (anon SELECT). Field yang hanya ada di salah satunya di-nullable.
// Nested collections dibiarkan sebagai JsonElement supaya bisa menerima jsonb
// dari view (array) dan juga bisa null bila datang dari raw table select.
@Serializable
data class SupabaseReportDto(
    val id: String,
    val code: String,
    @SerialName("public_tracking_token") val publicTrackingToken: String? = null,
    val category: String,
    val severity: String,
    val status: String,
    @SerialName("risk_level") val riskLevel: String,
    @SerialName("risk_score") val riskScore: Int,
    val description: String,
    val address: String? = null,
    val lat: Double,
    val lng: Double,
    val kelurahan: String,
    val kecamatan: String,
    @SerialName("reporter_name") val reporterName: String? = null,
    @SerialName("reporter_contact") val reporterContact: String? = null,
    @SerialName("assigned_officer_id") val assignedOfficerId: String? = null,
    @SerialName("assigned_officer_name") val assignedOfficerName: String? = null,
    @SerialName("blocked_reason") val blockedReason: String? = null,
    @SerialName("submission_mode") val submissionMode: String? = null,
    @SerialName("archived_at") val archivedAt: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("field_notes") val fieldNotes: JsonElement? = null,
    @SerialName("completion_photos") val completionPhotos: JsonElement? = null,
    @SerialName("report_photos") val reportPhotos: JsonElement? = null,
    @SerialName("risk_breakdowns") val riskBreakdowns: JsonElement? = null,
    @SerialName("report_status_history") val reportStatusHistory: JsonElement? = null,
)

// Payload untuk insert ke tabel reports. Dipisah biar tidak bawa nested collections
// (yang di-insert lewat tabel anak terpisah), dan supaya field yang dibiarkan
// server (created_at/updated_at default now(), plus generated columns) tidak
// ikut kalau kita mau. Untuk konsistensi lintas device, kita kirimkan created_at
// & updated_at sendiri.
@Serializable
data class ReportInsertPayload(
    val id: String,
    val code: String,
    @SerialName("public_tracking_token") val publicTrackingToken: String,
    val category: String,
    val severity: String,
    val status: String,
    @SerialName("risk_level") val riskLevel: String,
    @SerialName("risk_score") val riskScore: Int,
    val description: String,
    val address: String? = null,
    val lat: Double,
    val lng: Double,
    val kelurahan: String,
    val kecamatan: String,
    @SerialName("reporter_name") val reporterName: String? = null,
    @SerialName("reporter_contact") val reporterContact: String? = null,
    @SerialName("submission_mode") val submissionMode: String? = null,
    @SerialName("field_notes") val fieldNotes: JsonElement? = null,
    @SerialName("completion_photos") val completionPhotos: JsonElement? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class ReportPhotoInsertPayload(
    @SerialName("report_id") val reportId: String,
    val url: String,
    val name: String? = null,
    val type: String? = null,
    val size: Int = 0,
    val kind: String = "report",
)

@Serializable
data class StatusHistoryInsertPayload(
    @SerialName("report_id") val reportId: String,
    val status: String,
    val actor: String,
    val note: String? = null,
    val at: String,
)

@Serializable
data class TrackingTokenRpcParams(
    @SerialName("p_token") val pToken: String,
)
