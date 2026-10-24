package com.migrely.app.di

import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.daymark.datasource.DayMarkDataSource
import com.migrely.app.home.data.mapper.AttackDomainMapper
import com.migrely.app.home.data.mapper.IntensityPointDomainMapper
import com.migrely.app.home.data.repository.HomeRepository
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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
@Suppress("TooManyFunctions")
internal object HomeDataModule {
    @Provides
    fun providesHomeRepository(
        attackDataSource: AttackDataSource,
        clockDataSource: ClockDataSource,
        dayMarkDataSource: DayMarkDataSource
    ): HomeRepository = HomeRepository(
        attackDataSource = attackDataSource,
        clockDataSource = clockDataSource,
        dayMarkDataSource = dayMarkDataSource,
        attackDomainMapper = AttackDomainMapper(IntensityPointDomainMapper())
    )

    @Provides
    fun providesGetDayMarksRepository(repository: HomeRepository): GetDayMarksRepository =
        repository

    @Provides
    fun providesSetPeriodRepository(repository: HomeRepository): SetPeriodRepository = repository

    @Provides
    fun providesGetTodayRepository(repository: HomeRepository): GetTodayRepository = repository

    @Provides
    fun providesGetCurrentTimeRepository(repository: HomeRepository): GetCurrentTimeRepository =
        repository

    @Provides
    fun providesStartAttackRepository(repository: HomeRepository): StartAttackRepository =
        repository

    @Provides
    fun providesGetAttackInProgressRepository(
        repository: HomeRepository
    ): GetAttackInProgressRepository = repository

    @Provides
    fun providesAddIntensityPointRepository(
        repository: HomeRepository
    ): AddIntensityPointRepository = repository

    @Provides
    fun providesReplaceLatestIntensityPointRepository(
        repository: HomeRepository
    ): ReplaceLatestIntensityPointRepository = repository

    @Provides
    fun providesGetPreviousAttackEndRepository(
        repository: HomeRepository
    ): GetPreviousAttackEndRepository = repository

    @Provides
    fun providesUpdateAttackStartRepository(
        repository: HomeRepository
    ): UpdateAttackStartRepository = repository

    @Provides
    fun providesEndAttackRepository(repository: HomeRepository): EndAttackRepository = repository

    @Provides
    fun providesDeleteAttackRepository(repository: HomeRepository): DeleteAttackRepository =
        repository

    @Provides
    fun providesReopenAttackRepository(repository: HomeRepository): ReopenAttackRepository =
        repository

    @Provides
    fun providesUpdateLongAttackCheckRepository(
        repository: HomeRepository
    ): UpdateLongAttackCheckRepository = repository

    @Provides
    fun providesRestoreDeletedAttackRepository(
        repository: HomeRepository
    ): RestoreDeletedAttackRepository = repository
}
