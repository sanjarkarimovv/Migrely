package com.migrely.app.home.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface HomeViewStateUiModel : Parcelable {
    @Parcelize
    data object Loading : HomeViewStateUiModel

    @Parcelize
    data class Today(val today: TodayUiModel) : HomeViewStateUiModel

    @Parcelize
    data class AttackInProgress(val attack: AttackInProgressUiModel) : HomeViewStateUiModel
}
