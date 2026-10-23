package com.migrely.app.di

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.home.presentation.viewmodel.HomeViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object HomeModule {
    @Provides
    fun providesHomeViewModel(useCaseExecutor: UseCaseExecutor): HomeViewModel =
        HomeViewModel(useCaseExecutor)
}
