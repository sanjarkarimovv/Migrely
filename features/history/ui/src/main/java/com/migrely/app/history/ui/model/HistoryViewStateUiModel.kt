package com.migrely.app.history.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** [requestedMonth] is the request this state answers, as `YearMonth.toString()`, or null for the current month. */
sealed interface HistoryViewStateUiModel : Parcelable {
    @Parcelize
    data object Loading : HistoryViewStateUiModel

    @Parcelize
    data class NothingLogged(val month: HistoryMonthUiModel, val requestedMonth: String?) :
        HistoryViewStateUiModel

    @Parcelize
    data class Ready(val month: HistoryMonthUiModel, val requestedMonth: String?) :
        HistoryViewStateUiModel
}
