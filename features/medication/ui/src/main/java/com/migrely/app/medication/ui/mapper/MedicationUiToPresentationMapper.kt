package com.migrely.app.medication.ui.mapper

import com.migrely.app.medication.presentation.model.MedicationCategoryPresentationModel
import com.migrely.app.medication.presentation.model.MedicationKindPresentationModel
import com.migrely.app.medication.presentation.model.MedicationPresentationModel
import com.migrely.app.medication.ui.model.MedicationCategoryUiModel
import com.migrely.app.medication.ui.model.MedicationKindUiModel
import com.migrely.app.medication.ui.model.MedicationUiModel

class MedicationUiToPresentationMapper(
    private val kindMapper: MedicationKindUiToPresentationMapper,
    private val categoryMapper: MedicationCategoryUiToPresentationMapper
) {
    fun toPresentation(medication: MedicationUiModel) = MedicationPresentationModel(
        id = medication.id,
        name = medication.name,
        kind = kindMapper.toPresentation(medication.kind),
        category = categoryMapper.toPresentation(medication.category),
        defaultDose = medication.defaultDose
    )
}

class MedicationKindUiToPresentationMapper {
    fun toPresentation(kind: MedicationKindUiModel): MedicationKindPresentationModel = when (kind) {
        MedicationKindUiModel.Acute -> MedicationKindPresentationModel.Acute
        MedicationKindUiModel.Preventive -> MedicationKindPresentationModel.Preventive
    }
}

class MedicationCategoryUiToPresentationMapper {
    fun toPresentation(category: MedicationCategoryUiModel?): MedicationCategoryPresentationModel? =
        when (category) {
            MedicationCategoryUiModel.SimplePainkiller ->
                MedicationCategoryPresentationModel.SimplePainkiller
            MedicationCategoryUiModel.Triptan -> MedicationCategoryPresentationModel.Triptan
            MedicationCategoryUiModel.CombinationOrOther ->
                MedicationCategoryPresentationModel.CombinationOrOther
            null -> null
        }
}
