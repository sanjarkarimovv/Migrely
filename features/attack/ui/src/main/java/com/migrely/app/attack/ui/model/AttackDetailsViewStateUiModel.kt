package com.migrely.app.attack.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface AttackDetailsViewStateUiModel : Parcelable {
    @Parcelize
    data object Loading : AttackDetailsViewStateUiModel

    @Parcelize
    data class Ready(val details: AttackDetailsUiModel) : AttackDetailsViewStateUiModel

    @Parcelize
    data object NotFound : AttackDetailsViewStateUiModel
}
