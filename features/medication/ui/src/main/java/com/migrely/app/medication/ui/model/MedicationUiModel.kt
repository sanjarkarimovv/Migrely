package com.migrely.app.medication.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** [category] is null for preventive medications and for "Not sure". */
@Parcelize
data class MedicationUiModel(
    val id: String,
    val name: String,
    val kind: MedicationKindUiModel,
    val category: MedicationCategoryUiModel?,
    val defaultDose: String?
) : Parcelable
