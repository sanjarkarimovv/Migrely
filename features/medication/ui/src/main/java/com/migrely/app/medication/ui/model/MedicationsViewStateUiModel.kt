package com.migrely.app.medication.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface MedicationsViewStateUiModel : Parcelable {
    @Parcelize
    data object Loading : MedicationsViewStateUiModel

    @Parcelize
    data class Ready(val medications: List<MedicationUiModel>) : MedicationsViewStateUiModel
}
