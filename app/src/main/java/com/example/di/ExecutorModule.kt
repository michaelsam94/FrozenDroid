package com.example.di

import com.example.domain.repository.CommandExecutor
import com.example.framework.CommandExecutorImpl
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
