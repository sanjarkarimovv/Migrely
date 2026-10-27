package com.migrely.app.medication.presentation.mapper

import com.migrely.app.medication.domain.model.IntakeEffectDomainModel
import com.migrely.app.medication.domain.model.OveruseCategoryDomainModel
import com.migrely.app.medication.domain.model.OveruseLevelDomainModel
import com.migrely.app.medication.presentation.model.IntakeEffectPresentationModel
import com.migrely.app.medication.presentation.model.OveruseCategoryPresentationModel
import com.migrely.app.medication.presentation.model.OveruseLevelPresentationModel

/** The card's answers and warnings, back to the domain. */
class MedicationPromptDomainMapper {
    fun toDomain(effect: IntakeEffectPresentationModel): IntakeEffectDomainModel = when (effect) {
        IntakeEffectPresentationModel.NotAtAll -> IntakeEffectDomainModel.NotAtAll
        IntakeEffectPresentationModel.ALittle -> IntakeEffectDomainModel.ALittle
        IntakeEffectPresentationModel.ALot -> IntakeEffectDomainModel.ALot
        IntakeEffectPresentationModel.Completely -> IntakeEffectDomainModel.Completely
    }

    fun toDomain(category: OveruseCategoryPresentationModel): OveruseCategoryDomainModel =
        when (category) {
            OveruseCategoryPresentationModel.SimplePainkiller ->
                OveruseCategoryDomainModel.SimplePainkiller
            OveruseCategoryPresentationModel.Triptan -> OveruseCategoryDomainModel.Triptan
            OveruseCategoryPresentationModel.CombinationOrOther ->
                OveruseCategoryDomainModel.CombinationOrOther
            OveruseCategoryPresentationModel.Multiple -> OveruseCategoryDomainModel.Multiple
        }

    fun toDomain(level: OveruseLevelPresentationModel): OveruseLevelDomainModel = when (level) {
        OveruseLevelPresentationModel.Soft -> OveruseLevelDomainModel.Soft
        OveruseLevelPresentationModel.Doctor -> OveruseLevelDomainModel.Doctor
    }
}
