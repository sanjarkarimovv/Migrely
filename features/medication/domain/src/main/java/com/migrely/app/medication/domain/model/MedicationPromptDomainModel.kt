package com.migrely.app.medication.domain.model

/** The one medication card home shows under today's card, if any (BRD D46). */
sealed interface MedicationPromptDomainModel {
    data object None : MedicationPromptDomainModel

    /** [days] of the last 30 reached [level] for [category] (BRD MED-F5, D45). */
    data class Overuse(
        val category: OveruseCategoryDomainModel,
        val level: OveruseLevelDomainModel,
        val days: Int
    ) : MedicationPromptDomainModel

    data class EffectQuestion(val question: PendingEffectQuestionDomainModel) :
        MedicationPromptDomainModel
}
