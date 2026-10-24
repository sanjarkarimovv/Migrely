package com.migrely.app.medication.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface SaveProblemUiModel : Parcelable {
    @Parcelize
    data class Duplicate(val existingName: String) : SaveProblemUiModel

    @Parcelize
    data object LimitReached : SaveProblemUiModel

    @Parcelize
    data object InvalidName : SaveProblemUiModel

    @Parcelize
    data object InvalidDose : SaveProblemUiModel
}
