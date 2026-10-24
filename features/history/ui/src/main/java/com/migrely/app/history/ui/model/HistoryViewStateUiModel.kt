package com.migrely.app.history.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface HistoryViewStateUiModel : Parcelable {
    @Parcelize
    data object Loading : HistoryViewStateUiModel

    @Parcelize
    data class NoAttacks(val month: HistoryMonthUiModel) : HistoryViewStateUiModel

    @Parcelize
    data class Ready(val month: HistoryMonthUiModel) : HistoryViewStateUiModel
}
