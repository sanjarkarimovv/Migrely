package com.migrely.app.reminders.presentation.navigation

import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent

sealed interface VisitCardPresentationNavigationEvent : PresentationNavigationEvent {
    /** The doctor report through History's premium gate, as History's own button opens it (design D6). */
    data object OnOpenReport : VisitCardPresentationNavigationEvent
}
