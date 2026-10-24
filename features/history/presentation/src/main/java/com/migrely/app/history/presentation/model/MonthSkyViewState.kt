package com.migrely.app.history.presentation.model

sealed interface MonthSkyViewState {
    data object Loading : MonthSkyViewState

    data class Ready(val month: HistoryMonthPresentationModel) : MonthSkyViewState

    /** The month could not be read; the card stays out of the way of the host screen. */
    data object Unavailable : MonthSkyViewState
}
