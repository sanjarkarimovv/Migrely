package com.migrely.app.medication.ui.mapper

import com.migrely.app.medication.presentation.model.IntakeEffectPresentationModel
import com.migrely.app.medication.presentation.model.OveruseCategoryPresentationModel
import com.migrely.app.medication.presentation.model.OveruseLevelPresentationModel
import com.migrely.app.medication.ui.model.IntakeEffectUiModel
import com.migrely.app.medication.ui.model.OveruseCategoryUiModel
import com.migrely.app.medication.ui.model.OveruseLevelUiModel

class MedicationPromptUiToPresentationMapper {
    fun toPresentation(effect: IntakeEffectUiModel): IntakeEffectPresentationModel = when (effect) {
        IntakeEffectUiModel.NotAtAll -> IntakeEffectPresentationModel.NotAtAll
        IntakeEffectUiModel.ALittle -> IntakeEffectPresentationModel.ALittle
        IntakeEffectUiModel.ALot -> IntakeEffectPresentationModel.ALot
        IntakeEffectUiModel.Completely -> IntakeEffectPresentationModel.Completely
    }

    fun toPresentation(category: OveruseCategoryUiModel): OveruseCategoryPresentationModel =
        when (category) {
            OveruseCategoryUiModel.SimplePainkiller ->
                OveruseCategoryPresentationModel.SimplePainkiller
            OveruseCategoryUiModel.Triptan -> OveruseCategoryPresentationModel.Triptan
            OveruseCategoryUiModel.CombinationOrOther ->
                OveruseCategoryPresentationModel.CombinationOrOther
            OveruseCategoryUiModel.Multiple -> OveruseCategoryPresentationModel.Multiple
        }

    fun toPresentation(level: OveruseLevelUiModel): OveruseLevelPresentationModel = when (level) {
        OveruseLevelUiModel.Soft -> OveruseLevelPresentationModel.Soft
        OveruseLevelUiModel.Doctor -> OveruseLevelPresentationModel.Doctor
    }
}
