package com.migrely.app.di

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.history.domain.repository.GetAttackMedicationIntakesRepository
import com.migrely.app.history.domain.repository.GetAttacksTouchingRepository
import com.migrely.app.history.domain.repository.GetCurrentTimeRepository
import com.migrely.app.history.domain.repository.GetDayMarksRepository
import com.migrely.app.history.domain.repository.GetEarliestAttackRepository
import com.migrely.app.history.domain.repository.GetEarliestDayMarkRepository
import com.migrely.app.history.domain.repository.GetRecentlyDeletedAttackRepository
import com.migrely.app.history.domain.repository.RestoreDeletedAttackRepository
import com.migrely.app.history.domain.repository.SetDayMarkRepository
import com.migrely.app.history.domain.usecase.GetHistoryMonthUseCase
import com.migrely.app.history.domain.usecase.RestoreDeletedAttackUseCase
import com.migrely.app.history.domain.usecase.SetDayMarkUseCase
import com.migrely.app.history.presentation.mapper.HistoryAttackPresentationMapper
import com.migrely.app.history.presentation.mapper.HistoryMonthPresentationMapper
import com.migrely.app.history.presentation.mapper.IntensityLevelPresentationMapper
import com.migrely.app.history.presentation.mapper.MonthStatsPresentationMapper
import com.migrely.app.history.presentation.mapper.PainLevelPresentationMapper
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
        getDayMarksRepository: GetDayMarksRepository,
        getEarliestDayMarkRepository: GetEarliestDayMarkRepository,
        getAttackMedicationIntakesRepository: GetAttackMedicationIntakesRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = GetHistoryMonthUseCase(
        getCurrentTimeRepository,
        getEarliestAttackRepository,
        getAttacksTouchingRepository,
        getRecentlyDeletedAttackRepository,
        getDayMarksRepository,
        getEarliestDayMarkRepository,
        getAttackMedicationIntakesRepository,
        coroutineContextProvider
    )

    @Provides
    fun providesSetDayMarkUseCase(
        getCurrentTimeRepository: GetCurrentTimeRepository,
        getAttacksTouchingRepository: GetAttacksTouchingRepository,
        setDayMarkRepository: SetDayMarkRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = SetDayMarkUseCase(
        getCurrentTimeRepository,
        getAttacksTouchingRepository,
        setDayMarkRepository,
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
        IntensityLevelPresentationMapper(PainLevelPresentationMapper()),
        MonthStatsPresentationMapper(PainLevelPresentationMapper())
    )

    @Provides
    fun providesHistoryViewModel(
        getHistoryMonthUseCase: GetHistoryMonthUseCase,
        restoreDeletedAttackUseCase: RestoreDeletedAttackUseCase,
        setDayMarkUseCase: SetDayMarkUseCase,
        historyMonthPresentationMapper: HistoryMonthPresentationMapper,
        useCaseExecutor: UseCaseExecutor
    ) = HistoryViewModel(
        getHistoryMonthUseCase,
        restoreDeletedAttackUseCase,
        setDayMarkUseCase,
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
