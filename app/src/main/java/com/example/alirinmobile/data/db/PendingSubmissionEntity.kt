package com.example.alirinmobile.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Queued submission to send when the network is back. Stores the report id; the actual
 * report row lives in `reports` already (in 'pending' status).
 */
@Entity(tableName = "pending_submissions")
data class PendingSubmissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reportId: String,
    val attempts: Int = 0,
    val enqueuedAt: Long = System.currentTimeMillis(),
)
