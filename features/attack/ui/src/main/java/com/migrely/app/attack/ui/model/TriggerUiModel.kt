package com.migrely.app.attack.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface TriggerUiModel : Parcelable {
    @Parcelize
    data class BuiltIn(val trigger: BuiltInTriggerUiModel) : TriggerUiModel

    @Parcelize
    data class Custom(val tag: CustomTagUiModel) : TriggerUiModel
}
