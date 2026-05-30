package com.michael.frozendroid.framework.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.michael.frozendroid.domain.repository.CpuWakeTelemetryCollector
import com.michael.frozendroid.domain.repository.TelemetryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class CpuMonitorWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val telemetryRepository: TelemetryRepository,
    private val cpuWakeTelemetryCollector: CpuWakeTelemetryCollector
) : CoroutineWorker(context, workerParams) {

    private val TAG = "CpuMonitorWorker"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting periodic CPU background monitor cycle...")

            // 1. Prune ancient data (> 7 days)
            val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            telemetryRepository.pruneOldEvents(sevenDaysAgo)
            Log.d(TAG, "Pruned background telemetry records older than 7 days.")

            // 2. Capture real wake-lock data exposed by the OS.
            val newEvents = cpuWakeTelemetryCollector.collectWakeEvents()
            if (newEvents.isNotEmpty()) {
                telemetryRepository.insertCpuWakeEvents(newEvents)
                Log.d(TAG, "Logged ${newEvents.size} real wake telemetry records successfully.")
            } else {
                Log.d(TAG, "No CPU wake telemetry was available from this device.")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error executing periodic telemetry monitoring", e)
            Result.retry()
        }
    }
}
