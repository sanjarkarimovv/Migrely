package com.migrely.app.di

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.history.domain.repository.GetAttacksTouchingRepository
import com.migrely.app.history.domain.repository.GetCurrentTimeRepository
import com.migrely.app.history.domain.repository.GetEarliestAttackRepository
import com.migrely.app.history.domain.repository.GetRecentlyDeletedAttackRepository
import com.migrely.app.history.domain.repository.RestoreDeletedAttackRepository
import com.migrely.app.history.domain.usecase.GetHistoryMonthUseCase
import com.migrely.app.history.domain.usecase.RestoreDeletedAttackUseCase
import com.migrely.app.history.presentation.mapper.HistoryAttackPresentationMapper
import com.migrely.app.history.presentation.mapper.HistoryMonthPresentationMapper
import com.migrely.app.history.presentation.mapper.IntensityLevelPresentationMapper
import com.migrely.app.history.presentation.viewmodel.HistoryViewModel
import com.migrely.app.history.presentation.viewmodel.MonthSkyViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object HistoryModule {
    @Provides
    fun providesGetHistoryMonthUseCase(
        getCurrentTimeRepository: GetCurrentTimeRepository,
        getEarliestAttackRepository: GetEarliestAttackRepository,
        getAttacksTouchingRepository: GetAttacksTouchingRepository,
        getRecentlyDeletedAttackRepository: GetRecentlyDeletedAttackRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = GetHistoryMonthUseCase(
        getCurrentTimeRepository,
        getEarliestAttackRepository,
        getAttacksTouchingRepository,
        getRecentlyDeletedAttackRepository,
        coroutineContextProvider
    )

    @Provides
    fun providesRestoreDeletedAttackUseCase(
        repository: RestoreDeletedAttackRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = RestoreDeletedAttackUseCase(repository, coroutineContextProvider)

    @Provides
    fun providesHistoryMonthPresentationMapper() = HistoryMonthPresentationMapper(
        HistoryAttackPresentationMapper(),
        IntensityLevelPresentationMapper()
    )

    @Provides
    fun providesHistoryViewModel(
        getHistoryMonthUseCase: GetHistoryMonthUseCase,
        restoreDeletedAttackUseCase: RestoreDeletedAttackUseCase,
        historyMonthPresentationMapper: HistoryMonthPresentationMapper,
        useCaseExecutor: UseCaseExecutor
    ) = HistoryViewModel(
        getHistoryMonthUseCase,
        restoreDeletedAttackUseCase,
        historyMonthPresentationMapper,
        useCaseExecutor
    )

    @Provides
    fun providesMonthSkyViewModel(
        getHistoryMonthUseCase: GetHistoryMonthUseCase,
        historyMonthPresentationMapper: HistoryMonthPresentationMapper,
        useCaseExecutor: UseCaseExecutor
    ) = MonthSkyViewModel(getHistoryMonthUseCase, historyMonthPresentationMapper, useCaseExecutor)
}
