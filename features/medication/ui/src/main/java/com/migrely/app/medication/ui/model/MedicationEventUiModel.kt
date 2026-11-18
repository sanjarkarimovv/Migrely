package com.migrely.app.medication.ui.model

/**
 * What a medication screen reacts to: a message to show, an intake just logged, an intake removed by "Undo", or a
 * refused save.
 */
sealed interface MedicationEventUiModel {
    data class Message(val message: MedicationMessageUiModel) : MedicationEventUiModel

    data class Logged(val intake: IntakeUiModel) : MedicationEventUiModel

    /** The removal is stored: the logged result goes and "Removed. Nothing was logged." shows (BRD D190 (2)). */
    data object IntakeUndone : MedicationEventUiModel

    data class NotSaved(val problem: SaveProblemUiModel) : MedicationEventUiModel
}
