package com.migrely.app.medication.data.mapper

import com.migrely.app.datasource.medication.model.MedicationKindDataModel
import com.migrely.app.medication.domain.model.MedicationKindDomainModel

class MedicationKindDataMapper {
    fun toData(kind: MedicationKindDomainModel): MedicationKindDataModel = when (kind) {
        MedicationKindDomainModel.Acute -> MedicationKindDataModel.Acute
        MedicationKindDomainModel.Preventive -> MedicationKindDataModel.Preventive
    }
}
