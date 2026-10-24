package com.migrely.app.medication.presentation.mapper

import com.migrely.app.medication.domain.model.MedicationPromptDomainModel
import com.migrely.app.medication.domain.model.OveruseCategoryDomainModel
import com.migrely.app.medication.domain.model.OveruseLevelDomainModel
import com.migrely.app.medication.domain.model.PendingEffectQuestionDomainModel
import com.migrely.app.medication.presentation.model.EffectQuestionPresentationModel
import com.migrely.app.medication.presentation.model.MedicationPromptViewState
import com.migrely.app.medication.presentation.model.OveruseCategoryPresentationModel
import com.migrely.app.medication.presentation.model.OveruseLevelPresentationModel
import com.migrely.app.medication.presentation.model.OveruseWarningPresentationModel

class MedicationPromptPresentationMapper {
    fun toPresentation(prompt: MedicationPromptDomainModel): MedicationPromptViewState =
        when (prompt) {
            MedicationPromptDomainModel.None -> MedicationPromptViewState.Hidden

            is MedicationPromptDomainModel.EffectQuestion ->
                MedicationPromptViewState.EffectQuestion(question(prompt.question))

            is MedicationPromptDomainModel.Overuse -> MedicationPromptViewState.OveruseWarning(
                OveruseWarningPresentationModel(
                    category = category(prompt.category),
                    level = level(prompt.level),
                    days = prompt.days,
                    thresholdDays = prompt.category.thresholdDays
                )
            )
        }

    private fun question(question: PendingEffectQuestionDomainModel) =
        EffectQuestionPresentationModel(
            intakeId = question.intakeId,
            medicationName = question.medicationName,
            dose = question.dose,
            takenAt = question.takenAt,
            zone = question.zone
        )

    private fun category(category: OveruseCategoryDomainModel) = when (category) {
        OveruseCategoryDomainModel.SimplePainkiller ->
            OveruseCategoryPresentationModel.SimplePainkiller
        OveruseCategoryDomainModel.Triptan -> OveruseCategoryPresentationModel.Triptan
        OveruseCategoryDomainModel.CombinationOrOther ->
            OveruseCategoryPresentationModel.CombinationOrOther
        OveruseCategoryDomainModel.Multiple -> OveruseCategoryPresentationModel.Multiple
    }

    private fun level(level: OveruseLevelDomainModel) = when (level) {
        OveruseLevelDomainModel.Soft -> OveruseLevelPresentationModel.Soft
        OveruseLevelDomainModel.Doctor -> OveruseLevelPresentationModel.Doctor
    }
}
