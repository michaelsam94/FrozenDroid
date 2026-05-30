package com.michael.frozendroid.domain.usecase

import com.michael.frozendroid.domain.model.AppPackage
import com.michael.frozendroid.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetInstalledAppsUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    operator fun invoke(): Flow<List<AppPackage>> {
        return appRepository.getApps()
    }
}
