package com.example.alirinmobile.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.alirinmobile.data.db.AppDatabase
import kotlinx.coroutines.delay

/**
 * Drains the local pending_submissions queue. The real Supabase upload happens here once
 * the backend is wired — for now it just simulates a successful sync after a short delay
 * so the queue clears and downstream observers reflect "sent" state.
 */
class SubmitReportWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.get(applicationContext)
        val pendingDao = db.pendingDao()
        val pending = pendingDao.all()
        if (pending.isEmpty()) return Result.success()

        pending.forEach { row ->
            try {
                // TODO Supabase: upload photos to Storage, INSERT into reports table, etc.
                delay(250)
                pendingDao.delete(row.id)
            } catch (t: Throwable) {
                pendingDao.incrementAttempts(row.id)
                if (row.attempts >= 5) pendingDao.delete(row.id)
                return Result.retry()
            }
        }
        return Result.success()
    }
}
