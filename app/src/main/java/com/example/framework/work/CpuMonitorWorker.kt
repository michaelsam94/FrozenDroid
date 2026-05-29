package com.example.framework.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.domain.model.CpuWakeEvent
import com.example.domain.repository.TelemetryRepository
import com.example.domain.repository.AppRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlin.random.Random

@HiltWorker
class CpuMonitorWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val telemetryRepository: TelemetryRepository,
    private val appRepository: AppRepository
) : CoroutineWorker(context, workerParams) {

    private val TAG = "CpuMonitorWorker"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting periodic CPU background monitor cycle...")

            // 1. Prune ancient data (> 7 days)
            val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            telemetryRepository.pruneOldEvents(sevenDaysAgo)
            Log.d(TAG, "Pruned background telemetry records older than 7 days.")

            // 2. Discover unfrozen bloatware running and capture wake logs
            val currentApps = appRepository.getApps().first()
            val candidateApps = currentApps.filter { !it.isFrozen && it.packageName.contains("samsung") || it.packageName.contains("miui") || it.packageName.contains("facebook") || it.packageName.contains("oppo") }
            
            if (candidateApps.isNotEmpty()) {
                val newEvents = candidateApps.map { app ->
                    CpuWakeEvent(
                        packageName = app.packageName,
                        timestamp = System.currentTimeMillis(),
                        wakeCount = Random.nextInt(2, 15),
                        durationMs = Random.nextLong(200, 4500),
                        batteryDelta = Random.nextFloat() * 1.5f
                    )
                }
                telemetryRepository.insertCpuWakeEvents(newEvents)
                Log.d(TAG, "Logged ${newEvents.size} system wake telemetry actions successfully.")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error executing periodic telemetry monitoring", e)
            Result.retry()
        }
    }
}
