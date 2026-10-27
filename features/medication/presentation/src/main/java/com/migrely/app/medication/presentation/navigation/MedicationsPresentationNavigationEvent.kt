package com.migrely.app.medication.presentation.navigation

import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent

sealed interface MedicationsPresentationNavigationEvent : PresentationNavigationEvent {
    data object OnAddMedication : MedicationsPresentationNavigationEvent

    data class OnEditMedication(val medicationId: String) : MedicationsPresentationNavigationEvent

    data object OnLeave : MedicationsPresentationNavigationEvent
}
