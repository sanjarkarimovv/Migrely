package com.migrely.app.di

import com.migrely.app.architecture.domain.UseCaseExecutor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object ArchitectureModule {
    @Provides
    fun providesUseCaseExecutor(): UseCaseExecutor = UseCaseExecutor()
}
