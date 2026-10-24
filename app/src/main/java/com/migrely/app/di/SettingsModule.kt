package com.migrely.app.di

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.navigation.mapper.SettingsNavigationEventDestinationMapper
import com.migrely.app.settings.presentation.viewmodel.SettingsViewModel
import com.migrely.app.settings.ui.di.SettingsDependencies
import com.migrely.app.settings.ui.mapper.SettingsNotificationUiMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Settings (change add-data-export-import, BRD D70). */
@Module
@InstallIn(SingletonComponent::class)
internal object SettingsModule {
    @Provides
    fun providesSettingsDependencies(useCaseExecutor: UseCaseExecutor) = SettingsDependencies(
        settingsViewModel = SettingsViewModel(useCaseExecutor),
        navigationMapper = SettingsNavigationEventDestinationMapper(),
        notificationMapper = SettingsNotificationUiMapper()
    )
}
