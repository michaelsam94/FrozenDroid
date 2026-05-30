package com.michael.frozendroid.data.local.daos

import androidx.room.*
import com.michael.frozendroid.data.local.entities.FreezeProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM freeze_profiles ORDER BY name ASC")
    fun getAllProfilesFlow(): Flow<List<FreezeProfileEntity>>

    @Query("SELECT * FROM freeze_profiles WHERE isActive = 1 LIMIT 1")
    fun getActiveProfileFlow(): Flow<FreezeProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: FreezeProfileEntity)

    @Query("DELETE FROM freeze_profiles WHERE id = :id")
    suspend fun deleteProfile(id: String)

    @Transaction
    suspend fun setActiveProfile(activeId: String?) {
        clearActiveProfiles()
        if (activeId != null) {
            markProfileActive(activeId)
        }
    }

    @Query("UPDATE freeze_profiles SET isActive = 0")
    suspend fun clearActiveProfiles()

    @Query("UPDATE freeze_profiles SET isActive = 1 WHERE id = :id")
    suspend fun markProfileActive(id: String)
}
