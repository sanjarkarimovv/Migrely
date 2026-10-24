package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.BuiltInSymptomPresentationModel
import com.migrely.app.attack.ui.model.BuiltInSymptomUiModel

class BuiltInSymptomUiToPresentationMapper {
    fun toPresentation(builtInSymptom: BuiltInSymptomUiModel): BuiltInSymptomPresentationModel =
        when (
            builtInSymptom
        ) {
            BuiltInSymptomUiModel.Aura -> BuiltInSymptomPresentationModel.Aura
            BuiltInSymptomUiModel.Nausea -> BuiltInSymptomPresentationModel.Nausea
            BuiltInSymptomUiModel.Vomiting -> BuiltInSymptomPresentationModel.Vomiting
            BuiltInSymptomUiModel.LightSensitivity ->
                BuiltInSymptomPresentationModel.LightSensitivity
            BuiltInSymptomUiModel.SoundSensitivity ->
                BuiltInSymptomPresentationModel.SoundSensitivity
            BuiltInSymptomUiModel.SmellSensitivity ->
                BuiltInSymptomPresentationModel.SmellSensitivity
            BuiltInSymptomUiModel.Dizziness -> BuiltInSymptomPresentationModel.Dizziness
            BuiltInSymptomUiModel.NeckPain -> BuiltInSymptomPresentationModel.NeckPain
        }
}
