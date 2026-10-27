package com.migrely.app.medication.presentation.mapper

import com.migrely.app.medication.domain.model.MedicationPromptDomainModel
import com.migrely.app.medication.domain.model.OveruseLevelDomainModel
import com.migrely.app.medication.domain.model.PendingEffectQuestionDomainModel
import com.migrely.app.medication.presentation.model.EffectQuestionPresentationModel
import com.migrely.app.medication.presentation.model.MedicationPromptViewState
import com.migrely.app.medication.presentation.model.OveruseLevelPresentationModel
import com.migrely.app.medication.presentation.model.OveruseWarningPresentationModel

class MedicationPromptPresentationMapper(
    private val overuseCategoryPresentationMapper: OveruseCategoryPresentationMapper
) {
    fun toPresentation(prompt: MedicationPromptDomainModel): MedicationPromptViewState =
        when (prompt) {
            MedicationPromptDomainModel.None -> MedicationPromptViewState.Hidden

            is MedicationPromptDomainModel.EffectQuestion ->
                MedicationPromptViewState.EffectQuestion(question(prompt.question))

            is MedicationPromptDomainModel.Overuse -> MedicationPromptViewState.OveruseWarning(
                OveruseWarningPresentationModel(
                    category = overuseCategoryPresentationMapper.toPresentation(prompt.category),
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

    private fun level(level: OveruseLevelDomainModel) = when (level) {
        OveruseLevelDomainModel.Soft -> OveruseLevelPresentationModel.Soft
        OveruseLevelDomainModel.Doctor -> OveruseLevelPresentationModel.Doctor
    }
}
