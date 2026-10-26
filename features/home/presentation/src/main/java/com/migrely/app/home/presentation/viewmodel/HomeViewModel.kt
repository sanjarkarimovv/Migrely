package com.migrely.app.home.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.home.domain.model.AnswerLongAttackResultDomainModel
import com.migrely.app.home.domain.model.EndAttackRequestDomainModel
import com.migrely.app.home.domain.model.EndAttackResultDomainModel
import com.migrely.app.home.domain.model.LongAttackAnswerDomainModel
import com.migrely.app.home.domain.model.LongAttackAnswerRequestDomainModel
import com.migrely.app.home.domain.model.MoveStartResultDomainModel
import com.migrely.app.home.domain.model.RecordIntensityResultDomainModel
import com.migrely.app.home.domain.model.StartAttackResultDomainModel
import com.migrely.app.home.domain.model.TodayResultDomainModel
import com.migrely.app.home.domain.model.UndoResultDomainModel
import com.migrely.app.home.domain.model.WriteResultDomainModel
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
import com.migrely.app.home.presentation.mapper.TodayPresentationMapper
import com.migrely.app.home.presentation.model.HomePresentationNotification
import com.migrely.app.home.presentation.model.HomePresentationNotification.AttackDeleted
import com.migrely.app.home.presentation.model.HomePresentationNotification.AttackEnded
import com.migrely.app.home.presentation.model.HomePresentationNotification.AttackStarted
import com.migrely.app.home.presentation.model.HomePresentationNotification.EndTimeOutOfRange
import com.migrely.app.home.presentation.model.HomePresentationNotification.SomethingWentWrong
import com.migrely.app.home.presentation.model.HomePresentationNotification.StartMoveRejected
import com.migrely.app.home.presentation.model.HomePresentationNotification.StorageFull
import com.migrely.app.home.presentation.model.HomePresentationNotification.UndoNotPossible
import com.migrely.app.home.presentation.model.HomePresentationNotification.Undone
import com.migrely.app.home.presentation.model.HomeViewState
import com.migrely.app.home.presentation.model.IntensityLevelPresentationModel
import com.migrely.app.home.presentation.navigation.HomePresentationNavigationEvent
import java.time.Duration
import java.time.Instant

/**
 * Stateless (skill rule 4): every successful action reloads today, and the UI passes back any id it needs, for example
 * to undo. Today is loaded on enter, on every resume and when the local date changes (design D11).
 */
