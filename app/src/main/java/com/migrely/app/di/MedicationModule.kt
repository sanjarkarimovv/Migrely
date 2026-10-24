package com.migrely.app.di

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.coroutine.CoroutineContextProvider
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
import com.migrely.app.medication.domain.usecase.ArchiveMedicationUseCase
import com.migrely.app.medication.domain.usecase.GetMedicationLogUseCase
import com.migrely.app.medication.domain.usecase.GetMedicationUseCase
import com.migrely.app.medication.domain.usecase.GetMedicationsUseCase
import com.migrely.app.medication.domain.usecase.LogIntakeUseCase
import com.migrely.app.medication.domain.usecase.RestoreMedicationUseCase
import com.migrely.app.medication.domain.usecase.SaveMedicationUseCase
import com.migrely.app.medication.domain.usecase.UndoIntakeUseCase
import com.migrely.app.medication.presentation.mapper.IntakePresentationMapper
import com.migrely.app.medication.presentation.mapper.MedicationDomainMapper
import com.migrely.app.medication.presentation.mapper.MedicationPresentationMapper
import com.migrely.app.medication.presentation.mapper.SaveProblemPresentationMapper
import com.migrely.app.medication.presentation.viewmodel.EditMedicationViewModel
import com.migrely.app.medication.presentation.viewmodel.LogMedicationViewModel
import com.migrely.app.medication.presentation.viewmodel.MedicationsViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
@Suppress("TooManyFunctions", "LongParameterList")
internal object MedicationModule {
    @Provides
    fun providesGetMedicationsUseCase(
        repository: GetActiveMedicationsRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = GetMedicationsUseCase(repository, coroutineContextProvider)

    @Provides
    fun providesGetMedicationUseCase(
        repository: GetMedicationRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = GetMedicationUseCase(repository, coroutineContextProvider)

    @Provides
    fun providesSaveMedicationUseCase(
        findMedicationByNameRepository: FindMedicationByNameRepository,
        countActiveMedicationsRepository: CountActiveMedicationsRepository,
        createMedicationRepository: CreateMedicationRepository,
        updateMedicationRepository: UpdateMedicationRepository,
        setMedicationArchivedRepository: SetMedicationArchivedRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = SaveMedicationUseCase(
        findMedicationByNameRepository,
        countActiveMedicationsRepository,
        createMedicationRepository,
        updateMedicationRepository,
        setMedicationArchivedRepository,
        coroutineContextProvider
    )

    @Provides
    fun providesArchiveMedicationUseCase(
        repository: SetMedicationArchivedRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = ArchiveMedicationUseCase(repository, coroutineContextProvider)

    @Provides
    fun providesRestoreMedicationUseCase(
        repository: SetMedicationArchivedRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = RestoreMedicationUseCase(repository, coroutineContextProvider)

    @Provides
    fun providesGetMedicationLogUseCase(
        getCurrentTimeRepository: GetCurrentTimeRepository,
        getAttackRepository: GetAttackRepository,
        getActiveMedicationsRepository: GetActiveMedicationsRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = GetMedicationLogUseCase(
        getCurrentTimeRepository,
        getAttackRepository,
        getActiveMedicationsRepository,
        coroutineContextProvider
    )

    @Provides
    fun providesLogIntakeUseCase(
        getCurrentTimeRepository: GetCurrentTimeRepository,
        findAttackAtRepository: FindAttackAtRepository,
        addIntakeRepository: AddIntakeRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = LogIntakeUseCase(
        getCurrentTimeRepository,
        findAttackAtRepository,
        addIntakeRepository,
        coroutineContextProvider
    )

    @Provides
    fun providesUndoIntakeUseCase(
        repository: DeleteIntakeRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = UndoIntakeUseCase(repository, coroutineContextProvider)

    @Provides
    fun providesMedicationsViewModel(
        getMedicationsUseCase: GetMedicationsUseCase,
        archiveMedicationUseCase: ArchiveMedicationUseCase,
        restoreMedicationUseCase: RestoreMedicationUseCase,
        useCaseExecutor: UseCaseExecutor
    ) = MedicationsViewModel(
        getMedicationsUseCase,
        archiveMedicationUseCase,
        restoreMedicationUseCase,
        MedicationPresentationMapper(),
        useCaseExecutor
    )

    @Provides
    fun providesEditMedicationViewModel(
        getMedicationUseCase: GetMedicationUseCase,
        saveMedicationUseCase: SaveMedicationUseCase,
        useCaseExecutor: UseCaseExecutor
    ) = EditMedicationViewModel(
        getMedicationUseCase,
        saveMedicationUseCase,
        MedicationPresentationMapper(),
        MedicationDomainMapper(),
        SaveProblemPresentationMapper(),
        useCaseExecutor
    )

    @Provides
    fun providesLogMedicationViewModel(
        getMedicationLogUseCase: GetMedicationLogUseCase,
        logIntakeUseCase: LogIntakeUseCase,
        undoIntakeUseCase: UndoIntakeUseCase,
        saveMedicationUseCase: SaveMedicationUseCase,
        useCaseExecutor: UseCaseExecutor
    ) = LogMedicationViewModel(
        getMedicationLogUseCase,
        logIntakeUseCase,
        undoIntakeUseCase,
        saveMedicationUseCase,
        MedicationPresentationMapper(),
        MedicationDomainMapper(),
        IntakePresentationMapper(),
        SaveProblemPresentationMapper(),
        useCaseExecutor
    )
}
