package com.migrely.app.medication.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification

sealed interface EditMedicationPresentationNotification : PresentationNotification {
    data class NotSaved(val problem: SaveProblemPresentationModel) :
        EditMedicationPresentationNotification

    data object StorageFull : EditMedicationPresentationNotification

    data object SomethingWentWrong : EditMedicationPresentationNotification
}
