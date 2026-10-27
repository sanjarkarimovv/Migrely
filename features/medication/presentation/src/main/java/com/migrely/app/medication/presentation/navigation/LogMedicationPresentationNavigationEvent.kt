package com.migrely.app.medication.presentation.navigation

import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent

sealed interface LogMedicationPresentationNavigationEvent : PresentationNavigationEvent {
    data object OnManageMedications : LogMedicationPresentationNavigationEvent

    /** "Done" or back: returns to where the screen was opened from. */
    data object OnDone : LogMedicationPresentationNavigationEvent
}
