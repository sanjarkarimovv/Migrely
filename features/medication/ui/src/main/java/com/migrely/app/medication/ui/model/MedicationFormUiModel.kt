package com.migrely.app.medication.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** What the user has typed in the medication editor (BRD MED-F1, MED-F2, D35); saveable as a whole. */
@Parcelize
data class MedicationFormUiModel(
    val name: String = "",
    val kind: MedicationKindUiModel = MedicationKindUiModel.Acute,
    val category: MedicationCategoryUiModel? = null,
    val dose: String = ""
) : Parcelable
