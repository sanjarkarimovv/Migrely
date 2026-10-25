package com.migrely.app.medication.ui.mapper

import androidx.annotation.StringRes
import com.migrely.app.medication.ui.R
import com.migrely.app.medication.ui.model.MedicationKindUiModel
import com.migrely.app.medication.ui.model.MedicationUiModel

/**
 * What a medication is, for lists and screen readers: its type, or "Preventive". Each is the editor's own choice, so
 * every kind reads in the same sentence case (store polish).
 */
class MedicationTypeLabelUiMapper(
    private val medicationCategoryLabelUiMapper: MedicationCategoryLabelUiMapper
) {
    @StringRes
    fun toUi(medication: MedicationUiModel): Int = when (medication.kind) {
        MedicationKindUiModel.Acute -> medicationCategoryLabelUiMapper.toUi(medication.category)
        MedicationKindUiModel.Preventive -> R.string.kind_preventive
    }
}
