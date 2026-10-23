package com.migrely.app.attack.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.attack.domain.model.AddCustomTagRequestDomainModel
import com.migrely.app.attack.domain.model.AddCustomTagResultDomainModel
import com.migrely.app.attack.domain.model.AddIntensityPointRequestDomainModel
import com.migrely.app.attack.domain.model.AddIntensityPointResultDomainModel
import com.migrely.app.attack.domain.model.AttackDetailsResultDomainModel
import com.migrely.app.attack.domain.model.MergeAttacksRequestDomainModel
import com.migrely.app.attack.domain.model.MergeAttacksResultDomainModel
import com.migrely.app.attack.domain.model.RemoveIntensityPointRequestDomainModel
import com.migrely.app.attack.domain.model.SelectAttackTagRequestDomainModel
import com.migrely.app.attack.domain.model.SetFunctionalImpactRequestDomainModel
import com.migrely.app.attack.domain.model.UndoTimesChangeRequestDomainModel
import com.migrely.app.attack.domain.model.UndoTimesChangeResultDomainModel
import com.migrely.app.attack.domain.model.UpdateAttackNoteRequestDomainModel
import com.migrely.app.attack.domain.model.UpdateAttackNoteResultDomainModel
import com.migrely.app.attack.domain.model.UpdateAttackTimesRequestDomainModel
import com.migrely.app.attack.domain.model.UpdateAttackTimesResultDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.usecase.AddCustomTagUseCase
import com.migrely.app.attack.domain.usecase.AddIntensityPointUseCase
import com.migrely.app.attack.domain.usecase.ArchiveCustomTagUseCase
import com.migrely.app.attack.domain.usecase.DeleteAttackUseCase
import com.migrely.app.attack.domain.usecase.GetAttackDetailsUseCase
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
import com.migrely.app.attack.presentation.mapper.FunctionalImpactDomainMapper
import com.migrely.app.attack.presentation.mapper.IntensityPointDomainMapper
import com.migrely.app.attack.presentation.mapper.IntensityPointPresentationMapper
import com.migrely.app.attack.presentation.mapper.TagKindDomainMapper
import com.migrely.app.attack.presentation.mapper.TimeFieldPresentationMapper
import com.migrely.app.attack.presentation.model.AttackDetailsPresentationNotification
import com.migrely.app.attack.presentation.model.AttackDetailsPresentationNotification.AttacksMerged
import com.migrely.app.attack.presentation.model.AttackDetailsPresentationNotification.FirstMaximumRecorded
import com.migrely.app.attack.presentation.model.AttackDetailsPresentationNotification.PointRejected
import com.migrely.app.attack.presentation.model.AttackDetailsPresentationNotification.PointRemoved
import com.migrely.app.attack.presentation.model.AttackDetailsPresentationNotification.PointsRemovedByTimeChange
import com.migrely.app.attack.presentation.model.AttackDetailsPresentationNotification.SomethingWentWrong
import com.migrely.app.attack.presentation.model.AttackDetailsPresentationNotification.StorageFull
import com.migrely.app.attack.presentation.model.AttackDetailsPresentationNotification.TagLimitReached
import com.migrely.app.attack.presentation.model.AttackDetailsPresentationNotification.TimeRejected
import com.migrely.app.attack.presentation.model.AttackDetailsPresentationNotification.UndoNotPossible
import com.migrely.app.attack.presentation.model.AttackDetailsPresentationNotification.Undone
import com.migrely.app.attack.presentation.model.AttackDetailsViewState
import com.migrely.app.attack.presentation.model.AttackTagPresentationModel
import com.migrely.app.attack.presentation.model.FunctionalImpactPresentationModel
import com.migrely.app.attack.presentation.model.IntensityPointPresentationModel
import com.migrely.app.attack.presentation.model.PointRejectionPresentationModel
import com.migrely.app.attack.presentation.model.TagKindPresentationModel
import com.migrely.app.attack.presentation.model.TimeFieldPresentationModel
import com.migrely.app.attack.presentation.model.TimeRejectionPresentationModel
import com.migrely.app.attack.presentation.model.TimesUndoPresentationModel
import com.migrely.app.attack.presentation.navigation.AttackDetailsPresentationNavigationEvent
import java.time.Instant

/**
 * Stateless (skill rule 4): every action takes the attack id and what it needs as parameters, and every successful
 * write reloads the details. Undo payloads travel inside the notifications and come back with the undo action.
 */
