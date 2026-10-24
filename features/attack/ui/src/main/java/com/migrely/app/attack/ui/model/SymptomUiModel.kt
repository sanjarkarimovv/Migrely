package com.migrely.app.attack.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface SymptomUiModel : Parcelable {
    @Parcelize
    data class BuiltIn(val symptom: BuiltInSymptomUiModel) : SymptomUiModel

    @Parcelize
    data class Custom(val tag: CustomTagUiModel) : SymptomUiModel
}
