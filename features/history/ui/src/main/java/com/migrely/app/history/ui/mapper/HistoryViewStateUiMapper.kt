package com.migrely.app.history.ui.mapper

import com.migrely.app.history.presentation.model.HistoryViewState
import com.migrely.app.history.ui.model.HistoryViewStateUiModel

class HistoryViewStateUiMapper(private val historyMonthUiMapper: HistoryMonthUiMapper) {
    fun toUi(viewState: HistoryViewState): HistoryViewStateUiModel = when (viewState) {
        HistoryViewState.Loading -> HistoryViewStateUiModel.Loading
        is HistoryViewState.NoAttacks -> HistoryViewStateUiModel.NoAttacks(
            historyMonthUiMapper.toUi(viewState.month)
        )
        is HistoryViewState.Ready -> HistoryViewStateUiModel.Ready(
            historyMonthUiMapper.toUi(viewState.month)
        )
    }
}
