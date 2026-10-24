package com.migrely.app.medication.data.mapper

import com.migrely.app.datasource.medication.model.MedicationCategoryDataModel
import com.migrely.app.datasource.medication.model.MedicationDataModel
import com.migrely.app.datasource.medication.model.MedicationKindDataModel
import com.migrely.app.medication.domain.model.MedicationCategoryDomainModel
import com.migrely.app.medication.domain.model.MedicationDomainModel
import com.migrely.app.medication.domain.model.MedicationKindDomainModel

class MedicationDomainMapper {
    fun toDomain(medication: MedicationDataModel) = MedicationDomainModel(
        id = medication.id,
        name = medication.name,
        kind = when (medication.kind) {
            MedicationKindDataModel.Acute -> MedicationKindDomainModel.Acute
            MedicationKindDataModel.Preventive -> MedicationKindDomainModel.Preventive
        },
        category = when (medication.category) {
            MedicationCategoryDataModel.SimplePainkiller ->
                MedicationCategoryDomainModel.SimplePainkiller
            MedicationCategoryDataModel.Triptan -> MedicationCategoryDomainModel.Triptan
            MedicationCategoryDataModel.CombinationOrOther ->
                MedicationCategoryDomainModel.CombinationOrOther
            null -> null
        },
        defaultDose = medication.defaultDose,
        archived = medication.archived
    )
}
