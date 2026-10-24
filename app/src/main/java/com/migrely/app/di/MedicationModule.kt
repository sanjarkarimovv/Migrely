package com.migrely.app.di

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.datasource.notification.datasource.NotificationDataSource
import com.migrely.app.datasource.reminder.datasource.ReminderSettingsDataSource
import com.migrely.app.datasource.schedule.datasource.ReminderScheduleDataSource
import com.migrely.app.medication.data.repository.EffectReminderRepository
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
import com.migrely.app.medication.domain.usecase.AcknowledgeOveruseUseCase
import com.migrely.app.medication.domain.usecase.AnswerEffectUseCase
import com.migrely.app.medication.domain.usecase.ArchiveMedicationUseCase
import com.migrely.app.medication.domain.usecase.GetMedicationLogUseCase
import com.migrely.app.medication.domain.usecase.GetMedicationPromptUseCase
import com.migrely.app.medication.domain.usecase.GetMedicationUseCase
import com.migrely.app.medication.domain.usecase.GetMedicationsUseCase
import com.migrely.app.medication.domain.usecase.GetOveruseStatusUseCase
import com.migrely.app.medication.domain.usecase.LogIntakeUseCase
import com.migrely.app.medication.domain.usecase.RestoreMedicationUseCase
import com.migrely.app.medication.domain.usecase.RunEffectQuestionUseCase
import com.migrely.app.medication.domain.usecase.SaveMedicationUseCase
import com.migrely.app.medication.domain.usecase.SkipEffectQuestionUseCase
import com.migrely.app.medication.domain.usecase.UndoIntakeUseCase
import com.migrely.app.medication.presentation.mapper.IntakePresentationMapper
import com.migrely.app.medication.presentation.mapper.MedicationDomainMapper
import com.migrely.app.medication.presentation.mapper.MedicationLogPresentationMapper
import com.migrely.app.medication.presentation.mapper.MedicationPresentationMapper
import com.migrely.app.medication.presentation.mapper.MedicationPromptDomainMapper
import com.migrely.app.medication.presentation.mapper.MedicationPromptPresentationMapper
import com.migrely.app.medication.presentation.mapper.OveruseCategoryPresentationMapper
import com.migrely.app.medication.presentation.mapper.OveruseIndicatorPresentationMapper
import com.migrely.app.medication.presentation.mapper.SaveProblemPresentationMapper
import com.migrely.app.medication.presentation.viewmodel.EditMedicationViewModel
import com.migrely.app.medication.presentation.viewmodel.LogMedicationViewModel
import com.migrely.app.medication.presentation.viewmodel.MedicationPromptViewModel
import com.migrely.app.medication.presentation.viewmodel.MedicationsViewModel
import com.migrely.app.medication.presentation.viewmodel.OveruseIndicatorViewModel
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
        countActiveMedicationsRepository: CountActiveMedicationsRepository,
        setMedicationArchivedRepository: SetMedicationArchivedRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = RestoreMedicationUseCase(
        countActiveMedicationsRepository,
        setMedicationArchivedRepository,
        coroutineContextProvider
    )

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
        getAttackRepository: GetAttackRepository,
        findAttackAtRepository: FindAttackAtRepository,
        addIntakeRepository: AddIntakeRepository,
        effectReminderRepository: EffectReminderRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = LogIntakeUseCase(
        getCurrentTimeRepository,
        getAttackRepository,
        findAttackAtRepository,
        addIntakeRepository,
        effectReminderRepository,
        coroutineContextProvider
    )

    @Provides
    fun providesUndoIntakeUseCase(
        repository: DeleteIntakeRepository,
        effectReminderRepository: EffectReminderRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = UndoIntakeUseCase(repository, effectReminderRepository, coroutineContextProvider)

    @Provides
    fun providesGetMedicationPromptUseCase(
        getCurrentTimeRepository: GetCurrentTimeRepository,
        getAcuteIntakesRepository: GetAcuteIntakesRepository,
        getOveruseNoticesRepository: GetOveruseNoticesRepository,
        getPendingEffectQuestionRepository: GetPendingEffectQuestionRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = GetMedicationPromptUseCase(
        getCurrentTimeRepository,
        getAcuteIntakesRepository,
        getOveruseNoticesRepository,
        getPendingEffectQuestionRepository,
        coroutineContextProvider
    )

    @Provides
    fun providesAnswerEffectUseCase(
        repository: SetIntakeEffectRepository,
        effectReminderRepository: EffectReminderRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = AnswerEffectUseCase(repository, effectReminderRepository, coroutineContextProvider)

    @Provides
    fun providesSkipEffectQuestionUseCase(
        repository: CloseEffectQuestionRepository,
        effectReminderRepository: EffectReminderRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = SkipEffectQuestionUseCase(repository, effectReminderRepository, coroutineContextProvider)

    @Provides
    fun providesEffectReminderRepository(
        reminderScheduleDataSource: ReminderScheduleDataSource,
        notificationDataSource: NotificationDataSource,
        reminderSettingsDataSource: ReminderSettingsDataSource
    ) = EffectReminderRepository(
        reminderScheduleDataSource,
        notificationDataSource,
        reminderSettingsDataSource
    )

    @Provides
    fun providesRunEffectQuestionUseCase(
        effectReminderRepository: EffectReminderRepository,
        getCurrentTimeRepository: GetCurrentTimeRepository,
        getPendingEffectQuestionRepository: GetPendingEffectQuestionRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = RunEffectQuestionUseCase(
        effectReminderRepository,
        effectReminderRepository,
        getCurrentTimeRepository,
        getPendingEffectQuestionRepository,
        effectReminderRepository,
        coroutineContextProvider
    )

    @Provides
    fun providesAcknowledgeOveruseUseCase(
        getCurrentTimeRepository: GetCurrentTimeRepository,
        repository: AcknowledgeOveruseRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = AcknowledgeOveruseUseCase(getCurrentTimeRepository, repository, coroutineContextProvider)

    @Provides
    fun providesGetOveruseStatusUseCase(
        getCurrentTimeRepository: GetCurrentTimeRepository,
        getAcuteIntakesRepository: GetAcuteIntakesRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = GetOveruseStatusUseCase(
        getCurrentTimeRepository,
        getAcuteIntakesRepository,
        coroutineContextProvider
    )

    @Provides
    fun providesOveruseIndicatorViewModel(
        getOveruseStatusUseCase: GetOveruseStatusUseCase,
        useCaseExecutor: UseCaseExecutor
    ) = OveruseIndicatorViewModel(
        getOveruseStatusUseCase,
        OveruseIndicatorPresentationMapper(OveruseCategoryPresentationMapper()),
        useCaseExecutor
    )

    @Provides
    fun providesMedicationPromptViewModel(
        getMedicationPromptUseCase: GetMedicationPromptUseCase,
        answerEffectUseCase: AnswerEffectUseCase,
        skipEffectQuestionUseCase: SkipEffectQuestionUseCase,
        acknowledgeOveruseUseCase: AcknowledgeOveruseUseCase,
        useCaseExecutor: UseCaseExecutor
    ) = MedicationPromptViewModel(
        getMedicationPromptUseCase,
        answerEffectUseCase,
        skipEffectQuestionUseCase,
        acknowledgeOveruseUseCase,
        MedicationPromptPresentationMapper(OveruseCategoryPresentationMapper()),
        MedicationPromptDomainMapper(),
        useCaseExecutor
    )

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
        MedicationLogPresentationMapper(MedicationPresentationMapper()),
        MedicationDomainMapper(),
        IntakePresentationMapper(),
        SaveProblemPresentationMapper(),
        useCaseExecutor
    )
}
