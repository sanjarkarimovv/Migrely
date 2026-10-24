package com.migrely.app.medication.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification

sealed interface MedicationsPresentationNotification : PresentationNotification {
    /** [medicationId] was archived and can be brought back with "Undo" (BRD EC-MED-01). */
    data class Archived(val medicationId: String) : MedicationsPresentationNotification

    data object Undone : MedicationsPresentationNotification

    data object StorageFull : MedicationsPresentationNotification

    data object SomethingWentWrong : MedicationsPresentationNotification
}
