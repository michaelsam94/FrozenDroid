package com.michael.frozendroid.di

import com.michael.frozendroid.domain.repository.CommandExecutor
import com.michael.frozendroid.framework.CommandExecutorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExecutorModule {

    @Binds
    @Singleton
    abstract fun bindCommandExecutor(
        executorImpl: CommandExecutorImpl
    ): CommandExecutor
}
