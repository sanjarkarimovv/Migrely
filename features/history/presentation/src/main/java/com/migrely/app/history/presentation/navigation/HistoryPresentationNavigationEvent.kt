package com.migrely.app.history.presentation.navigation

import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent
import com.migrely.app.history.presentation.model.PremiumFeaturePresentationModel
import java.time.LocalDate

sealed interface HistoryPresentationNavigationEvent : PresentationNavigationEvent {
    data class OnViewAttackDetails(val attackId: String) : HistoryPresentationNavigationEvent

    /** Add an attack that has already ended on [day], or with the usual defaults when null (BRD D30). */
    data class OnAddPastAttack(val day: LocalDate?) : HistoryPresentationNavigationEvent

    data object OnLeave : HistoryPresentationNavigationEvent

    /** A premium user wants a report for their doctor (BRD REP-F1, D81, D108). */
    data object OnOpenReport : HistoryPresentationNavigationEvent

    /** A premium user wants to see their patterns (BRD INS-F1, D87, D108). */
    data object OnOpenInsights : HistoryPresentationNavigationEvent

    /** A free user tapped a premium [feature]: its paywall opens (BRD D108). */
    data class OnOpenPaywall(val feature: PremiumFeaturePresentationModel) :
        HistoryPresentationNavigationEvent
}
