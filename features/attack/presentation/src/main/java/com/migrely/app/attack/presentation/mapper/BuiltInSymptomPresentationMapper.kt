package com.migrely.app.attack.presentation.mapper

import com.migrely.app.attack.domain.model.BuiltInSymptomDomainModel
import com.migrely.app.attack.presentation.model.BuiltInSymptomPresentationModel

class BuiltInSymptomPresentationMapper {
    fun toPresentation(builtInSymptom: BuiltInSymptomDomainModel): BuiltInSymptomPresentationModel =
        when (
            builtInSymptom
        ) {
            BuiltInSymptomDomainModel.Aura -> BuiltInSymptomPresentationModel.Aura
            BuiltInSymptomDomainModel.Nausea -> BuiltInSymptomPresentationModel.Nausea
            BuiltInSymptomDomainModel.Vomiting -> BuiltInSymptomPresentationModel.Vomiting
            BuiltInSymptomDomainModel.LightSensitivity ->
                BuiltInSymptomPresentationModel.LightSensitivity

            BuiltInSymptomDomainModel.SoundSensitivity ->
                BuiltInSymptomPresentationModel.SoundSensitivity

            BuiltInSymptomDomainModel.SmellSensitivity ->
                BuiltInSymptomPresentationModel.SmellSensitivity

            BuiltInSymptomDomainModel.Dizziness -> BuiltInSymptomPresentationModel.Dizziness
            BuiltInSymptomDomainModel.NeckPain -> BuiltInSymptomPresentationModel.NeckPain
        }
}
