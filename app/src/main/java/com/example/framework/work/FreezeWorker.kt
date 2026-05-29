package com.example.framework.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.domain.repository.CommandResult
import com.example.domain.usecase.FreezeAppUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class FreezeWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val freezeAppUseCase: FreezeAppUseCase
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_PACKAGE_NAME = "package_name"
        const val KEY_PROGRESS = "progress"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val packageName = inputData.getString(KEY_PACKAGE_NAME) ?: return@withContext Result.failure()

        setProgress(workDataOf(KEY_PROGRESS to 20))
        
        // Execute the safety-gated freeze action
        setProgress(workDataOf(KEY_PROGRESS to 50))
        val result = freezeAppUseCase(packageName)
        setProgress(workDataOf(KEY_PROGRESS to 100))

        when (result) {
            is CommandResult.Success -> {
                Result.success(workDataOf("status" to "SUCCESS", "output" to result.output))
            }
            is CommandResult.Failure -> {
                Result.failure(workDataOf("status" to "FAILED", "error" to result.stderr))
            }
            else -> {
                Result.failure(workDataOf("status" to "DENIED", "error" to "Permission or privilege not initialized"))
            }
        }
    }
}
