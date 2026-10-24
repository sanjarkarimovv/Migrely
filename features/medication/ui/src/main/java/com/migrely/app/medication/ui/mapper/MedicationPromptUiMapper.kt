package com.migrely.app.medication.ui.mapper

import com.migrely.app.medication.presentation.model.MedicationPromptViewState
import com.migrely.app.medication.presentation.model.OveruseCategoryPresentationModel
import com.migrely.app.medication.presentation.model.OveruseLevelPresentationModel
import com.migrely.app.medication.ui.model.MedicationPromptUiModel
import com.migrely.app.medication.ui.model.OveruseCategoryUiModel
import com.migrely.app.medication.ui.model.OveruseLevelUiModel

class MedicationPromptUiMapper {
    fun toUi(viewState: MedicationPromptViewState): MedicationPromptUiModel = when (viewState) {
        MedicationPromptViewState.Hidden -> MedicationPromptUiModel.Hidden

        is MedicationPromptViewState.EffectQuestion -> MedicationPromptUiModel.EffectQuestion(
            intakeId = viewState.question.intakeId,
            medicationName = viewState.question.medicationName,
            dose = viewState.question.dose,
            takenAtEpochMillis = viewState.question.takenAt.toEpochMilli(),
            zoneId = viewState.question.zone.id
        )

        is MedicationPromptViewState.OveruseWarning -> MedicationPromptUiModel.OveruseWarning(
            category = category(viewState.warning.category),
            level = when (viewState.warning.level) {
                OveruseLevelPresentationModel.Soft -> OveruseLevelUiModel.Soft
                OveruseLevelPresentationModel.Doctor -> OveruseLevelUiModel.Doctor
            },
            days = viewState.warning.days,
            thresholdDays = viewState.warning.thresholdDays
        )
    }

    private fun category(category: OveruseCategoryPresentationModel) = when (category) {
        OveruseCategoryPresentationModel.SimplePainkiller -> OveruseCategoryUiModel.SimplePainkiller
        OveruseCategoryPresentationModel.Triptan -> OveruseCategoryUiModel.Triptan
        OveruseCategoryPresentationModel.CombinationOrOther ->
            OveruseCategoryUiModel.CombinationOrOther
        OveruseCategoryPresentationModel.Multiple -> OveruseCategoryUiModel.Multiple
    }
}
