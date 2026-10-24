package com.migrely.app.history.presentation.model

sealed interface HistoryViewState {
    data object Loading : HistoryViewState

    /** Nothing has been logged yet (BRD EC-HIS-01). */
    data class NoAttacks(val month: HistoryMonthPresentationModel) : HistoryViewState

    data class Ready(val month: HistoryMonthPresentationModel) : HistoryViewState
}
