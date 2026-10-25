package com.migrely.app.di

import com.migrely.app.activity.CurrentActivityHolder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * The activity on screen, which `MigrelyApplication` keeps up to date (change add-subscription,
 * design D1).
 */
@Module
@InstallIn(SingletonComponent::class)
internal object CurrentActivityModule {
    @Provides
    @Singleton
    fun providesCurrentActivityHolder() = CurrentActivityHolder()
}
