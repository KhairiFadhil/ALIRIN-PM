package com.example.alirinmobile.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.alirinmobile.AlirinApplication

// Periodic (15 menit) top-up sync di background. Selain itu, PetaScreen memicu
// syncNow() manual sesering polling foreground (30s), dan NetworkCallback
// memicu retryPending() saat network kembali online.
class SyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? AlirinApplication ?: return Result.success()
        val outboxOk = runCatching { app.reportRepository.retryPending() }.isSuccess
        val syncOk = runCatching { app.reportRepository.syncNow() }.isSuccess
        return if (outboxOk && syncOk) Result.success() else Result.retry()
    }
}
