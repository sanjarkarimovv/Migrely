package com.migrely.app.medication.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification

sealed interface LogMedicationPresentationNotification : PresentationNotification {
    /** The screen shows what was logged with "Undo" and "Done" (BRD D34). */
    data class Logged(val intake: IntakePresentationModel) : LogMedicationPresentationNotification

    /** "Undo" removed the intake: the list shows again (BRD D34). */
    data object IntakeUndone : LogMedicationPresentationNotification

    /** "Undo" found the disk full: the intake is still stored, so its result stays (BRD D190 (2), EC-MED-17). */
    data object IntakeKeptStorageFull : LogMedicationPresentationNotification

    /** "Undo" failed another way: the intake is still stored, so its result stays (BRD D190 (2), EC-MED-17). */
    data object IntakeKeptFailed : LogMedicationPresentationNotification

    /** The first medication could not be added (BRD EC-MED-08). */
    data class NotSaved(val problem: SaveProblemPresentationModel) :
        LogMedicationPresentationNotification

    data object InFuture : LogMedicationPresentationNotification

    data object StorageFull : LogMedicationPresentationNotification

    data object SomethingWentWrong : LogMedicationPresentationNotification
}
