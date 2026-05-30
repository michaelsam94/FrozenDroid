package com.michael.frozendroid.domain.repository

sealed class CommandResult {
    data class Success(val output: String) : CommandResult()
    data class Failure(val code: Int, val stderr: String) : CommandResult()
    object PermissionDenied : CommandResult()
    object NotAvailable : CommandResult()
}

interface CommandExecutor {
    suspend fun freeze(packageName: String): CommandResult
    suspend fun unfreeze(packageName: String): CommandResult
    suspend fun forceStop(packageName: String): CommandResult
    suspend fun getPackageState(packageName: String): Int
}
