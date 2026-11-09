package com.migrely.app.reminders.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.reminders.domain.model.VisitCardDomainModel
import com.migrely.app.reminders.domain.usecase.CloseVisitCardUseCase
import com.migrely.app.reminders.domain.usecase.GetVisitCardUseCase
import com.migrely.app.reminders.presentation.model.VisitCardPresentationNotification
import com.migrely.app.reminders.presentation.model.VisitCardViewState
import com.migrely.app.reminders.presentation.navigation.VisitCardPresentationNavigationEvent
import java.time.LocalDate

/**
 * Home's visit card (BRD D168, design D8), in a slot home leaves to `:app`; it enters again every time home loads. A tap
 * on either button takes it away at once and for that visit, and "Make a report" opens the report through History.
 * Only the latest read is shown, so a read that answers late never brings a closed card back.
 */
class VisitCardViewModel(
    private val getVisitCardUseCase: GetVisitCardUseCase,
    private val closeVisitCardUseCase: CloseVisitCardUseCase,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<VisitCardViewState, VisitCardPresentationNotification>(useCaseExecutor) {
    /** The number of the latest read or close; both happen on the main thread. */
    private var latest = 0

    fun onEnter() {
        val read = ++latest
        getVisitCardUseCase(
            onResult = { card ->
                if (read == latest) {
                    updateViewState(
                        when (card) {
                            VisitCardDomainModel.Hidden -> VisitCardViewState.Hidden
                            is VisitCardDomainModel.Shown -> VisitCardViewState.Shown(card.visit)
                        }
                    )
                }
            }
        )
    }

    fun onCloseAction(visit: LocalDate) = close(visit)

    fun onReportAction(visit: LocalDate) {
        close(visit)
        emitNavigationEvent(VisitCardPresentationNavigationEvent.OnOpenReport)
    }

    private fun close(visit: LocalDate) {
        latest++
        updateViewState(VisitCardViewState.Hidden)
        closeVisitCardUseCase(value = visit)
    }
}
