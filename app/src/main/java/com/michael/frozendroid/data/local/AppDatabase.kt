package com.michael.frozendroid.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.michael.frozendroid.data.local.converters.RoomConverters
import com.michael.frozendroid.data.local.daos.AppPackageDao
import com.michael.frozendroid.data.local.daos.ProfileDao
import com.michael.frozendroid.data.local.daos.TelemetryDao
import com.michael.frozendroid.data.local.entities.AppPackageEntity
import com.michael.frozendroid.data.local.entities.CpuWakeEventEntity
import com.michael.frozendroid.data.local.entities.FreezeProfileEntity
import com.michael.frozendroid.data.local.entities.UserOverrideEntity

@Database(
    entities = [
        AppPackageEntity::class,
        FreezeProfileEntity::class,
        CpuWakeEventEntity::class,
        UserOverrideEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appPackageDao(): AppPackageDao
    abstract fun profileDao(): ProfileDao
    abstract fun telemetryDao(): TelemetryDao
}
