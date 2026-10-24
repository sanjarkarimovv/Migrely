package com.migrely.app.medication.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification

sealed interface LogMedicationPresentationNotification : PresentationNotification {
    /** The screen shows what was logged with "Undo" and "Done" (BRD D34). */
    data class Logged(val intake: IntakePresentationModel) : LogMedicationPresentationNotification

    data object IntakeUndone : LogMedicationPresentationNotification

    /** The first medication could not be added (BRD EC-MED-08). */
    data class NotSaved(val problem: SaveProblemPresentationModel) :
        LogMedicationPresentationNotification

    data object InFuture : LogMedicationPresentationNotification

    data object StorageFull : LogMedicationPresentationNotification

    data object SomethingWentWrong : LogMedicationPresentationNotification
}
