package com.migrely.app.medication.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification

sealed interface MedicationsPresentationNotification : PresentationNotification {
    /** [medicationId] was archived and can be brought back with "Undo" (BRD EC-MED-01). */
    data class Archived(val medicationId: String) : MedicationsPresentationNotification

    data object Undone : MedicationsPresentationNotification

    /** "Undo" came after the list reached 50 again, so the medication stays archived (BRD D35). */
    data object LimitReached : MedicationsPresentationNotification

    data object StorageFull : MedicationsPresentationNotification

    data object SomethingWentWrong : MedicationsPresentationNotification
}
