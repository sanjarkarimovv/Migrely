package com.migrely.app.di

import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.medication.datasource.MedicationDataSource
import com.migrely.app.medication.data.mapper.MedicationCategoryDataMapper
import com.migrely.app.medication.data.mapper.MedicationDomainMapper
import com.migrely.app.medication.data.mapper.MedicationKindDataMapper
import com.migrely.app.medication.data.repository.MedicationRepository
import com.migrely.app.medication.domain.repository.AddIntakeRepository
import com.migrely.app.medication.domain.repository.CountActiveMedicationsRepository
import com.migrely.app.medication.domain.repository.CreateMedicationRepository
import com.migrely.app.medication.domain.repository.DeleteIntakeRepository
import com.migrely.app.medication.domain.repository.FindAttackAtRepository
import com.migrely.app.medication.domain.repository.FindMedicationByNameRepository
import com.migrely.app.medication.domain.repository.GetActiveMedicationsRepository
import com.migrely.app.medication.domain.repository.GetAttackRepository
import com.migrely.app.medication.domain.repository.GetCurrentTimeRepository
import com.migrely.app.medication.domain.repository.GetMedicationRepository
import com.migrely.app.medication.domain.repository.SetMedicationArchivedRepository
import com.migrely.app.medication.domain.repository.UpdateMedicationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
@Suppress("TooManyFunctions")
internal object MedicationDataModule {
    @Provides
    fun providesMedicationRepository(
        medicationDataSource: MedicationDataSource,
        attackDataSource: AttackDataSource,
        clockDataSource: ClockDataSource
    ) = MedicationRepository(
        medicationDataSource = medicationDataSource,
        attackDataSource = attackDataSource,
        clockDataSource = clockDataSource,
        medicationDomainMapper = MedicationDomainMapper(),
        medicationKindDataMapper = MedicationKindDataMapper(),
        medicationCategoryDataMapper = MedicationCategoryDataMapper()
    )

    @Provides
    fun providesGetCurrentTimeRepository(
        repository: MedicationRepository
    ): GetCurrentTimeRepository = repository

    @Provides
    fun providesGetActiveMedicationsRepository(
        repository: MedicationRepository
    ): GetActiveMedicationsRepository = repository

    @Provides
    fun providesGetMedicationRepository(repository: MedicationRepository): GetMedicationRepository =
        repository

    @Provides
    fun providesFindMedicationByNameRepository(
        repository: MedicationRepository
    ): FindMedicationByNameRepository = repository

    @Provides
    fun providesCountActiveMedicationsRepository(
        repository: MedicationRepository
    ): CountActiveMedicationsRepository = repository

    @Provides
    fun providesCreateMedicationRepository(
        repository: MedicationRepository
    ): CreateMedicationRepository = repository

    @Provides
    fun providesUpdateMedicationRepository(
        repository: MedicationRepository
    ): UpdateMedicationRepository = repository

    @Provides
    fun providesSetMedicationArchivedRepository(
        repository: MedicationRepository
    ): SetMedicationArchivedRepository = repository

    @Provides
    fun providesGetAttackRepository(repository: MedicationRepository): GetAttackRepository =
        repository

    @Provides
    fun providesFindAttackAtRepository(repository: MedicationRepository): FindAttackAtRepository =
        repository

    @Provides
    fun providesAddIntakeRepository(repository: MedicationRepository): AddIntakeRepository =
        repository

    @Provides
    fun providesDeleteIntakeRepository(repository: MedicationRepository): DeleteIntakeRepository =
        repository
}
