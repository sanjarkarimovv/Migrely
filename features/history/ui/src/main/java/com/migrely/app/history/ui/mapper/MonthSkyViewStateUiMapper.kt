package com.migrely.app.history.ui.mapper

import com.migrely.app.history.presentation.model.MonthSkyViewState
import com.migrely.app.history.ui.model.MonthSkyViewStateUiModel

class MonthSkyViewStateUiMapper(private val historyMonthUiMapper: HistoryMonthUiMapper) {
    fun toUi(viewState: MonthSkyViewState): MonthSkyViewStateUiModel = when (viewState) {
        MonthSkyViewState.Loading -> MonthSkyViewStateUiModel.Loading
        is MonthSkyViewState.Ready -> MonthSkyViewStateUiModel.Ready(
            historyMonthUiMapper.toUi(viewState.month)
        )
        MonthSkyViewState.Unavailable -> MonthSkyViewStateUiModel.Unavailable
    }
}
