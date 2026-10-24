package com.migrely.app.di

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.datasource.backup.datasource.BackupDataSource
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSource
import com.migrely.app.insights.data.mapper.InsightsRecordsDomainMapper
import com.migrely.app.insights.data.repository.InsightsRepository
import com.migrely.app.insights.domain.usecase.GetInsightsUseCase
import com.migrely.app.insights.presentation.mapper.InsightsPresentationMapper
import com.migrely.app.insights.presentation.viewmodel.InsightsViewModel
import com.migrely.app.insights.ui.di.InsightsDependencies
import com.migrely.app.insights.ui.mapper.InsightsNotificationUiMapper
import com.migrely.app.insights.ui.mapper.InsightsViewStateUiMapper
import com.migrely.app.navigation.mapper.InsightsNavigationEventDestinationMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Insights (change add-trigger-insights, BRD D87–D92). One repository serves every repository interface. */
@Module
@InstallIn(SingletonComponent::class)
internal object InsightsModule {
    @Provides
    fun providesInsightsRepository(
        backupDataSource: BackupDataSource,
        preferencesDataSource: PreferencesDataSource,
        clockDataSource: ClockDataSource
    ) = InsightsRepository(
        backupDataSource,
        preferencesDataSource,
        clockDataSource,
        InsightsRecordsDomainMapper()
    )

    @Provides
    fun providesInsightsViewModel(
        repository: InsightsRepository,
        coroutines: CoroutineContextProvider,
        useCaseExecutor: UseCaseExecutor
    ) = InsightsViewModel(
        GetInsightsUseCase(repository, repository, repository, coroutines),
        InsightsPresentationMapper(),
        useCaseExecutor
    )

    @Provides
    fun providesInsightsDependencies(insightsViewModel: InsightsViewModel) = InsightsDependencies(
        insightsViewModel = insightsViewModel,
        insightsViewStateUiMapper = InsightsViewStateUiMapper(),
        navigationMapper = InsightsNavigationEventDestinationMapper(),
        notificationMapper = InsightsNotificationUiMapper()
    )
}
