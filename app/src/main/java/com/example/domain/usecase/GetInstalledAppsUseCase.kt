package com.example.domain.usecase

import com.example.domain.model.AppPackage
import com.example.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetInstalledAppsUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    operator fun invoke(): Flow<List<AppPackage>> {
        return appRepository.getApps()
    }
}
