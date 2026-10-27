package com.migrely.app.history.presentation.navigation

import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent
import java.time.LocalDate

sealed interface MonthSkyPresentationNavigationEvent : PresentationNavigationEvent {
    /** Open History on the current month (BRD D29). */
    data object OnViewMonth : MonthSkyPresentationNavigationEvent

    /** Open History with [day] chosen. */
    data class OnViewDay(val day: LocalDate) : MonthSkyPresentationNavigationEvent
}
