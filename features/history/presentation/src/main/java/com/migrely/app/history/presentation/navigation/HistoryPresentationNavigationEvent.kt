package com.migrely.app.history.presentation.navigation

import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent
import java.time.LocalDate

sealed interface HistoryPresentationNavigationEvent : PresentationNavigationEvent {
    data class OnViewAttackDetails(val attackId: String) : HistoryPresentationNavigationEvent

    /** Add an attack that has already ended on [day], or with the usual defaults when null (BRD D30). */
    data class OnAddPastAttack(val day: LocalDate?) : HistoryPresentationNavigationEvent

    data object OnLeave : HistoryPresentationNavigationEvent
}
