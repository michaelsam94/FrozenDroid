package com.michael.frozendroid.data.local.daos

import androidx.room.*
import com.michael.frozendroid.data.local.entities.AppPackageEntity
import com.michael.frozendroid.data.local.entities.UserOverrideEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppPackageDao {
    @Query("SELECT * FROM app_packages ORDER BY label ASC")
    fun getAllAppsFlow(): Flow<List<AppPackageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<AppPackageEntity>)

    @Query("DELETE FROM app_packages")
    suspend fun clearAllApps()

    @Query("SELECT * FROM user_overrides")
    fun getAllOverridesFlow(): Flow<List<UserOverrideEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOverride(override: UserOverrideEntity)

    @Query("DELETE FROM user_overrides WHERE packageName = :packageName")
    suspend fun deleteOverride(packageName: String)
}
