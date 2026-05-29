package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.converters.RoomConverters
import com.example.data.local.daos.AppPackageDao
import com.example.data.local.daos.ProfileDao
import com.example.data.local.daos.TelemetryDao
import com.example.data.local.entities.AppPackageEntity
import com.example.data.local.entities.CpuWakeEventEntity
import com.example.data.local.entities.FreezeProfileEntity
import com.example.data.local.entities.UserOverrideEntity

@Database(
    entities = [
        AppPackageEntity::class,
        FreezeProfileEntity::class,
        CpuWakeEventEntity::class,
        UserOverrideEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appPackageDao(): AppPackageDao
    abstract fun profileDao(): ProfileDao
    abstract fun telemetryDao(): TelemetryDao
}
