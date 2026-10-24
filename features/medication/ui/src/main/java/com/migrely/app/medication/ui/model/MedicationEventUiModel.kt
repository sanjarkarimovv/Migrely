package com.migrely.app.medication.ui.model

/** What a medication screen reacts to: a message to show, an intake just logged, or a refused save. */
sealed interface MedicationEventUiModel {
    data class Message(val message: MedicationMessageUiModel) : MedicationEventUiModel

    data class Logged(val intake: IntakeUiModel) : MedicationEventUiModel

    data class NotSaved(val problem: SaveProblemUiModel) : MedicationEventUiModel
}
