package com.michael.frozendroid.domain.usecase

import com.michael.frozendroid.domain.repository.CommandExecutor
import com.michael.frozendroid.domain.repository.CommandResult
import javax.inject.Inject

class FreezeAppUseCase @Inject constructor(
    private val commandExecutor: CommandExecutor,
    private val checkSafetyLevelUseCase: CheckSafetyLevelUseCase
) {
    suspend operator fun invoke(packageName: String): CommandResult {
        FreezeSafetyPolicy.blockReason(packageName)?.let { reason ->
            return CommandResult.Failure(-1, reason)
        }

        if (checkSafetyLevelUseCase.isDangerous(packageName)) {
            return CommandResult.Failure(-1, "Operation blocked: package is classified as DANGEROUS to device stability.")
        }
        return commandExecutor.freeze(packageName)
    }
}
