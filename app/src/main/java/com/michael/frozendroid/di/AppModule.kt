package com.michael.frozendroid.di

import android.content.Context
import com.michael.frozendroid.data.repository.AppRepositoryImpl
import com.michael.frozendroid.data.repository.ProfileRepositoryImpl
import com.michael.frozendroid.data.repository.TelemetryRepositoryImpl
import com.michael.frozendroid.domain.repository.AppRepository
import com.michael.frozendroid.domain.repository.CpuWakeTelemetryCollector
import com.michael.frozendroid.domain.repository.ProfileRepository
import com.michael.frozendroid.domain.repository.TelemetryRepository
import com.michael.frozendroid.framework.telemetry.SystemCpuWakeTelemetryCollector
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAppRepository(
        impl: AppRepositoryImpl
    ): AppRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        impl: ProfileRepositoryImpl
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindTelemetryRepository(
        impl: TelemetryRepositoryImpl
    ): TelemetryRepository

    @Binds
    @Singleton
    abstract fun bindCpuWakeTelemetryCollector(
        impl: SystemCpuWakeTelemetryCollector
    ): CpuWakeTelemetryCollector

    companion object {
        @Provides
        @Singleton
        fun provideContext(@ApplicationContext context: Context): Context {
            return context
        }
    }
}
