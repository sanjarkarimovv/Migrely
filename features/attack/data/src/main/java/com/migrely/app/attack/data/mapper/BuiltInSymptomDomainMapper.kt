package com.migrely.app.attack.data.mapper

import com.migrely.app.attack.domain.model.BuiltInSymptomDomainModel
import com.migrely.app.datasource.attack.model.BuiltInSymptomDataModel

class BuiltInSymptomDomainMapper {
    fun toDomain(symptom: BuiltInSymptomDataModel): BuiltInSymptomDomainModel = when (symptom) {
        BuiltInSymptomDataModel.Aura -> BuiltInSymptomDomainModel.Aura
        BuiltInSymptomDataModel.Nausea -> BuiltInSymptomDomainModel.Nausea
        BuiltInSymptomDataModel.Vomiting -> BuiltInSymptomDomainModel.Vomiting
        BuiltInSymptomDataModel.LightSensitivity -> BuiltInSymptomDomainModel.LightSensitivity
        BuiltInSymptomDataModel.SoundSensitivity -> BuiltInSymptomDomainModel.SoundSensitivity
        BuiltInSymptomDataModel.SmellSensitivity -> BuiltInSymptomDomainModel.SmellSensitivity
        BuiltInSymptomDataModel.Dizziness -> BuiltInSymptomDomainModel.Dizziness
        BuiltInSymptomDataModel.NeckPain -> BuiltInSymptomDomainModel.NeckPain
    }
}
