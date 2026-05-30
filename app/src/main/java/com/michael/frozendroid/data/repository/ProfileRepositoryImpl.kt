package com.michael.frozendroid.data.repository

import com.michael.frozendroid.data.local.daos.ProfileDao
import com.michael.frozendroid.data.local.entities.FreezeProfileEntity
import com.michael.frozendroid.domain.model.FreezeProfile
import com.michael.frozendroid.domain.repository.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao
) : ProfileRepository {

    override fun getProfiles(): Flow<List<FreezeProfile>> {
        return profileDao.getAllProfilesFlow().map { entities ->
            entities.map { entity ->
                FreezeProfile(
                    id = entity.id,
                    name = entity.name,
                    icon = entity.icon,
                    frozenPackages = entity.frozenPackages,
                    scheduleTimes = entity.scheduleTimes,
                    isActive = entity.isActive
                )
            }
        }.flowOn(Dispatchers.Default)
    }

    override fun getActiveProfile(): Flow<FreezeProfile?> {
        return profileDao.getActiveProfileFlow().map { entity ->
            entity?.let {
                FreezeProfile(
                    id = it.id,
                    name = it.name,
                    icon = it.icon,
                    frozenPackages = it.frozenPackages,
                    scheduleTimes = it.scheduleTimes,
                    isActive = it.isActive
                )
            }
        }.flowOn(Dispatchers.Default)
    }

    override suspend fun insertProfile(profile: FreezeProfile) = withContext(Dispatchers.IO) {
        profileDao.insertProfile(
            FreezeProfileEntity(
                id = profile.id,
                name = profile.name,
                icon = profile.icon,
                frozenPackages = profile.frozenPackages,
                scheduleTimes = profile.scheduleTimes,
                isActive = profile.isActive
            )
        )
    }

    override suspend fun deleteProfile(id: String) = withContext(Dispatchers.IO) {
        profileDao.deleteProfile(id)
    }

    override suspend fun setActiveProfile(id: String?) = withContext(Dispatchers.IO) {
        profileDao.setActiveProfile(id)
    }
}
