package com.example.domain.usecase

import com.example.domain.repository.CommandExecutor
import com.example.domain.repository.CommandResult
import javax.inject.Inject

class UnfreezeAppUseCase @Inject constructor(
    private val commandExecutor: CommandExecutor
) {
    suspend operator fun invoke(packageName: String): CommandResult {
        return commandExecutor.unfreeze(packageName)
    }
}
