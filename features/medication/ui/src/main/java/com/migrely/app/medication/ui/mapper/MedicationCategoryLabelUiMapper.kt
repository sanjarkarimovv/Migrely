package com.migrely.app.medication.ui.mapper

import androidx.annotation.StringRes
import com.migrely.app.medication.ui.R
import com.migrely.app.medication.ui.model.MedicationCategoryUiModel

/** An acute medication's type, in sentence case as the editor offers it; null is "Not sure" (BRD EC-MED-04). */
class MedicationCategoryLabelUiMapper {
    @StringRes
    fun toUi(category: MedicationCategoryUiModel?): Int = when (category) {
        MedicationCategoryUiModel.SimplePainkiller -> R.string.category_simple
        MedicationCategoryUiModel.Triptan -> R.string.category_triptan
        MedicationCategoryUiModel.CombinationOrOther -> R.string.category_combination
        null -> R.string.category_not_sure
    }
}
