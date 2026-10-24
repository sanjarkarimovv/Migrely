package com.migrely.app.di

import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.daymark.datasource.DayMarkDataSource
import com.migrely.app.datasource.medication.datasource.MedicationDataSource
import com.migrely.app.history.data.mapper.AttackMedicationIntakeDomainMapper
import com.migrely.app.history.data.mapper.DayMarkDomainMapper
import com.migrely.app.history.data.mapper.HistoryAttackDomainMapper
import com.migrely.app.history.data.mapper.ZoneDomainMapper
import com.migrely.app.history.data.repository.HistoryRepository
import com.migrely.app.history.domain.repository.GetAttackMedicationIntakesRepository
import com.migrely.app.history.domain.repository.GetAttacksTouchingRepository
import com.migrely.app.history.domain.repository.GetCurrentTimeRepository
import com.migrely.app.history.domain.repository.GetDayMarksRepository
import com.migrely.app.history.domain.repository.GetEarliestAttackRepository
import com.migrely.app.history.domain.repository.GetEarliestDayMarkRepository
import com.migrely.app.history.domain.repository.GetRecentlyDeletedAttackRepository
import com.migrely.app.history.domain.repository.RestoreDeletedAttackRepository
import com.migrely.app.history.domain.repository.SetDayMarkRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object HistoryDataModule {
    @Provides
    fun providesHistoryRepository(
        attackDataSource: AttackDataSource,
        clockDataSource: ClockDataSource,
        dayMarkDataSource: DayMarkDataSource,
        medicationDataSource: MedicationDataSource
    ): HistoryRepository = HistoryRepository(
        attackDataSource = attackDataSource,
        clockDataSource = clockDataSource,
        dayMarkDataSource = dayMarkDataSource,
        medicationDataSource = medicationDataSource,
        historyAttackDomainMapper = HistoryAttackDomainMapper(ZoneDomainMapper()),
        zoneDomainMapper = ZoneDomainMapper(),
        dayMarkDomainMapper = DayMarkDomainMapper(),
        attackMedicationIntakeDomainMapper = AttackMedicationIntakeDomainMapper(ZoneDomainMapper())
    )

    @Provides
    fun providesGetAttackMedicationIntakesRepository(
        repository: HistoryRepository
    ): GetAttackMedicationIntakesRepository = repository

    @Provides
    fun providesGetDayMarksRepository(repository: HistoryRepository): GetDayMarksRepository =
        repository

    @Provides
    fun providesSetDayMarkRepository(repository: HistoryRepository): SetDayMarkRepository =
        repository

    @Provides
    fun providesGetEarliestDayMarkRepository(
        repository: HistoryRepository
    ): GetEarliestDayMarkRepository = repository

    @Provides
    fun providesGetCurrentTimeRepository(repository: HistoryRepository): GetCurrentTimeRepository =
        repository

    @Provides
    fun providesGetEarliestAttackRepository(
        repository: HistoryRepository
    ): GetEarliestAttackRepository = repository

    @Provides
    fun providesGetAttacksTouchingRepository(
        repository: HistoryRepository
    ): GetAttacksTouchingRepository = repository

    @Provides
    fun providesGetRecentlyDeletedAttackRepository(
        repository: HistoryRepository
    ): GetRecentlyDeletedAttackRepository = repository

    @Provides
    fun providesRestoreDeletedAttackRepository(
        repository: HistoryRepository
    ): RestoreDeletedAttackRepository = repository
}
