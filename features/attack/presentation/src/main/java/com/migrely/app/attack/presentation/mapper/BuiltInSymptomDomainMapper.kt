package com.migrely.app.attack.presentation.mapper

import com.migrely.app.attack.domain.model.BuiltInSymptomDomainModel
import com.migrely.app.attack.presentation.model.BuiltInSymptomPresentationModel

class BuiltInSymptomDomainMapper {
    fun toDomain(builtInSymptom: BuiltInSymptomPresentationModel): BuiltInSymptomDomainModel =
        when (
            builtInSymptom
        ) {
            BuiltInSymptomPresentationModel.Aura -> BuiltInSymptomDomainModel.Aura
            BuiltInSymptomPresentationModel.Nausea -> BuiltInSymptomDomainModel.Nausea
            BuiltInSymptomPresentationModel.Vomiting -> BuiltInSymptomDomainModel.Vomiting
            BuiltInSymptomPresentationModel.LightSensitivity ->
                BuiltInSymptomDomainModel.LightSensitivity

            BuiltInSymptomPresentationModel.SoundSensitivity ->
                BuiltInSymptomDomainModel.SoundSensitivity

            BuiltInSymptomPresentationModel.SmellSensitivity ->
                BuiltInSymptomDomainModel.SmellSensitivity

            BuiltInSymptomPresentationModel.Dizziness -> BuiltInSymptomDomainModel.Dizziness
            BuiltInSymptomPresentationModel.NeckPain -> BuiltInSymptomDomainModel.NeckPain
        }
}
