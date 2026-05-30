package com.michael.frozendroid.domain.usecase

import com.michael.frozendroid.domain.model.SafetyLevel
import com.michael.frozendroid.domain.repository.AppRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CheckSafetyLevelUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    suspend fun isDangerous(packageName: String): Boolean {
        // Query current app list to see what SafetyLevel is resolved
        val apps = appRepository.getApps().first()
        val match = apps.find { it.packageName == packageName }
        return match?.safetyLevel == SafetyLevel.DANGEROUS
    }
}