@Suppress("LongParameterList", "TooManyFunctions")
class AttackDetailsViewModel(
    private val getAttackDetailsUseCase: GetAttackDetailsUseCase,
    private val updateAttackTimesUseCase: UpdateAttackTimesUseCase,
    private val undoTimesChangeUseCase: UndoTimesChangeUseCase,
    private val addIntensityPointUseCase: AddIntensityPointUseCase,
    private val removeIntensityPointUseCase: RemoveIntensityPointUseCase,
    private val selectAttackTagUseCase: SelectAttackTagUseCase,
    private val setFunctionalImpactUseCase: SetFunctionalImpactUseCase,
    private val updateAttackNoteUseCase: UpdateAttackNoteUseCase,
    private val addCustomTagUseCase: AddCustomTagUseCase,
    private val archiveCustomTagUseCase: ArchiveCustomTagUseCase,
    private val deleteAttackUseCase: DeleteAttackUseCase,
    private val mergeAttacksUseCase: MergeAttacksUseCase,
    private val attackDetailsPresentationMapper: AttackDetailsPresentationMapper,
    private val attackTagDomainMapper: AttackTagDomainMapper,
    private val functionalImpactDomainMapper: FunctionalImpactDomainMapper,
    private val intensityPointDomainMapper: IntensityPointDomainMapper,
    private val intensityPointPresentationMapper: IntensityPointPresentationMapper,
    private val tagKindDomainMapper: TagKindDomainMapper,
    private val attackRangePresentationMapper: AttackRangePresentationMapper,
    private val timeFieldPresentationMapper: TimeFieldPresentationMapper,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<AttackDetailsViewState, AttackDetailsPresentationNotification>(useCaseExecutor) {
    /** [attacksMerged] is true when the screen opens on the result of a merge from the add screen. */
    fun onEnter(attackId: String, attacksMerged: Boolean) {
        updateViewState(AttackDetailsViewState.Loading)
        if (attacksMerged) notify(AttacksMerged)
        loadDetails(attackId)
    }

    fun onResume(attackId: String) = loadDetails(attackId)

    /** The user picked a new [field]; [start] and [end] are both times as they should be stored. */
    fun onTimesAction(
        attackId: String,
        field: TimeFieldPresentationModel,
        start: Instant,
        end: Instant?
    ) {
        updateAttackTimesUseCase(
            value = UpdateAttackTimesRequestDomainModel(attackId, start, end),
            onResult = { result -> presentTimesResult(attackId, field, start, end, result) }
        )
    }

    /** The user chose to merge the refused times with the attacks they overlap (BRD EC-ATK-05, D27). */
    fun onMergeAction(attackId: String, start: Instant, end: Instant?) {
        mergeAttacksUseCase(
            value = MergeAttacksRequestDomainModel(attackId, start, end),
            onResult = { result ->
                when (result) {
                    is MergeAttacksResultDomainModel.Merged ->
                        succeeded(result.attackId, AttacksMerged)

                    is MergeAttacksResultDomainModel.Saved -> loadDetails(result.attackId)
                    is MergeAttacksResultDomainModel.InFuture -> notify(
                        TimeRejected(
                            timeFieldPresentationMapper.toPresentation(result.field),
                            TimeRejectionPresentationModel.InFuture
                        )
                    )

                    MergeAttacksResultDomainModel.EndBeforeStart -> notify(
                        TimeRejected(
                            TimeFieldPresentationModel.End,
                            TimeRejectionPresentationModel.EndBeforeStart
                        )
                    )

                    MergeAttacksResultDomainModel.NotFound ->
                        updateViewState(AttackDetailsViewState.NotFound)
                    MergeAttacksResultDomainModel.StorageFull -> notify(StorageFull)
                    MergeAttacksResultDomainModel.Failed -> notify(SomethingWentWrong)
                }
            }
        )
    }

    fun onUndoTimesChangeAction(undo: TimesUndoPresentationModel) {
        undoTimesChangeUseCase(
            value = UndoTimesChangeRequestDomainModel(
                attackId = undo.attackId,
                start = undo.start,
                end = undo.end,
                points = undo.removedPoints.map(intensityPointDomainMapper::toDomain)
            ),
            onResult = { result ->
                when (result) {
                    UndoTimesChangeResultDomainModel.Restored -> succeeded(undo.attackId, Undone)
                    UndoTimesChangeResultDomainModel.NotPossible ->
                        succeeded(undo.attackId, UndoNotPossible)
                    UndoTimesChangeResultDomainModel.StorageFull -> notify(StorageFull)
                    UndoTimesChangeResultDomainModel.Failed -> notify(SomethingWentWrong)
                }
            }
        )
    }

    fun onAddPointAction(attackId: String, point: IntensityPointPresentationModel) {
        addIntensityPointUseCase(
            value = AddIntensityPointRequestDomainModel(
                attackId,
                intensityPointDomainMapper.toDomain(point)
            ),
            onResult = { result ->
                when (result) {
                    is AddIntensityPointResultDomainModel.Added ->
                        if (result.firstMaximum) {
                            succeeded(attackId, FirstMaximumRecorded)
                        } else {
                            loadDetails(
                                attackId
                            )
                        }

                    AddIntensityPointResultDomainModel.OutsideAttack ->
                        notify(PointRejected(PointRejectionPresentationModel.OutsideAttack))

                    AddIntensityPointResultDomainModel.InFuture ->
                        notify(PointRejected(PointRejectionPresentationModel.InFuture))

                    AddIntensityPointResultDomainModel.NotFound ->
                        updateViewState(AttackDetailsViewState.NotFound)
                    AddIntensityPointResultDomainModel.StorageFull -> notify(StorageFull)
                    AddIntensityPointResultDomainModel.Failed -> notify(SomethingWentWrong)
                }
            }
        )
    }

    fun onRemovePointAction(attackId: String, point: IntensityPointPresentationModel) {
        removeIntensityPointUseCase(
            value = RemoveIntensityPointRequestDomainModel(
                attackId,
                intensityPointDomainMapper.toDomain(point)
            ),
            onResult = { result -> presentWrite(attackId, result, PointRemoved(attackId, point)) }
        )
    }

    fun onUndoPointRemovalAction(attackId: String, point: IntensityPointPresentationModel) {
        addIntensityPointUseCase(
            value = AddIntensityPointRequestDomainModel(
                attackId,
                intensityPointDomainMapper.toDomain(point)
            ),
            onResult = { result ->
                when (result) {
                    is AddIntensityPointResultDomainModel.Added -> succeeded(attackId, Undone)
                    AddIntensityPointResultDomainModel.OutsideAttack,
                    AddIntensityPointResultDomainModel.InFuture,
                    AddIntensityPointResultDomainModel.NotFound ->
                        succeeded(attackId, UndoNotPossible)

                    AddIntensityPointResultDomainModel.StorageFull -> notify(StorageFull)
                    AddIntensityPointResultDomainModel.Failed -> notify(SomethingWentWrong)
                }
            }
        )
    }

    fun onTagAction(attackId: String, tag: AttackTagPresentationModel, selected: Boolean) {
        selectAttackTagUseCase(
            value = SelectAttackTagRequestDomainModel(
                attackId,
                attackTagDomainMapper.toDomain(tag),
                selected
            ),
            onResult = { result -> presentWrite(attackId, result) }
        )
    }

    fun onImpactAction(attackId: String, impact: FunctionalImpactPresentationModel?) {
        setFunctionalImpactUseCase(
            value = SetFunctionalImpactRequestDomainModel(
                attackId,
                impact?.let(functionalImpactDomainMapper::toDomain)
            ),
            onResult = { result -> presentWrite(attackId, result) }
        )
    }

    fun onNoteAction(attackId: String, note: String) {
        updateAttackNoteUseCase(
            value = UpdateAttackNoteRequestDomainModel(attackId, note),
            onResult = { result ->
                when (result) {
                    UpdateAttackNoteResultDomainModel.Saved -> loadDetails(attackId)
                    UpdateAttackNoteResultDomainModel.StorageFull -> notify(StorageFull)
                    UpdateAttackNoteResultDomainModel.TooLong,
                    UpdateAttackNoteResultDomainModel.Failed -> notify(SomethingWentWrong)
                }
            }
        )
    }

    fun onAddCustomTagAction(attackId: String, kind: TagKindPresentationModel, name: String) {
        addCustomTagUseCase(
            value = AddCustomTagRequestDomainModel(
                attackId,
                tagKindDomainMapper.toDomain(kind),
                name
            ),
            onResult = { result ->
                when (result) {
                    is AddCustomTagResultDomainModel.Added -> loadDetails(attackId)
                    AddCustomTagResultDomainModel.LimitReached -> notify(TagLimitReached)
                    AddCustomTagResultDomainModel.StorageFull -> notify(StorageFull)
                    AddCustomTagResultDomainModel.Invalid,
                    AddCustomTagResultDomainModel.Failed -> notify(SomethingWentWrong)
                }
            }
        )
    }

    fun onArchiveCustomTagAction(attackId: String, tagId: String) {
        archiveCustomTagUseCase(value = tagId, onResult = { result ->
            presentWrite(attackId, result)
        })
    }

    fun onDeleteAction(attackId: String) {
        deleteAttackUseCase(
            value = attackId,
            onResult = { result ->
                when (result) {
                    WriteResultDomainModel.Written ->
                        emitNavigationEvent(
                            AttackDetailsPresentationNavigationEvent.OnAttackDeleted
                        )

                    WriteResultDomainModel.StorageFull -> notify(StorageFull)
                    WriteResultDomainModel.Failed -> notify(SomethingWentWrong)
                }
            }
        )
    }

    fun onLeaveAction() = emitNavigationEvent(AttackDetailsPresentationNavigationEvent.OnLeave)

    @Suppress("LongParameterList")
    private fun presentTimesResult(
        attackId: String,
        field: TimeFieldPresentationModel,
        start: Instant,
        end: Instant?,
        result: UpdateAttackTimesResultDomainModel
    ) {
        when (result) {
            is UpdateAttackTimesResultDomainModel.Updated ->
                if (result.removedPoints.isEmpty()) {
                    loadDetails(attackId)
                } else {
                    succeeded(
                        attackId,
                        PointsRemovedByTimeChange(
                            TimesUndoPresentationModel(
                                attackId = attackId,
                                start = result.previousStart,
                                end = result.previousEnd,
                                removedPoints = result.removedPoints.map(
                                    intensityPointPresentationMapper::toPresentation
                                )
                            )
                        )
                    )
                }

            is UpdateAttackTimesResultDomainModel.InFuture -> notify(
                TimeRejected(
                    field = timeFieldPresentationMapper.toPresentation(result.field),
                    reason = TimeRejectionPresentationModel.InFuture
                )
            )

            UpdateAttackTimesResultDomainModel.EndBeforeStart ->
                notify(TimeRejected(field, TimeRejectionPresentationModel.EndBeforeStart))

            is UpdateAttackTimesResultDomainModel.Overlaps -> notify(
                TimeRejected(
                    field,
                    TimeRejectionPresentationModel.Overlaps(
                        attacks = result.attacks.map(attackRangePresentationMapper::toPresentation),
                        requestedStart = start,
                        requestedEnd = end
                    )
                )
            )

            UpdateAttackTimesResultDomainModel.NotFound ->
                updateViewState(AttackDetailsViewState.NotFound)
            UpdateAttackTimesResultDomainModel.StorageFull -> notify(StorageFull)
            UpdateAttackTimesResultDomainModel.Failed -> notify(SomethingWentWrong)
        }
    }

    private fun presentWrite(
        attackId: String,
        result: WriteResultDomainModel,
        onWritten: AttackDetailsPresentationNotification? = null
    ) {
        when (result) {
            WriteResultDomainModel.Written ->
                if (onWritten == null) loadDetails(attackId) else succeeded(attackId, onWritten)

            WriteResultDomainModel.StorageFull -> notify(StorageFull)
            WriteResultDomainModel.Failed -> notify(SomethingWentWrong)
        }
    }

    private fun succeeded(attackId: String, notification: AttackDetailsPresentationNotification) {
        notify(notification)
        loadDetails(attackId)
    }

    private fun loadDetails(attackId: String) {
        getAttackDetailsUseCase(
            value = attackId,
            onResult = { result ->
                when (result) {
                    is AttackDetailsResultDomainModel.Loaded -> updateViewState(
                        AttackDetailsViewState.Ready(
                            attackDetailsPresentationMapper
                                .toPresentation(result.screen)
                        )
                    )

                    AttackDetailsResultDomainModel.NotFound ->
                        updateViewState(AttackDetailsViewState.NotFound)
                    AttackDetailsResultDomainModel.Failed -> notify(SomethingWentWrong)
                }
            }
        )
    }
}
