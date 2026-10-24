package com.migrely.app.di

import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.medication.datasource.MedicationDataSource
import com.migrely.app.medication.data.mapper.AcuteIntakeDomainMapper
import com.migrely.app.medication.data.mapper.IntakeEffectDataMapper
import com.migrely.app.medication.data.mapper.LinkedAttackDomainMapper
import com.migrely.app.medication.data.mapper.MedicationCategoryDataMapper
import com.migrely.app.medication.data.mapper.MedicationDomainMapper
import com.migrely.app.medication.data.mapper.MedicationKindDataMapper
import com.migrely.app.medication.data.mapper.OveruseDataMapper
import com.migrely.app.medication.data.mapper.OveruseNoticeDomainMapper
import com.migrely.app.medication.data.mapper.PendingEffectQuestionDomainMapper
import com.migrely.app.medication.data.mapper.ZoneDomainMapper
import com.migrely.app.medication.data.repository.MedicationPromptRepository
import com.migrely.app.medication.data.repository.MedicationRepository
import com.migrely.app.medication.domain.repository.AcknowledgeOveruseRepository
import com.migrely.app.medication.domain.repository.AddIntakeRepository
import com.migrely.app.medication.domain.repository.CloseEffectQuestionRepository
import com.migrely.app.medication.domain.repository.CountActiveMedicationsRepository
import com.migrely.app.medication.domain.repository.CreateMedicationRepository
import com.migrely.app.medication.domain.repository.DeleteIntakeRepository
import com.migrely.app.medication.domain.repository.FindAttackAtRepository
import com.migrely.app.medication.domain.repository.FindMedicationByNameRepository
import com.migrely.app.medication.domain.repository.GetActiveMedicationsRepository
import com.migrely.app.medication.domain.repository.GetAcuteIntakesRepository
import com.migrely.app.medication.domain.repository.GetAttackRepository
import com.migrely.app.medication.domain.repository.GetCurrentTimeRepository
import com.migrely.app.medication.domain.repository.GetMedicationRepository
import com.migrely.app.medication.domain.repository.GetOveruseNoticesRepository
import com.migrely.app.medication.domain.repository.GetPendingEffectQuestionRepository
import com.migrely.app.medication.domain.repository.SetIntakeEffectRepository
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
        medicationCategoryDataMapper = MedicationCategoryDataMapper(),
        linkedAttackDomainMapper = LinkedAttackDomainMapper()
    )

    @Provides
    fun providesMedicationPromptRepository(
        medicationDataSource: MedicationDataSource
    ): MedicationPromptRepository {
        val zoneDomainMapper = ZoneDomainMapper()
        return MedicationPromptRepository(
            medicationDataSource = medicationDataSource,
            pendingEffectQuestionDomainMapper = PendingEffectQuestionDomainMapper(zoneDomainMapper),
            acuteIntakeDomainMapper = AcuteIntakeDomainMapper(zoneDomainMapper),
            overuseNoticeDomainMapper = OveruseNoticeDomainMapper(),
            intakeEffectDataMapper = IntakeEffectDataMapper(),
            overuseDataMapper = OveruseDataMapper()
        )
    }

    @Provides
    fun providesGetPendingEffectQuestionRepository(
        repository: MedicationPromptRepository
    ): GetPendingEffectQuestionRepository = repository

    @Provides
    fun providesGetAcuteIntakesRepository(
        repository: MedicationPromptRepository
    ): GetAcuteIntakesRepository = repository

    @Provides
    fun providesGetOveruseNoticesRepository(
        repository: MedicationPromptRepository
    ): GetOveruseNoticesRepository = repository

    @Provides
    fun providesSetIntakeEffectRepository(
        repository: MedicationPromptRepository
    ): SetIntakeEffectRepository = repository

    @Provides
    fun providesCloseEffectQuestionRepository(
        repository: MedicationPromptRepository
    ): CloseEffectQuestionRepository = repository

    @Provides
    fun providesAcknowledgeOveruseRepository(
        repository: MedicationPromptRepository
    ): AcknowledgeOveruseRepository = repository

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
