package com.michael.frozendroid.domain.usecase

import com.michael.frozendroid.domain.repository.CommandExecutor
import com.michael.frozendroid.domain.repository.CommandResult
import javax.inject.Inject

class UnfreezeAppUseCase @Inject constructor(
    private val commandExecutor: CommandExecutor
) {
    suspend operator fun invoke(packageName: String): CommandResult {
        FreezeSafetyPolicy.validatePackageName(packageName)?.let { reason ->
            return CommandResult.Failure(-1, reason)
        }

        return commandExecutor.unfreeze(packageName)
    }
}
