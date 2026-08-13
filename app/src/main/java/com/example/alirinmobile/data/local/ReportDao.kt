package com.example.alirinmobile.data.local

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// NOTE: no UNIQUE constraints locally. Source of truth for uniqueness is Supabase;
// duplicating UNIQUE here causes INSERT OR REPLACE to silently drop unrelated rows
// when many sync-batch rows share an empty tracking_token (view sengaja tidak
// membocorkan token karena token = kredensial akses ke laporan pribadi).
@Entity(
    tableName = "reports",
    indices = [
        Index(value = ["code"]),
        Index(value = ["public_tracking_token"]),
        Index(value = ["updated_at_ms"]),
        Index(value = ["sync_status"]),
    ],
)
data class ReportEntity(
    @PrimaryKey val id: String,
    val code: String,
    @ColumnInfo(name = "public_tracking_token") val publicTrackingToken: String?,
    val category: String,
    val severity: String,
    val status: String,
    @ColumnInfo(name = "risk_level") val riskLevel: String,
    @ColumnInfo(name = "risk_score") val riskScore: Int,
    val description: String,
    val address: String?,
    val lat: Double,
    val lng: Double,
    val kelurahan: String,
    val kecamatan: String,
    @ColumnInfo(name = "reporter_name") val reporterName: String?,
    @ColumnInfo(name = "reporter_contact") val reporterContact: String?,
    @ColumnInfo(name = "assigned_officer_id") val assignedOfficerId: String?,
    @ColumnInfo(name = "assigned_officer_name") val assignedOfficerName: String?,
    @ColumnInfo(name = "blocked_reason") val blockedReason: String?,
    @ColumnInfo(name = "archived_at") val archivedAt: String?,
    @ColumnInfo(name = "submission_mode") val submissionMode: String?,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String,
    @ColumnInfo(name = "updated_at_ms") val updatedAtMs: Long,

    @ColumnInfo(name = "photos_json") val photosJson: String,
    @ColumnInfo(name = "completion_photos_json") val completionPhotosJson: String,
    @ColumnInfo(name = "risk_breakdown_json") val riskBreakdownJson: String,
    @ColumnInfo(name = "status_history_json") val statusHistoryJson: String,
    @ColumnInfo(name = "field_notes_json") val fieldNotesJson: String,

    @ColumnInfo(name = "sync_status") val syncStatus: String,
    @ColumnInfo(name = "sync_attempts") val syncAttempts: Int,
    @ColumnInfo(name = "sync_last_error") val syncLastError: String?,
    @ColumnInfo(name = "local_only") val localOnly: Boolean,
)

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY updated_at_ms DESC")
    fun observeAll(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE id = :id LIMIT 1")
    fun observe(id: String): Flow<ReportEntity?>

    @Query("SELECT * FROM reports WHERE id = :id LIMIT 1")
    suspend fun get(id: String): ReportEntity?

    @Query("SELECT * FROM reports WHERE public_tracking_token = :token LIMIT 1")
    suspend fun findByToken(token: String): ReportEntity?

    @Query("SELECT code FROM reports WHERE code LIKE :yearPrefix || '%' ORDER BY code DESC LIMIT 1")
    suspend fun latestCodeForYear(yearPrefix: String): String?

    @Query("SELECT * FROM reports WHERE sync_status IN ('pending','failed') ORDER BY updated_at_ms ASC")
    suspend fun pendingOutbox(): List<ReportEntity>

    @Query("SELECT id FROM reports WHERE local_only = 1")
    suspend fun localOnlyIds(): List<String>

    @Query("SELECT COUNT(*) FROM reports")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(report: ReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(reports: List<ReportEntity>)

    @Query("UPDATE reports SET sync_status = :s, sync_attempts = sync_attempts + 1, sync_last_error = :err WHERE id = :id")
    suspend fun markSyncState(id: String, s: String, err: String?)

    @Query("UPDATE reports SET sync_status = :s, local_only = :localOnly, sync_last_error = NULL WHERE id = :id")
    suspend fun markSynced(id: String, s: String, localOnly: Boolean)
}
