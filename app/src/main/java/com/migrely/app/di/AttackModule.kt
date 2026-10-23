package com.migrely.app.di

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.attack.domain.repository.AddIntensityPointRepository
import com.migrely.app.attack.domain.repository.CountActiveCustomTagsRepository
import com.migrely.app.attack.domain.repository.CreateCustomTagRepository
import com.migrely.app.attack.domain.repository.DeleteAttackRepository
import com.migrely.app.attack.domain.repository.FindCustomTagRepository
import com.migrely.app.attack.domain.repository.FindOverlappingAttacksRepository
import com.migrely.app.attack.domain.repository.GetActiveCustomTagsRepository
import com.migrely.app.attack.domain.repository.GetAttackDetailsRepository
import com.migrely.app.attack.domain.repository.GetCurrentTimeRepository
import com.migrely.app.attack.domain.repository.GetMaximumRecordedRepository
import com.migrely.app.attack.domain.repository.MergeAttacksRepository
import com.migrely.app.attack.domain.repository.RemoveIntensityPointRepository
import com.migrely.app.attack.domain.repository.RestoreAttackTimesRepository
import com.migrely.app.attack.domain.repository.SelectAttackTagRepository
import com.migrely.app.attack.domain.repository.SetCustomTagArchivedRepository
import com.migrely.app.attack.domain.repository.SetFunctionalImpactRepository
import com.migrely.app.attack.domain.repository.StorePastAttackRepository
import com.migrely.app.attack.domain.repository.UpdateAttackNoteRepository
import com.migrely.app.attack.domain.repository.UpdateAttackTimesRepository
import com.migrely.app.attack.domain.usecase.AddCustomTagUseCase
import com.migrely.app.attack.domain.usecase.AddIntensityPointUseCase
import com.migrely.app.attack.domain.usecase.ArchiveCustomTagUseCase
import com.migrely.app.attack.domain.usecase.CreatePastAttackUseCase
import com.migrely.app.attack.domain.usecase.DeleteAttackUseCase
import com.migrely.app.attack.domain.usecase.GetAttackDetailsUseCase
import com.migrely.app.attack.domain.usecase.GetPastAttackDraftUseCase
import com.migrely.app.attack.domain.usecase.MergeAttacksUseCase
import com.migrely.app.attack.domain.usecase.RemoveIntensityPointUseCase
import com.migrely.app.attack.domain.usecase.SelectAttackTagUseCase
import com.migrely.app.attack.domain.usecase.SetFunctionalImpactUseCase
import com.migrely.app.attack.domain.usecase.UndoTimesChangeUseCase
import com.migrely.app.attack.domain.usecase.UpdateAttackNoteUseCase
import com.migrely.app.attack.domain.usecase.UpdateAttackTimesUseCase
import com.migrely.app.attack.presentation.mapper.AttackDetailsPresentationMapper
import com.migrely.app.attack.presentation.mapper.AttackRangePresentationMapper
import com.migrely.app.attack.presentation.mapper.AttackTagDomainMapper
import com.migrely.app.attack.presentation.mapper.BuiltInSymptomDomainMapper
import com.migrely.app.attack.presentation.mapper.BuiltInSymptomPresentationMapper
import com.migrely.app.attack.presentation.mapper.BuiltInTriggerDomainMapper
import com.migrely.app.attack.presentation.mapper.BuiltInTriggerPresentationMapper
import com.migrely.app.attack.presentation.mapper.CustomTagDomainMapper
import com.migrely.app.attack.presentation.mapper.CustomTagPresentationMapper
import com.migrely.app.attack.presentation.mapper.FunctionalImpactDomainMapper
import com.migrely.app.attack.presentation.mapper.FunctionalImpactPresentationMapper
import com.migrely.app.attack.presentation.mapper.HeadLocationDomainMapper
import com.migrely.app.attack.presentation.mapper.HeadLocationPresentationMapper
import com.migrely.app.attack.presentation.mapper.IntensityPointDomainMapper
import com.migrely.app.attack.presentation.mapper.IntensityPointPresentationMapper
import com.migrely.app.attack.presentation.mapper.PastAttackDraftPresentationMapper
import com.migrely.app.attack.presentation.mapper.TagKindDomainMapper
import com.migrely.app.attack.presentation.mapper.TagKindPresentationMapper
import com.migrely.app.attack.presentation.mapper.TimeFieldPresentationMapper
import com.migrely.app.attack.presentation.viewmodel.AddPastAttackViewModel
import com.migrely.app.attack.presentation.viewmodel.AttackDetailsViewModel
import com.migrely.app.coroutine.CoroutineContextProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
@Suppress("TooManyFunctions", "LongParameterList")
internal object AttackModule {
    @Provides
    fun providesGetAttackDetailsUseCase(
        getAttackDetailsRepository: GetAttackDetailsRepository,
        getActiveCustomTagsRepository: GetActiveCustomTagsRepository,
        getCurrentTimeRepository: GetCurrentTimeRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = GetAttackDetailsUseCase(
        getAttackDetailsRepository,
        getActiveCustomTagsRepository,
        getCurrentTimeRepository,
        coroutineContextProvider
    )

    @Provides
    fun providesUpdateAttackTimesUseCase(
        getAttackDetailsRepository: GetAttackDetailsRepository,
        getCurrentTimeRepository: GetCurrentTimeRepository,
        findOverlappingAttacksRepository: FindOverlappingAttacksRepository,
        updateAttackTimesRepository: UpdateAttackTimesRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = UpdateAttackTimesUseCase(
        getAttackDetailsRepository,
        getCurrentTimeRepository,
        findOverlappingAttacksRepository,
        updateAttackTimesRepository,
        coroutineContextProvider
    )

    @Provides
    fun providesUndoTimesChangeUseCase(
        getAttackDetailsRepository: GetAttackDetailsRepository,
        getCurrentTimeRepository: GetCurrentTimeRepository,
        findOverlappingAttacksRepository: FindOverlappingAttacksRepository,
        restoreAttackTimesRepository: RestoreAttackTimesRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = UndoTimesChangeUseCase(
        getAttackDetailsRepository,
        getCurrentTimeRepository,
        findOverlappingAttacksRepository,
        restoreAttackTimesRepository,
        coroutineContextProvider
    )

    @Provides
    fun providesAddIntensityPointUseCase(
        getAttackDetailsRepository: GetAttackDetailsRepository,
        getCurrentTimeRepository: GetCurrentTimeRepository,
        getMaximumRecordedRepository: GetMaximumRecordedRepository,
        addIntensityPointRepository: AddIntensityPointRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = AddIntensityPointUseCase(
        getAttackDetailsRepository,
        getCurrentTimeRepository,
        getMaximumRecordedRepository,
        addIntensityPointRepository,
        coroutineContextProvider
    )

    @Provides
    fun providesRemoveIntensityPointUseCase(
        removeIntensityPointRepository: RemoveIntensityPointRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = RemoveIntensityPointUseCase(removeIntensityPointRepository, coroutineContextProvider)

    @Provides
    fun providesSelectAttackTagUseCase(
        selectAttackTagRepository: SelectAttackTagRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = SelectAttackTagUseCase(selectAttackTagRepository, coroutineContextProvider)

    @Provides
    fun providesSetFunctionalImpactUseCase(
        setFunctionalImpactRepository: SetFunctionalImpactRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = SetFunctionalImpactUseCase(setFunctionalImpactRepository, coroutineContextProvider)

    @Provides
    fun providesUpdateAttackNoteUseCase(
        updateAttackNoteRepository: UpdateAttackNoteRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = UpdateAttackNoteUseCase(updateAttackNoteRepository, coroutineContextProvider)

    @Provides
    fun providesAddCustomTagUseCase(
        findCustomTagRepository: FindCustomTagRepository,
        countActiveCustomTagsRepository: CountActiveCustomTagsRepository,
        createCustomTagRepository: CreateCustomTagRepository,
        setCustomTagArchivedRepository: SetCustomTagArchivedRepository,
        selectAttackTagRepository: SelectAttackTagRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = AddCustomTagUseCase(
        findCustomTagRepository,
        countActiveCustomTagsRepository,
        createCustomTagRepository,
        setCustomTagArchivedRepository,
        selectAttackTagRepository,
        coroutineContextProvider
    )

    @Provides
    fun providesArchiveCustomTagUseCase(
        setCustomTagArchivedRepository: SetCustomTagArchivedRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = ArchiveCustomTagUseCase(setCustomTagArchivedRepository, coroutineContextProvider)

    @Provides
    fun providesDeleteAttackUseCase(
        deleteAttackRepository: DeleteAttackRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = DeleteAttackUseCase(deleteAttackRepository, coroutineContextProvider)

    @Provides
    fun providesGetPastAttackDraftUseCase(
        getCurrentTimeRepository: GetCurrentTimeRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = GetPastAttackDraftUseCase(getCurrentTimeRepository, coroutineContextProvider)

    @Provides
    fun providesCreatePastAttackUseCase(
        getCurrentTimeRepository: GetCurrentTimeRepository,
        findOverlappingAttacksRepository: FindOverlappingAttacksRepository,
        storePastAttackRepository: StorePastAttackRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = CreatePastAttackUseCase(
        getCurrentTimeRepository,
        findOverlappingAttacksRepository,
        storePastAttackRepository,
        coroutineContextProvider
    )

    @Provides
    fun providesMergeAttacksUseCase(
        getAttackDetailsRepository: GetAttackDetailsRepository,
        getCurrentTimeRepository: GetCurrentTimeRepository,
        findOverlappingAttacksRepository: FindOverlappingAttacksRepository,
        storePastAttackRepository: StorePastAttackRepository,
        updateAttackTimesRepository: UpdateAttackTimesRepository,
        mergeAttacksRepository: MergeAttacksRepository,
        updateAttackNoteRepository: UpdateAttackNoteRepository,
        coroutineContextProvider: CoroutineContextProvider
    ) = MergeAttacksUseCase(
        getAttackDetailsRepository,
        getCurrentTimeRepository,
        findOverlappingAttacksRepository,
        storePastAttackRepository,
        updateAttackTimesRepository,
        mergeAttacksRepository,
        updateAttackNoteRepository,
        coroutineContextProvider
    )

    @Provides
    fun providesAddPastAttackViewModel(
        getPastAttackDraftUseCase: GetPastAttackDraftUseCase,
        createPastAttackUseCase: CreatePastAttackUseCase,
        mergeAttacksUseCase: MergeAttacksUseCase,
        useCaseExecutor: UseCaseExecutor
    ) = AddPastAttackViewModel(
        getPastAttackDraftUseCase = getPastAttackDraftUseCase,
        createPastAttackUseCase = createPastAttackUseCase,
        mergeAttacksUseCase = mergeAttacksUseCase,
        pastAttackDraftPresentationMapper = PastAttackDraftPresentationMapper(),
        attackRangePresentationMapper = AttackRangePresentationMapper(),
        timeFieldPresentationMapper = TimeFieldPresentationMapper(),
        useCaseExecutor = useCaseExecutor
    )

    @Provides
    fun providesAttackDetailsViewModel(
        getAttackDetailsUseCase: GetAttackDetailsUseCase,
        updateAttackTimesUseCase: UpdateAttackTimesUseCase,
        undoTimesChangeUseCase: UndoTimesChangeUseCase,
        addIntensityPointUseCase: AddIntensityPointUseCase,
        removeIntensityPointUseCase: RemoveIntensityPointUseCase,
        selectAttackTagUseCase: SelectAttackTagUseCase,
        setFunctionalImpactUseCase: SetFunctionalImpactUseCase,
        updateAttackNoteUseCase: UpdateAttackNoteUseCase,
        addCustomTagUseCase: AddCustomTagUseCase,
        archiveCustomTagUseCase: ArchiveCustomTagUseCase,
        deleteAttackUseCase: DeleteAttackUseCase,
        mergeAttacksUseCase: MergeAttacksUseCase,
        useCaseExecutor: UseCaseExecutor
    ): AttackDetailsViewModel {
        val tagKindDomainMapper = TagKindDomainMapper()
        return AttackDetailsViewModel(
            getAttackDetailsUseCase = getAttackDetailsUseCase,
            updateAttackTimesUseCase = updateAttackTimesUseCase,
            undoTimesChangeUseCase = undoTimesChangeUseCase,
            addIntensityPointUseCase = addIntensityPointUseCase,
            removeIntensityPointUseCase = removeIntensityPointUseCase,
            selectAttackTagUseCase = selectAttackTagUseCase,
            setFunctionalImpactUseCase = setFunctionalImpactUseCase,
            updateAttackNoteUseCase = updateAttackNoteUseCase,
            addCustomTagUseCase = addCustomTagUseCase,
            archiveCustomTagUseCase = archiveCustomTagUseCase,
            deleteAttackUseCase = deleteAttackUseCase,
            mergeAttacksUseCase = mergeAttacksUseCase,
            attackDetailsPresentationMapper = AttackDetailsPresentationMapper(
                HeadLocationPresentationMapper(),
                BuiltInSymptomPresentationMapper(),
                BuiltInTriggerPresentationMapper(),
                CustomTagPresentationMapper(TagKindPresentationMapper()),
                FunctionalImpactPresentationMapper(),
                IntensityPointPresentationMapper()
            ),
            attackTagDomainMapper = AttackTagDomainMapper(
                HeadLocationDomainMapper(),
                BuiltInSymptomDomainMapper(),
                BuiltInTriggerDomainMapper(),
                CustomTagDomainMapper(tagKindDomainMapper)
            ),
            functionalImpactDomainMapper = FunctionalImpactDomainMapper(),
            intensityPointDomainMapper = IntensityPointDomainMapper(),
            intensityPointPresentationMapper = IntensityPointPresentationMapper(),
            tagKindDomainMapper = tagKindDomainMapper,
            attackRangePresentationMapper = AttackRangePresentationMapper(),
            timeFieldPresentationMapper = TimeFieldPresentationMapper(),
            useCaseExecutor = useCaseExecutor
        )
    }
}
