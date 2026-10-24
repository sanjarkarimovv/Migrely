package com.migrely.app.history.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.history.domain.model.DayMarkKindDomainModel
import com.migrely.app.history.domain.model.HistoryMonthResultDomainModel
import com.migrely.app.history.domain.model.SetDayMarkRequestDomainModel
import com.migrely.app.history.domain.model.SetDayMarkResultDomainModel
import com.migrely.app.history.domain.model.UndoResultDomainModel
import com.migrely.app.history.domain.usecase.GetHistoryMonthUseCase
import com.migrely.app.history.domain.usecase.RestoreDeletedAttackUseCase
import com.migrely.app.history.domain.usecase.SetDayMarkUseCase
import com.migrely.app.history.presentation.mapper.HistoryMonthPresentationMapper
import com.migrely.app.history.presentation.model.HistoryPresentationNotification
import com.migrely.app.history.presentation.model.HistoryPresentationNotification.AttackDeleted
import com.migrely.app.history.presentation.model.HistoryPresentationNotification.AttackLoggedOnDay
import com.migrely.app.history.presentation.model.HistoryPresentationNotification.SomethingWentWrong
import com.migrely.app.history.presentation.model.HistoryPresentationNotification.StorageFull
import com.migrely.app.history.presentation.model.HistoryPresentationNotification.UndoNotPossible
import com.migrely.app.history.presentation.model.HistoryPresentationNotification.Undone
import com.migrely.app.history.presentation.model.HistoryViewState
import com.migrely.app.history.presentation.navigation.HistoryPresentationNavigationEvent
import java.time.LocalDate
import java.time.YearMonth

/**
 * Stateless (skill rule 4): the screen owns the month asked for and passes it to every call; null asks for the current
 * month. The month is loaded on enter, on every resume and after an undo, so changes made on the details screen show at
 * once (design D4).
 */
class HistoryViewModel(
    private val getHistoryMonthUseCase: GetHistoryMonthUseCase,
    private val restoreDeletedAttackUseCase: RestoreDeletedAttackUseCase,
    private val setDayMarkUseCase: SetDayMarkUseCase,
    private val historyMonthPresentationMapper: HistoryMonthPresentationMapper,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<HistoryViewState, HistoryPresentationNotification>(useCaseExecutor) {
    fun onEnter(month: YearMonth?) = loadMonth(month, announceDeletion = true)

    fun onResume(month: YearMonth?) = loadMonth(month, announceDeletion = true)

    fun onMonthAction(month: YearMonth) = loadMonth(month)

    fun onAttackAction(attackId: String) =
        emitNavigationEvent(HistoryPresentationNavigationEvent.OnViewAttackDetails(attackId))

    /** [day] is the chosen day, or null for the usual defaults (BRD D30, EC-HIS-01). */
    fun onAddPastAttackAction(day: LocalDate?) =
        emitNavigationEvent(HistoryPresentationNavigationEvent.OnAddPastAttack(day))

    fun onUndoDeleteAction(attackId: String, month: YearMonth?) {
        restoreDeletedAttackUseCase(
            value = attackId,
            onResult = { result ->
                when (result) {
                    UndoResultDomainModel.Undone -> {
                        notify(Undone)
                        loadMonth(month)
                    }

                    UndoResultDomainModel.NotPossible -> {
                        notify(UndoNotPossible)
                        loadMonth(month)
                    }

                    UndoResultDomainModel.Failed -> notify(SomethingWentWrong)
                }
            }
        )
    }

    /** Marks or unmarks [day] as a period day, then loads [month] again (BRD DAY-F1, D48). */
    fun onPeriodAction(month: YearMonth?, day: LocalDate, marked: Boolean) =
        setMark(month, SetDayMarkRequestDomainModel(day, DayMarkKindDomainModel.Period, marked))

    /** Marks or unmarks [day] as pain-free, then loads [month] again (BRD DAY-F2, EC-DAY-01). */
    fun onPainFreeAction(month: YearMonth?, day: LocalDate, marked: Boolean) =
        setMark(month, SetDayMarkRequestDomainModel(day, DayMarkKindDomainModel.PainFree, marked))

    fun onLeaveAction() = emitNavigationEvent(HistoryPresentationNavigationEvent.OnLeave)

    private fun setMark(month: YearMonth?, request: SetDayMarkRequestDomainModel) {
        setDayMarkUseCase(
            value = request,
            onResult = { result ->
                when (result) {
                    SetDayMarkResultDomainModel.Done -> loadMonth(month)

                    SetDayMarkResultDomainModel.AttackLogged -> {
                        notify(AttackLoggedOnDay)
                        loadMonth(month)
                    }

                    SetDayMarkResultDomainModel.StorageFull -> notify(StorageFull)

                    SetDayMarkResultDomainModel.FutureDay,
                    SetDayMarkResultDomainModel.Failed -> notify(SomethingWentWrong)
                }
            }
        )
    }

    /**
     * Only a load on entering or returning to the screen announces a recent deletion, so the result of an undo is never
     * followed by the deletion it undid.
     */
    private fun loadMonth(month: YearMonth?, announceDeletion: Boolean = false) {
        getHistoryMonthUseCase(
            value = month,
            onResult = { result ->
                when (result) {
                    is HistoryMonthResultDomainModel.Loaded -> {
                        val shown = historyMonthPresentationMapper.toPresentation(result.month)
                        updateViewState(
                            if (result.month.anyAttackLogged) {
                                HistoryViewState.Ready(shown, requestedMonth = month)
                            } else {
                                HistoryViewState.NoAttacks(shown, requestedMonth = month)
                            }
                        )
                        val deleted = result.recentlyDeleted
                        if (announceDeletion && deleted != null) {
                            notify(AttackDeleted(deleted.id, deleted.deletedAt))
                        }
                    }

                    HistoryMonthResultDomainModel.Failed -> notify(SomethingWentWrong)
                }
            }
        )
    }
}