@Suppress("LongParameterList", "TooManyFunctions")
class HomeViewModel(
    private val getTodayUseCase: GetTodayUseCase,
    private val startAttackUseCase: StartAttackUseCase,
    private val recordIntensityUseCase: RecordIntensityUseCase,
    private val moveAttackStartEarlierUseCase: MoveAttackStartEarlierUseCase,
    private val endAttackUseCase: EndAttackUseCase,
    private val undoAttackStartUseCase: UndoAttackStartUseCase,
    private val undoAttackEndUseCase: UndoAttackEndUseCase,
    private val answerLongAttackCheckUseCase: AnswerLongAttackCheckUseCase,
    private val restoreDeletedAttackUseCase: RestoreDeletedAttackUseCase,
    private val setPeriodTodayUseCase: SetPeriodTodayUseCase,
    private val todayPresentationMapper: TodayPresentationMapper,
    private val intensityLevelDomainMapper: IntensityLevelDomainMapper,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<HomeViewState, HomePresentationNotification>(useCaseExecutor) {
    fun onEnter(startAttackRequested: Boolean) {
        if (startAttackRequested) startAttack() else loadToday(announceDeletion = true)
    }

    fun onResume() = loadToday(announceDeletion = true)

    fun onDateChanged() = loadToday()

    fun onStartAttackAction() = startAttack()

    fun onIntensityAction(level: IntensityLevelPresentationModel) {
        recordIntensityUseCase(
            value = intensityLevelDomainMapper.toDomain(level),
            onResult = { result ->
                when (result) {
                    RecordIntensityResultDomainModel.Recorded,
                    RecordIntensityResultDomainModel.NotInProgress -> loadToday()

                    RecordIntensityResultDomainModel.StorageFull -> notify(StorageFull)
                    RecordIntensityResultDomainModel.Failed -> notify(SomethingWentWrong)
                }
            }
        )
    }

    fun onMoveStartEarlierAction(minutes: Int) {
        moveAttackStartEarlierUseCase(
            value = Duration.ofMinutes(minutes.toLong()),
            onResult = { result ->
                when (result) {
                    is MoveStartResultDomainModel.Moved,
                    MoveStartResultDomainModel.NotInProgress -> loadToday()

                    is MoveStartResultDomainModel.OverlapsPrevious -> notify(
                        StartMoveRejected(result.previousEnd)
                    )
                    MoveStartResultDomainModel.StorageFull -> notify(StorageFull)
                    MoveStartResultDomainModel.Failed -> notify(SomethingWentWrong)
                }
            }
        )
    }

    fun onEndAttackAction() = endAttack(end = null)

    fun onEndedAtAction(end: Instant) = endAttack(end)

    fun onUndoStartAction(attackId: String) {
        undoAttackStartUseCase(value = attackId, onResult = ::presentUndo)
    }

    fun onUndoEndAction(attackId: String) {
        undoAttackEndUseCase(value = attackId, onResult = ::presentUndo)
    }

    fun onUndoDeleteAction(attackId: String) {
        restoreDeletedAttackUseCase(value = attackId, onResult = ::presentUndo)
    }

    fun onAttackDetailsAction(attackId: String) =
        emitNavigationEvent(HomePresentationNavigationEvent.OnViewAttackDetails(attackId))

    fun onAddPastAttackAction() =
        emitNavigationEvent(HomePresentationNavigationEvent.OnAddPastAttack)

    /** [attackId] is the attack in progress when called from attack mode, or null from the normal mode (BRD D34). */
    fun onLogMedicationAction(attackId: String?) =
        emitNavigationEvent(HomePresentationNavigationEvent.OnLogMedication(attackId))

    /** The gear in home's header (BRD D70). */
    fun onSettingsAction() = emitNavigationEvent(HomePresentationNavigationEvent.OnOpenSettings)

    /**
     * Home's one-tap period toggle for today (BRD DAY-F1, D48). The load after the change keeps the toggle, so a mark
     * removed by mistake while "Track periods" is off can be put back; the next load on entering or returning to home
     * offers it only as saved (D79).
     */
    fun onPeriodTodayAction(marked: Boolean) {
        setPeriodTodayUseCase(
            value = marked,
            onResult = { result ->
                when (result) {
                    WriteResultDomainModel.Written -> loadToday(keepPeriodToggle = true)
                    WriteResultDomainModel.StorageFull -> notify(StorageFull)
                    WriteResultDomainModel.Failed -> notify(SomethingWentWrong)
                }
            }
        )
    }

    fun onStillGoingAction(attackId: String) =
        answerLongAttackCheck(attackId, LongAttackAnswerDomainModel.StillGoing)

    fun onDontAskAgainAction(attackId: String) =
        answerLongAttackCheck(attackId, LongAttackAnswerDomainModel.DontAskAgain)

    private fun startAttack() {
        startAttackUseCase(
            onResult = { result ->
                when (result) {
                    is StartAttackResultDomainModel.Started -> {
                        notify(AttackStarted(result.attack.id))
                        loadToday()
                    }

                    is StartAttackResultDomainModel.AlreadyInProgress -> loadToday()
                    StartAttackResultDomainModel.StorageFull -> notify(StorageFull)
                    StartAttackResultDomainModel.Failed -> notify(SomethingWentWrong)
                }
            }
        )
    }

    private fun endAttack(end: Instant?) {
        endAttackUseCase(
            value = EndAttackRequestDomainModel(end),
            onResult = { result ->
                when (result) {
                    is EndAttackResultDomainModel.Ended -> {
                        notify(AttackEnded(result.attackId))
                        loadToday()
                    }

                    EndAttackResultDomainModel.NotInProgress -> loadToday()
                    EndAttackResultDomainModel.EndOutOfRange -> notify(EndTimeOutOfRange)
                    EndAttackResultDomainModel.StorageFull -> notify(StorageFull)
                    EndAttackResultDomainModel.Failed -> notify(SomethingWentWrong)
                }
            }
        )
    }

    private fun answerLongAttackCheck(attackId: String, answer: LongAttackAnswerDomainModel) {
        answerLongAttackCheckUseCase(
            value = LongAttackAnswerRequestDomainModel(attackId, answer),
            onResult = { result ->
                when (result) {
                    AnswerLongAttackResultDomainModel.Saved,
                    AnswerLongAttackResultDomainModel.NotInProgress -> loadToday()

                    AnswerLongAttackResultDomainModel.StorageFull -> notify(StorageFull)
                    AnswerLongAttackResultDomainModel.Failed -> notify(SomethingWentWrong)
                }
            }
        )
    }

    private fun presentUndo(result: UndoResultDomainModel) {
        when (result) {
            UndoResultDomainModel.Undone -> {
                notify(Undone)
                loadToday()
            }

            UndoResultDomainModel.NotPossible -> {
                notify(UndoNotPossible)
                loadToday()
            }

            UndoResultDomainModel.Failed -> notify(SomethingWentWrong)
        }
    }

    /**
     * Loads today. Only a load on entering or returning to the screen announces a recent deletion, so the undo of a
     * deletion never replaces the undo of an action taken here. [keepPeriodToggle] keeps the "Period" toggle that was
     * just used; every other load offers it as today's result says (BRD D79).
     */
    private fun loadToday(announceDeletion: Boolean = false, keepPeriodToggle: Boolean = false) {
        getTodayUseCase(
            onResult = { result ->
                when (result) {
                    is TodayResultDomainModel.Loaded -> {
                        updateViewState(
                            todayPresentationMapper.toPresentation(
                                result.today,
                                result.periodToday,
                                result.periodOffered || keepPeriodToggle
                            )
                        )
                        val deleted = result.recentlyDeleted
                        if (announceDeletion && deleted != null) {
                            notify(AttackDeleted(deleted.id, deleted.deletedAt))
                        }
                    }

                    TodayResultDomainModel.Failed -> notify(SomethingWentWrong)
                }
            }
        )
    }
}
