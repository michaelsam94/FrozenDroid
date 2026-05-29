package com.example.domain.usecase

import com.example.domain.repository.CommandExecutor
import com.example.domain.repository.CommandResult
import javax.inject.Inject

class FreezeAppUseCase @Inject constructor(
    private val commandExecutor: CommandExecutor,
    private val checkSafetyLevelUseCase: CheckSafetyLevelUseCase
) {
    suspend operator fun invoke(packageName: String): CommandResult {
        if (checkSafetyLevelUseCase.isDangerous(packageName)) {
            return CommandResult.Failure(-1, "Operation blocked: package is classified as DANGEROUS to device stability.")
        }
        return commandExecutor.freeze(packageName)
    }
}
