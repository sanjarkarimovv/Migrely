package com.migrely.app.medication.presentation.model

/** Home's one medication card (BRD D46). */
sealed interface MedicationPromptViewState {
    data object Hidden : MedicationPromptViewState

    data class EffectQuestion(val question: EffectQuestionPresentationModel) :
        MedicationPromptViewState

    data class OveruseWarning(val warning: OveruseWarningPresentationModel) :
        MedicationPromptViewState
}
