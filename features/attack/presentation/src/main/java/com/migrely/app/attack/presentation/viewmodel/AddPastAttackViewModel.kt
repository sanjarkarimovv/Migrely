package com.migrely.app.attack.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.attack.domain.model.CreatePastAttackRequestDomainModel
import com.migrely.app.attack.domain.model.CreatePastAttackResultDomainModel
import com.migrely.app.attack.domain.model.MergeAttacksRequestDomainModel
import com.migrely.app.attack.domain.model.MergeAttacksResultDomainModel
import com.migrely.app.attack.domain.usecase.CreatePastAttackUseCase
import com.migrely.app.attack.domain.usecase.GetPastAttackDraftUseCase
import com.migrely.app.attack.domain.usecase.MergeAttacksUseCase
import com.migrely.app.attack.presentation.mapper.AttackRangePresentationMapper
import com.migrely.app.attack.presentation.mapper.PastAttackDraftPresentationMapper
import com.migrely.app.attack.presentation.mapper.TimeFieldPresentationMapper
import com.migrely.app.attack.presentation.model.AddPastAttackPresentationNotification
import com.migrely.app.attack.presentation.model.AddPastAttackViewState
import com.migrely.app.attack.presentation.navigation.AddPastAttackPresentationNavigationEvent
import java.time.Instant

/**
 * Adds an attack that has already ended (BRD ATK-F3). Stateless (skill rule 4): the picked times live in the screen and
 * come back with every action.
 */
@Suppress("LongParameterList")
class AddPastAttackViewModel(
    private val getPastAttackDraftUseCase: GetPastAttackDraftUseCase,
    private val createPastAttackUseCase: CreatePastAttackUseCase,
    private val mergeAttacksUseCase: MergeAttacksUseCase,
    private val pastAttackDraftPresentationMapper: PastAttackDraftPresentationMapper,
    private val attackRangePresentationMapper: AttackRangePresentationMapper,
    private val timeFieldPresentationMapper: TimeFieldPresentationMapper,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<AddPastAttackViewState, AddPastAttackPresentationNotification>(
    useCaseExecutor
) {
    fun onEnter() {
        updateViewState(AddPastAttackViewState.Loading)
        getPastAttackDraftUseCase(onResult = { draft ->
            val ready = pastAttackDraftPresentationMapper.toPresentation(draft)
            updateViewState(AddPastAttackViewState.Ready(ready))
        })
    }

    fun onAddAction(start: Instant, end: Instant) {
        createPastAttackUseCase(
            value = CreatePastAttackRequestDomainModel(start, end),
            onResult = { result ->
                when (result) {
                    is CreatePastAttackResultDomainModel.Created -> emitNavigationEvent(
                        AddPastAttackPresentationNavigationEvent.OnPastAttackAdded(result.attackId)
                    )

                    is CreatePastAttackResultDomainModel.InFuture -> notify(
                        AddPastAttackPresentationNotification.InFuture(
                            timeFieldPresentationMapper.toPresentation(result.field)
                        )
                    )

                    CreatePastAttackResultDomainModel.EndBeforeStart ->
                        notify(AddPastAttackPresentationNotification.EndBeforeStart)

                    is CreatePastAttackResultDomainModel.Overlaps -> notify(
                        AddPastAttackPresentationNotification.Overlaps(
                            result.attacks.map(attackRangePresentationMapper::toPresentation)
                        )
                    )

                    CreatePastAttackResultDomainModel.StorageFull ->
                        notify(AddPastAttackPresentationNotification.StorageFull)

                    CreatePastAttackResultDomainModel.Failed ->
                        notify(AddPastAttackPresentationNotification.SomethingWentWrong)
                }
            }
        )
    }

    fun onMergeAction(start: Instant, end: Instant) {
        mergeAttacksUseCase(
            value = MergeAttacksRequestDomainModel(
                attackId = null,
                start = start,
                end = end,
                editedNote = null
            ),
            onResult = { result ->
                when (result) {
                    is MergeAttacksResultDomainModel.Merged -> emitNavigationEvent(
                        AddPastAttackPresentationNavigationEvent.OnAttacksMerged(result.attackId)
                    )

                    // Nothing overlapped any more, so the attack was simply added.
                    is MergeAttacksResultDomainModel.Saved -> emitNavigationEvent(
                        AddPastAttackPresentationNavigationEvent.OnPastAttackAdded(result.attackId)
                    )

                    is MergeAttacksResultDomainModel.InFuture -> notify(
                        AddPastAttackPresentationNotification.InFuture(
                            timeFieldPresentationMapper.toPresentation(result.field)
                        )
                    )

                    MergeAttacksResultDomainModel.EndBeforeStart ->
                        notify(AddPastAttackPresentationNotification.EndBeforeStart)

                    MergeAttacksResultDomainModel.StorageFull ->
                        notify(AddPastAttackPresentationNotification.StorageFull)

                    // A new attack has no stored attack to lose, so NotFound is a failure here.
                    MergeAttacksResultDomainModel.NotFound,
                    MergeAttacksResultDomainModel.Failed ->
                        notify(AddPastAttackPresentationNotification.SomethingWentWrong)
                }
            }
        )
    }

    fun onLeaveAction() = emitNavigationEvent(AddPastAttackPresentationNavigationEvent.OnLeave)
}
