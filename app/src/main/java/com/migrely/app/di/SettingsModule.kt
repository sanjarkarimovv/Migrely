package com.migrely.app.di

import android.content.Context
import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.ui.notification.mapper.NoNotificationUiMapper
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.datasource.notification.datasource.NotificationDataSource
import com.migrely.app.navigation.mapper.DeleteAllDataNavigationEventDestinationMapper
import com.migrely.app.navigation.mapper.SettingsNavigationEventDestinationMapper
import com.migrely.app.navigation.mapper.SettingsPageNavigationEventDestinationMapper
import com.migrely.app.settings.data.repository.DeleteAllDataRepository
import com.migrely.app.settings.data.repository.SettingsRepository
import com.migrely.app.settings.domain.usecase.DeleteAllDataUseCase
import com.migrely.app.settings.domain.usecase.GetAppearanceUseCase
import com.migrely.app.settings.domain.usecase.GetSettingsUseCase
import com.migrely.app.settings.domain.usecase.SetPeriodTrackingUseCase
import com.migrely.app.settings.domain.usecase.SetThemeUseCase
import com.migrely.app.settings.presentation.mapper.DeleteConfirmationResolver
import com.migrely.app.settings.presentation.mapper.SettingsPresentationMapper
import com.migrely.app.settings.presentation.mapper.ThemeDomainMapper
import com.migrely.app.settings.presentation.mapper.ThemePresentationMapper
import com.migrely.app.settings.presentation.viewmodel.DeleteAllDataViewModel
import com.migrely.app.settings.presentation.viewmodel.SettingsPageViewModel
import com.migrely.app.settings.presentation.viewmodel.SettingsViewModel
import com.migrely.app.settings.ui.di.DeleteAllDataDependencies
import com.migrely.app.settings.ui.di.SettingsDependencies
import com.migrely.app.settings.ui.di.SettingsPageDependencies
import com.migrely.app.settings.ui.mapper.DeleteAllDataEventPresenter
import com.migrely.app.settings.ui.mapper.DeleteAllDataNotificationUiMapper
import com.migrely.app.settings.ui.mapper.DeleteAllDataViewStateUiMapper
import com.migrely.app.settings.ui.mapper.DeleteProgressUiResolver
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
    /** For the app itself, which draws every screen in the chosen theme, or dark during an attack (BRD D94). */
    @Provides
    fun providesGetAppearanceUseCase(
        repository: SettingsRepository,
        coroutines: CoroutineContextProvider
    ): GetAppearanceUseCase = GetAppearanceUseCase(repository, repository, coroutines)

    @Provides
    fun providesSettingsViewModel(
        repository: SettingsRepository,
        coroutines: CoroutineContextProvider,
        useCaseExecutor: UseCaseExecutor
    ) = SettingsViewModel(
        GetSettingsUseCase(repository, coroutines),
        SetPeriodTrackingUseCase(repository, coroutines),
        SetThemeUseCase(repository, coroutines),
        SettingsPresentationMapper(ThemePresentationMapper()),
        ThemeDomainMapper(),
        useCaseExecutor
    )

    @Provides
    fun providesSettingsDependencies(settingsViewModel: SettingsViewModel): SettingsDependencies {
        val presenter = SettingsEventPresenter()
        return SettingsDependencies(
            settingsViewModel = settingsViewModel,
            settingsViewStateUiMapper = SettingsViewStateUiMapper(ThemeUiMapper()),
            themeUiToPresentationMapper = ThemeUiToPresentationMapper(),
            settingsEventPresenter = presenter,
            navigationMapper = SettingsNavigationEventDestinationMapper(),
            notificationMapper = SettingsNotificationUiMapper(presenter)
        )
    }

    /** One for the app, so deleting all data survives a rotation (ARCHITECTURE section 13). */
    @Provides
    @Singleton
    fun providesDeleteAllDataViewModel(
        deleteAllDataRepository: DeleteAllDataRepository,
        coroutines: CoroutineContextProvider,
        useCaseExecutor: UseCaseExecutor
    ) = DeleteAllDataViewModel(
        DeleteAllDataUseCase(
            deleteAllDataRepository,
            deleteAllDataRepository,
            deleteAllDataRepository,
            deleteAllDataRepository,
            coroutines
        ),
        DeleteConfirmationResolver(),
        useCaseExecutor
    )

    @Provides
    @Singleton
    fun providesDeleteAllDataDependencies(
        deleteAllDataViewModel: DeleteAllDataViewModel,
        notificationDataSource: NotificationDataSource,
        @ApplicationContext context: Context
    ): DeleteAllDataDependencies {
        val presenter = DeleteAllDataEventPresenter()
        return DeleteAllDataDependencies(
            deleteAllDataViewModel = deleteAllDataViewModel,
            deleteAllDataViewStateUiMapper = DeleteAllDataViewStateUiMapper(),
            deleteConfirmationResolver = DeleteConfirmationResolver(),
            deleteProgressUiResolver = DeleteProgressUiResolver(),
            deleteAllDataEventPresenter = presenter,
            navigationMapper = DeleteAllDataNavigationEventDestinationMapper {
                // A reminder that ran while everything was deleted may have posted after the cancel step.
                notificationDataSource.cancelAll()
                restartApp(context)
            },
            notificationMapper = DeleteAllDataNotificationUiMapper(presenter)
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
