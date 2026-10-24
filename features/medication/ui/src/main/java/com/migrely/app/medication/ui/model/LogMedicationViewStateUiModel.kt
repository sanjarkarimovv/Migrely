package com.migrely.app.medication.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface LogMedicationViewStateUiModel : Parcelable {
    @Parcelize
    data object Loading : LogMedicationViewStateUiModel

    @Parcelize
    data class Ready(val log: MedicationLogUiModel) : LogMedicationViewStateUiModel
}
