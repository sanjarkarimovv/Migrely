package com.migrely.app.di

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.coroutine.CoroutineContextProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object ArchitectureModule {
    @Provides
    fun providesUseCaseExecutor(): UseCaseExecutor = UseCaseExecutor()

    @Provides
    fun providesCoroutineContextProvider(): CoroutineContextProvider =
        CoroutineContextProvider.Default
}
