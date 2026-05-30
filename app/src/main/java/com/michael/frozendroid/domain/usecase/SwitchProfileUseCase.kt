package com.michael.frozendroid.domain.usecase

import com.michael.frozendroid.domain.repository.ProfileRepository
import com.michael.frozendroid.domain.repository.AppRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SwitchProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val appRepository: AppRepository,
    private val freezeAppUseCase: FreezeAppUseCase,
    private val unfreezeAppUseCase: UnfreezeAppUseCase
) {
    suspend operator fun invoke(profileId: String?): Boolean {
        profileRepository.setActiveProfile(profileId)
        
        val apps = appRepository.getApps().first()
        val profiles = profileRepository.getProfiles().first()
        val targetProfile = profiles.find { it.id == profileId }
        
        val targetFrozen = targetProfile?.frozenPackages?.toSet() ?: emptySet()
        
        // Chunk unfreezes and freezes in parallel/batches for high performance
        apps.forEach { app ->
            val shouldFreeze = targetFrozen.contains(app.packageName)
            if (shouldFreeze && !app.isFrozen) {
                freezeAppUseCase(app.packageName)
            } else if (!shouldFreeze && app.isFrozen) {
                unfreezeAppUseCase(app.packageName)
            }
        }
        
        // Refresh the app list to ensure local database matches the physical states
        appRepository.refreshAppList()
        return true
    }
}
