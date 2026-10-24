package com.migrely.app.di

import android.content.Context
import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.ui.notification.mapper.NoNotificationUiMapper
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.datasource.notification.datasource.NotificationDataSource
import com.migrely.app.navigation.mapper.SettingsNavigationEventDestinationMapper
import com.migrely.app.navigation.mapper.SettingsPageNavigationEventDestinationMapper
import com.migrely.app.settings.data.repository.DeleteAllDataRepository
import com.migrely.app.settings.data.repository.SettingsRepository
import com.migrely.app.settings.domain.usecase.DeleteAllDataUseCase
import com.migrely.app.settings.domain.usecase.GetSettingsUseCase
import com.migrely.app.settings.domain.usecase.GetThemeUseCase
import com.migrely.app.settings.domain.usecase.SetPeriodTrackingUseCase
import com.migrely.app.settings.domain.usecase.SetThemeUseCase
import com.migrely.app.settings.presentation.mapper.SettingsPresentationMapper
import com.migrely.app.settings.presentation.mapper.ThemeDomainMapper
import com.migrely.app.settings.presentation.mapper.ThemePresentationMapper
import com.migrely.app.settings.presentation.viewmodel.SettingsPageViewModel
import com.migrely.app.settings.presentation.viewmodel.SettingsViewModel
import com.migrely.app.settings.ui.di.SettingsDependencies
import com.migrely.app.settings.ui.di.SettingsPageDependencies
import com.migrely.app.settings.ui.mapper.SettingsEventPresenter
import com.migrely.app.settings.ui.mapper.SettingsNotificationUiMapper
import com.migrely.app.settings.ui.mapper.SettingsViewStateUiMapper
import com.migrely.app.settings.ui.mapper.ThemeUiMapper
import com.migrely.app.settings.ui.mapper.ThemeUiToPresentationMapper
import com.migrely.app.start.restartApp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Settings (changes add-data-export-import, add-onboarding and add-settings-and-safety, BRD D70, D79, D93–D97). */
@Module
@InstallIn(SingletonComponent::class)
internal object SettingsModule {
    /** For the app itself, which draws every screen in the chosen theme (BRD D94). */
    @Provides
    fun providesGetThemeUseCase(
        repository: SettingsRepository,
        coroutines: CoroutineContextProvider
    ) = GetThemeUseCase(repository, coroutines)

    /** One for the app, so deleting all data survives a rotation (ARCHITECTURE section 13). */
    @Provides
    @Singleton
    fun providesSettingsViewModel(
        repository: SettingsRepository,
        deleteAllDataRepository: DeleteAllDataRepository,
        coroutines: CoroutineContextProvider,
        useCaseExecutor: UseCaseExecutor
    ) = SettingsViewModel(
        GetSettingsUseCase(repository, coroutines),
        SetPeriodTrackingUseCase(repository, coroutines),
        SetThemeUseCase(repository, coroutines),
        DeleteAllDataUseCase(
            deleteAllDataRepository,
            deleteAllDataRepository,
            deleteAllDataRepository,
            deleteAllDataRepository,
            coroutines
        ),
        SettingsPresentationMapper(ThemePresentationMapper()),
        ThemeDomainMapper(),
        useCaseExecutor
    )

    @Provides
    @Singleton
    fun providesSettingsDependencies(
        settingsViewModel: SettingsViewModel,
        notificationDataSource: NotificationDataSource,
        @ApplicationContext context: Context
    ): SettingsDependencies {
        val presenter = SettingsEventPresenter()
        return SettingsDependencies(
            settingsViewModel = settingsViewModel,
            settingsViewStateUiMapper = SettingsViewStateUiMapper(ThemeUiMapper()),
            themeUiToPresentationMapper = ThemeUiToPresentationMapper(),
            settingsEventPresenter = presenter,
            navigationMapper = SettingsNavigationEventDestinationMapper {
                // A reminder that ran while everything was deleted may have posted after the cancel step.
                notificationDataSource.cancelAll()
                restartApp(context)
            },
            notificationMapper = SettingsNotificationUiMapper(presenter)
        )
    }

    @Provides
    fun providesSettingsPageDependencies(useCaseExecutor: UseCaseExecutor) =
        SettingsPageDependencies(
            settingsPageViewModel = SettingsPageViewModel(useCaseExecutor),
            navigationMapper = SettingsPageNavigationEventDestinationMapper(),
            notificationMapper = NoNotificationUiMapper()
        )
}
