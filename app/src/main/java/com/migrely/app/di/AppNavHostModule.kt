package com.migrely.app.di

import com.migrely.app.attack.ui.di.AttackDetailsDependencies
import com.migrely.app.home.ui.di.HomeDependencies
import com.migrely.app.ui.main.di.AppNavHostDependencies
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object AppNavHostModule {
    @Provides
    fun providesAppNavHostDependencies(
        homeDependencies: HomeDependencies,
        attackDetailsDependencies: AttackDetailsDependencies
    ): AppNavHostDependencies = AppNavHostDependencies(
        homeDependencies = homeDependencies,
        attackDetailsDependencies = attackDetailsDependencies
    )
}
