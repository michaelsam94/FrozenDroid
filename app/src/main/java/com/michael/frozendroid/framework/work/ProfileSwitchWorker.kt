package com.michael.frozendroid.framework.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.michael.frozendroid.domain.usecase.FreezeAppUseCase
import com.michael.frozendroid.domain.usecase.UnfreezeAppUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

@HiltWorker
class ProfileSwitchWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val freezeUseCase: FreezeAppUseCase,
    private val unfreezeUseCase: UnfreezeAppUseCase
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_FREEZE_LIST = "freeze_list"
        const val KEY_UNFREEZE_LIST = "unfreeze_list"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val freezePackages = inputData.getStringArray(KEY_FREEZE_LIST)?.toList() ?: emptyList()
        val unfreezePackages = inputData.getStringArray(KEY_UNFREEZE_LIST)?.toList() ?: emptyList()

        // Unfreeze packages in batches of 5 in parallel to accelerate restore action
        unfreezePackages.chunked(5).forEach { chunk ->
            coroutineScope {
                chunk.map { pkg ->
                    async { unfreezeUseCase(pkg) }
                }.awaitAll()
            }
        }

        // Freeze packages in batches of 5 in parallel
        freezePackages.chunked(5).forEach { chunk ->
            coroutineScope {
                chunk.map { pkg ->
                    async { freezeUseCase(pkg) }
                }.awaitAll()
            }
        }

        Result.success()
    }
}
