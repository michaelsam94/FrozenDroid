package com.michael.frozendroid.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.michael.frozendroid.data.local.AppDatabase
import com.michael.frozendroid.data.local.daos.AppPackageDao
import com.michael.frozendroid.data.local.daos.ProfileDao
import com.michael.frozendroid.data.local.daos.TelemetryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DELETE FROM cpu_wake_events WHERE batteryDelta > 0")
        }
    }

    @Provides
    @Singleton
    fun provideRoomDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "frozendroid_database"
        )
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideAppPackageDao(database: AppDatabase): AppPackageDao {
        return database.appPackageDao()
    }

    @Provides
    @Singleton
    fun provideProfileDao(database: AppDatabase): ProfileDao {
        return database.profileDao()
    }

    @Provides
    @Singleton
    fun provideTelemetryDao(database: AppDatabase): TelemetryDao {
        return database.telemetryDao()
    }
}
