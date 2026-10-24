package com.migrely.app.medication.ui.mapper

import com.migrely.app.medication.presentation.model.MedicationCategoryPresentationModel
import com.migrely.app.medication.presentation.model.MedicationKindPresentationModel
import com.migrely.app.medication.presentation.model.MedicationPresentationModel
import com.migrely.app.medication.ui.model.MedicationCategoryUiModel
import com.migrely.app.medication.ui.model.MedicationKindUiModel
import com.migrely.app.medication.ui.model.MedicationUiModel

class MedicationUiMapper {
    fun toUi(medication: MedicationPresentationModel) = MedicationUiModel(
        id = medication.id,
        name = medication.name,
        kind = when (medication.kind) {
            MedicationKindPresentationModel.Acute -> MedicationKindUiModel.Acute
            MedicationKindPresentationModel.Preventive -> MedicationKindUiModel.Preventive
        },
        category = when (medication.category) {
            MedicationCategoryPresentationModel.SimplePainkiller ->
                MedicationCategoryUiModel.SimplePainkiller
            MedicationCategoryPresentationModel.Triptan -> MedicationCategoryUiModel.Triptan
            MedicationCategoryPresentationModel.CombinationOrOther ->
                MedicationCategoryUiModel.CombinationOrOther
            null -> null
        },
        defaultDose = medication.defaultDose
    )
}
