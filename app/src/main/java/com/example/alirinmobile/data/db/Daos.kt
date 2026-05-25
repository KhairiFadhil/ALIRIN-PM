package com.example.alirinmobile.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY datetime(updatedAt) DESC, datetime(createdAt) DESC")
    fun observeAll(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<ReportEntity?>

    @Query("SELECT * FROM reports WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ReportEntity?

    @Query("SELECT COUNT(*) FROM reports")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(report: ReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(reports: List<ReportEntity>)

    @Query("UPDATE reports SET status = :status, updatedAt = :ts WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, ts: String)
}

@Dao
interface DraftDao {
    @Query("SELECT * FROM drafts WHERE id = 0 LIMIT 1")
    fun observe(): Flow<DraftEntity?>

    @Query("SELECT * FROM drafts WHERE id = 0 LIMIT 1")
    suspend fun get(): DraftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(draft: DraftEntity)

    @Query("DELETE FROM drafts WHERE id = 0")
    suspend fun clear()
}

@Dao
interface PendingSubmissionDao {
    @Query("SELECT * FROM pending_submissions ORDER BY enqueuedAt ASC")
    suspend fun all(): List<PendingSubmissionEntity>

    @Query("SELECT COUNT(*) FROM pending_submissions")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pending: PendingSubmissionEntity): Long

    @Query("DELETE FROM pending_submissions WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE pending_submissions SET attempts = attempts + 1 WHERE id = :id")
    suspend fun incrementAttempts(id: Long)
}
