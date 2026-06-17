package com.example.alirinmobile.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val code: String,
    val mode: String,
    val status: String,
    val risk: String,
    val score: Int,
    val category: String,
    val kelurahan: String,
    val kecamatan: String,
    val address: String?,
    val createdAt: String,
    val updatedAt: String?,
    val description: String,
    val photos: Int,
    val lat: Double?,
    val lng: Double?,
    val updatedAtMs: Long,
    val historyJson: String,
)

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY updatedAtMs DESC")
    fun observeAll(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE id = :id LIMIT 1")
    suspend fun get(id: String): ReportEntity?

    @Query("SELECT COUNT(*) FROM reports")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(report: ReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(reports: List<ReportEntity>)
}
