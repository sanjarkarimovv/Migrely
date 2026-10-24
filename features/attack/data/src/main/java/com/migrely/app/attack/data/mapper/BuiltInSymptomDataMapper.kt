package com.migrely.app.attack.data.mapper

import com.migrely.app.attack.domain.model.BuiltInSymptomDomainModel
import com.migrely.app.datasource.attack.model.BuiltInSymptomDataModel

class BuiltInSymptomDataMapper {
    fun toData(symptom: BuiltInSymptomDomainModel): BuiltInSymptomDataModel = when (symptom) {
        BuiltInSymptomDomainModel.Aura -> BuiltInSymptomDataModel.Aura
        BuiltInSymptomDomainModel.Nausea -> BuiltInSymptomDataModel.Nausea
        BuiltInSymptomDomainModel.Vomiting -> BuiltInSymptomDataModel.Vomiting
        BuiltInSymptomDomainModel.LightSensitivity -> BuiltInSymptomDataModel.LightSensitivity
        BuiltInSymptomDomainModel.SoundSensitivity -> BuiltInSymptomDataModel.SoundSensitivity
        BuiltInSymptomDomainModel.SmellSensitivity -> BuiltInSymptomDataModel.SmellSensitivity
        BuiltInSymptomDomainModel.Dizziness -> BuiltInSymptomDataModel.Dizziness
        BuiltInSymptomDomainModel.NeckPain -> BuiltInSymptomDataModel.NeckPain
    }
}
