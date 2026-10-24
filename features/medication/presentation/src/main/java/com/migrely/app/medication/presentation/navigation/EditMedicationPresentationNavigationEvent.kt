package com.migrely.app.medication.presentation.navigation

import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent

sealed interface EditMedicationPresentationNavigationEvent : PresentationNavigationEvent {
    /** Saved, or leaving without saving: both return to the list. */
    data object OnLeave : EditMedicationPresentationNavigationEvent
}
