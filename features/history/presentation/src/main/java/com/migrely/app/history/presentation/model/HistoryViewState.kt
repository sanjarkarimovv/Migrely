package com.migrely.app.history.presentation.model

import java.time.YearMonth

/**
 * [requestedMonth] is the month the screen asked for (null for the current one), returned so the screen can ignore a
 * slower answer to a request it has since replaced; [month] is what the domain showed after clamping it.
 */
sealed interface HistoryViewState {
    data object Loading : HistoryViewState

    /** No attack, day mark or attack-medication intake has been logged yet (BRD EC-HIS-01, D99). */
    data class NothingLogged(
        val month: HistoryMonthPresentationModel,
        val requestedMonth: YearMonth?
    ) : HistoryViewState

    data class Ready(val month: HistoryMonthPresentationModel, val requestedMonth: YearMonth?) :
        HistoryViewState
}
