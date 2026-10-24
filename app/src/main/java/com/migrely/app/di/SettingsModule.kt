package com.migrely.app.di

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.navigation.mapper.SettingsNavigationEventDestinationMapper
import com.migrely.app.settings.data.repository.SettingsRepository
import com.migrely.app.settings.domain.usecase.GetSettingsUseCase
import com.migrely.app.settings.domain.usecase.GetThemeUseCase
import com.migrely.app.settings.domain.usecase.SetPeriodTrackingUseCase
import com.migrely.app.settings.presentation.mapper.SettingsPresentationMapper
import com.migrely.app.settings.presentation.viewmodel.SettingsViewModel
import com.migrely.app.settings.ui.di.SettingsDependencies
import com.migrely.app.settings.ui.mapper.SettingsEventPresenter
import com.migrely.app.settings.ui.mapper.SettingsNotificationUiMapper
import com.migrely.app.settings.ui.mapper.SettingsViewStateUiMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Settings (changes add-data-export-import and add-onboarding, BRD D70, D79). */
@Module
@InstallIn(SingletonComponent::class)
internal object SettingsModule {
    /** For the app itself, which draws every screen in the chosen theme (BRD D94). */
    @Provides
    fun providesGetThemeUseCase(
        repository: SettingsRepository,
        coroutines: CoroutineContextProvider
    ) = GetThemeUseCase(repository, coroutines)

    @Provides
    fun providesSettingsViewModel(
        repository: SettingsRepository,
        coroutines: CoroutineContextProvider,
        useCaseExecutor: UseCaseExecutor
    ) = SettingsViewModel(
        GetSettingsUseCase(repository, coroutines),
        SetPeriodTrackingUseCase(repository, coroutines),
        SettingsPresentationMapper(),
        useCaseExecutor
    )

    @Provides
    fun providesSettingsDependencies(settingsViewModel: SettingsViewModel): SettingsDependencies {
        val presenter = SettingsEventPresenter()
        return SettingsDependencies(
            settingsViewModel = settingsViewModel,
            settingsViewStateUiMapper = SettingsViewStateUiMapper(),
            settingsEventPresenter = presenter,
            navigationMapper = SettingsNavigationEventDestinationMapper(),
            notificationMapper = SettingsNotificationUiMapper(presenter)
        )
    }
}
