package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.BuiltInSymptomPresentationModel
import com.migrely.app.attack.ui.model.BuiltInSymptomUiModel

class BuiltInSymptomUiMapper {
    fun toUi(builtInSymptom: BuiltInSymptomPresentationModel): BuiltInSymptomUiModel = when (
        builtInSymptom
    ) {
        BuiltInSymptomPresentationModel.Aura -> BuiltInSymptomUiModel.Aura
        BuiltInSymptomPresentationModel.Nausea -> BuiltInSymptomUiModel.Nausea
        BuiltInSymptomPresentationModel.Vomiting -> BuiltInSymptomUiModel.Vomiting
        BuiltInSymptomPresentationModel.LightSensitivity -> BuiltInSymptomUiModel.LightSensitivity
        BuiltInSymptomPresentationModel.SoundSensitivity -> BuiltInSymptomUiModel.SoundSensitivity
        BuiltInSymptomPresentationModel.SmellSensitivity -> BuiltInSymptomUiModel.SmellSensitivity
        BuiltInSymptomPresentationModel.Dizziness -> BuiltInSymptomUiModel.Dizziness
        BuiltInSymptomPresentationModel.NeckPain -> BuiltInSymptomUiModel.NeckPain
    }
}
