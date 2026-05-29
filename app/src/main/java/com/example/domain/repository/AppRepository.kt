package com.example.domain.repository

import com.example.domain.model.AppPackage
import com.example.domain.model.SafetyLevel
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    fun getApps(): Flow<List<AppPackage>>
    suspend fun refreshAppList()
    suspend fun saveSafetyOverride(packageName: String, safetyLevel: SafetyLevel?)
    fun getSafetyOverrides(): Flow<Map<String, SafetyLevel>>
}
