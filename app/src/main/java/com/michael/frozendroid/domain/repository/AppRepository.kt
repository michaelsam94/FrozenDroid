package com.michael.frozendroid.domain.repository

import com.michael.frozendroid.domain.model.AppPackage
import com.michael.frozendroid.domain.model.SafetyLevel
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    fun getApps(): Flow<List<AppPackage>>
    suspend fun refreshAppList()
    suspend fun saveSafetyOverride(packageName: String, safetyLevel: SafetyLevel?)
    fun getSafetyOverrides(): Flow<Map<String, SafetyLevel>>
}
