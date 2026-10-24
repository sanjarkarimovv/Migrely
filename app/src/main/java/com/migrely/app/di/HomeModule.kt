package com.migrely.app.di

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.home.domain.repository.AddIntensityPointRepository
import com.migrely.app.home.domain.repository.DeleteAttackRepository
import com.migrely.app.home.domain.repository.EndAttackRepository
import com.migrely.app.home.domain.repository.GetAttackInProgressRepository
import com.migrely.app.home.domain.repository.GetCurrentTimeRepository
import com.migrely.app.home.domain.repository.GetDayMarksRepository
import com.migrely.app.home.domain.repository.GetPreviousAttackEndRepository
import com.migrely.app.home.domain.repository.GetTodayRepository
import com.migrely.app.home.domain.repository.ReopenAttackRepository
import com.migrely.app.home.domain.repository.ReplaceLatestIntensityPointRepository
import com.migrely.app.home.domain.repository.RestoreDeletedAttackRepository
import com.migrely.app.home.domain.repository.SetPeriodRepository
import com.migrely.app.home.domain.repository.StartAttackRepository
import com.migrely.app.home.domain.repository.UpdateAttackStartRepository
import com.migrely.app.home.domain.repository.UpdateLongAttackCheckRepository
import com.migrely.app.home.domain.usecase.AnswerLongAttackCheckUseCase
import com.migrely.app.home.domain.usecase.EndAttackUseCase
import com.migrely.app.home.domain.usecase.GetTodayUseCase
import com.migrely.app.home.domain.usecase.MoveAttackStartEarlierUseCase
import com.migrely.app.home.domain.usecase.RecordIntensityUseCase
import com.migrely.app.home.domain.usecase.RestoreDeletedAttackUseCase
import com.migrely.app.home.domain.usecase.SetPeriodTodayUseCase
import com.migrely.app.home.domain.usecase.StartAttackUseCase
import com.migrely.app.home.domain.usecase.UndoAttackEndUseCase
import com.migrely.app.home.domain.usecase.UndoAttackStartUseCase
import com.migrely.app.home.presentation.mapper.IntensityLevelDomainMapper
import com.migrely.app.home.presentation.mapper.IntensityLevelPresentationMapper
import com.migrely.app.home.presentation.mapper.TodayPresentationMapper
import com.migrely.app.home.presentation.viewmodel.HomeViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
@Suppress("TooManyFunctions", "LongParameterList")
internal object HomeModule {
    @Provides
    fun providesGetTodayUseCase(
        getCurrentTimeRepository: GetCurrentTimeRepository,
        repository: GetTodayRepository,
        getDayMarksRepository: GetDayMarksRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = GetTodayUseCase(
        getCurrentTimeRepository,
        repository,
        getDayMarksRepository,
        coroutineContextProvider
    )

    @Provides
    fun providesSetPeriodTodayUseCase(
        getCurrentTimeRepository: GetCurrentTimeRepository,
        setPeriodRepository: SetPeriodRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = SetPeriodTodayUseCase(
        getCurrentTimeRepository,
        setPeriodRepository,
        coroutineContextProvider
    )

    @Provides
    fun providesStartAttackUseCase(
        getCurrentTimeRepository: GetCurrentTimeRepository,
        startAttackRepository: StartAttackRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = StartAttackUseCase(
        getCurrentTimeRepository,
        startAttackRepository,
        coroutineContextProvider
    )

    @Provides
    fun providesRecordIntensityUseCase(
        getCurrentTimeRepository: GetCurrentTimeRepository,
        getAttackInProgressRepository: GetAttackInProgressRepository,
        addIntensityPointRepository: AddIntensityPointRepository,
        replaceLatestIntensityPointRepository: ReplaceLatestIntensityPointRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = RecordIntensityUseCase(
        getCurrentTimeRepository,
        getAttackInProgressRepository,
        addIntensityPointRepository,
        replaceLatestIntensityPointRepository,
        coroutineContextProvider
    )

    @Provides
    fun providesMoveAttackStartEarlierUseCase(
        getAttackInProgressRepository: GetAttackInProgressRepository,
        getPreviousAttackEndRepository: GetPreviousAttackEndRepository,
        updateAttackStartRepository: UpdateAttackStartRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = MoveAttackStartEarlierUseCase(
        getAttackInProgressRepository,
        getPreviousAttackEndRepository,
        updateAttackStartRepository,
        coroutineContextProvider
    )

    @Provides
    fun providesEndAttackUseCase(
        getCurrentTimeRepository: GetCurrentTimeRepository,
        getAttackInProgressRepository: GetAttackInProgressRepository,
        endAttackRepository: EndAttackRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = EndAttackUseCase(
        getCurrentTimeRepository,
        getAttackInProgressRepository,
        endAttackRepository,
        coroutineContextProvider
    )

    @Provides
    fun providesUndoAttackStartUseCase(
        getAttackInProgressRepository: GetAttackInProgressRepository,
        deleteAttackRepository: DeleteAttackRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = UndoAttackStartUseCase(
        getAttackInProgressRepository,
        deleteAttackRepository,
        coroutineContextProvider
    )

    @Provides
    fun providesUndoAttackEndUseCase(
        reopenAttackRepository: ReopenAttackRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = UndoAttackEndUseCase(reopenAttackRepository, coroutineContextProvider)

    @Provides
    fun providesAnswerLongAttackCheckUseCase(
        getCurrentTimeRepository: GetCurrentTimeRepository,
        getAttackInProgressRepository: GetAttackInProgressRepository,
        updateLongAttackCheckRepository: UpdateLongAttackCheckRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = AnswerLongAttackCheckUseCase(
        getCurrentTimeRepository,
        getAttackInProgressRepository,
        updateLongAttackCheckRepository,
        coroutineContextProvider
    )

    @Provides
    fun providesRestoreDeletedAttackUseCase(
        restoreDeletedAttackRepository: RestoreDeletedAttackRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = RestoreDeletedAttackUseCase(restoreDeletedAttackRepository, coroutineContextProvider)

    @Provides
    fun providesTodayPresentationMapper() =
        TodayPresentationMapper(IntensityLevelPresentationMapper())

    @Provides
    fun providesIntensityLevelDomainMapper() = IntensityLevelDomainMapper()

    @Provides
    fun providesHomeViewModel(
        getTodayUseCase: GetTodayUseCase,
        startAttackUseCase: StartAttackUseCase,
        recordIntensityUseCase: RecordIntensityUseCase,
        moveAttackStartEarlierUseCase: MoveAttackStartEarlierUseCase,
        endAttackUseCase: EndAttackUseCase,
        undoAttackStartUseCase: UndoAttackStartUseCase,
        undoAttackEndUseCase: UndoAttackEndUseCase,
        answerLongAttackCheckUseCase: AnswerLongAttackCheckUseCase,
        restoreDeletedAttackUseCase: RestoreDeletedAttackUseCase,
        setPeriodTodayUseCase: SetPeriodTodayUseCase,
        todayPresentationMapper: TodayPresentationMapper,
        intensityLevelDomainMapper: IntensityLevelDomainMapper,
        useCaseExecutor: UseCaseExecutor
    ) = HomeViewModel(
        getTodayUseCase = getTodayUseCase,
        startAttackUseCase = startAttackUseCase,
        recordIntensityUseCase = recordIntensityUseCase,
        moveAttackStartEarlierUseCase = moveAttackStartEarlierUseCase,
        endAttackUseCase = endAttackUseCase,
        undoAttackStartUseCase = undoAttackStartUseCase,
        undoAttackEndUseCase = undoAttackEndUseCase,
        answerLongAttackCheckUseCase = answerLongAttackCheckUseCase,
        restoreDeletedAttackUseCase = restoreDeletedAttackUseCase,
        setPeriodTodayUseCase = setPeriodTodayUseCase,
        todayPresentationMapper = todayPresentationMapper,
        intensityLevelDomainMapper = intensityLevelDomainMapper,
        useCaseExecutor = useCaseExecutor
    )
}
