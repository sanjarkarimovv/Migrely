package com.migrely.app.medication.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface EditMedicationViewStateUiModel : Parcelable {
    @Parcelize
    data object Loading : EditMedicationViewStateUiModel

    @Parcelize
    data class Ready(val medication: MedicationUiModel?) : EditMedicationViewStateUiModel

    @Parcelize
    data object NotFound : EditMedicationViewStateUiModel
}
