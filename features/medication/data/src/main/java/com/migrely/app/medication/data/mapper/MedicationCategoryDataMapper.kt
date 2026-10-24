package com.migrely.app.medication.data.mapper

import com.migrely.app.datasource.medication.model.MedicationCategoryDataModel
import com.migrely.app.medication.domain.model.MedicationCategoryDomainModel

class MedicationCategoryDataMapper {
    fun toData(category: MedicationCategoryDomainModel?): MedicationCategoryDataModel? =
        when (category) {
            MedicationCategoryDomainModel.SimplePainkiller ->
                MedicationCategoryDataModel.SimplePainkiller
            MedicationCategoryDomainModel.Triptan -> MedicationCategoryDataModel.Triptan
            MedicationCategoryDomainModel.CombinationOrOther ->
                MedicationCategoryDataModel.CombinationOrOther
            null -> null
        }
}
