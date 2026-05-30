package com.michael.frozendroid.domain.repository

import com.michael.frozendroid.domain.model.FreezeProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getProfiles(): Flow<List<FreezeProfile>>
    fun getActiveProfile(): Flow<FreezeProfile?>
    suspend fun insertProfile(profile: FreezeProfile)
    suspend fun deleteProfile(id: String)
    suspend fun setActiveProfile(id: String?)
}
