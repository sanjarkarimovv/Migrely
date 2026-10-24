package com.migrely.app.history.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface MonthSkyViewStateUiModel : Parcelable {
    @Parcelize
    data object Loading : MonthSkyViewStateUiModel

    @Parcelize
    data class Ready(val month: HistoryMonthUiModel) : MonthSkyViewStateUiModel

    @Parcelize
    data object Unavailable : MonthSkyViewStateUiModel
}
