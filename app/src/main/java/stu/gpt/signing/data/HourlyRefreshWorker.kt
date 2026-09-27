package stu.gpt.signing.data

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Hourly refresh worker that:
 * - Refreshes all databases from the last-picked document URIs (Names, Roles, Locations, Stage)
 */
class HourlyRefreshWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("HourlyRefreshWorker", "Starting hourly refresh job…")
            Repository.logAdminEvent("Hourly refresh job started")

            // Refresh all databases from persisted URIs if changed
            // For hourly refresh, we don't need conditional Cast behavior
            Repository.refreshAllFromPersistedUrisIfChanged(false)

            Log.d("HourlyRefreshWorker", "Hourly refresh job completed successfully.")
            Repository.logAdminEvent("Hourly refresh job completed successfully")
            Result.success()
        } catch (t: Throwable) {
            Log.e("HourlyRefreshWorker", "Hourly refresh job failed: ${t.message}", t)
            Repository.logAdminEvent("Hourly refresh job failed: ${t.message}")
            Result.retry()
        }
    }
}
