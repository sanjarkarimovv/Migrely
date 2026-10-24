package com.migrely.app.history.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.history.domain.model.HistoryMonthResultDomainModel
import com.migrely.app.history.domain.usecase.GetHistoryMonthUseCase
import com.migrely.app.history.presentation.mapper.HistoryMonthPresentationMapper
import com.migrely.app.history.presentation.model.MonthSkyPresentationNotification
import com.migrely.app.history.presentation.model.MonthSkyViewState
import com.migrely.app.history.presentation.navigation.MonthSkyPresentationNavigationEvent
import java.time.LocalDate

/**
 * The current month's calendar that home shows under today's card (BRD 18.3 A, D29). It enters again every time its
 * host screen refreshes, so it follows the host's data (design D1).
 */
class MonthSkyViewModel(
    private val getHistoryMonthUseCase: GetHistoryMonthUseCase,
    private val historyMonthPresentationMapper: HistoryMonthPresentationMapper,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<MonthSkyViewState, MonthSkyPresentationNotification>(useCaseExecutor) {
    fun onEnter() {
        getHistoryMonthUseCase(
            value = null,
            onResult = { result ->
                updateViewState(
                    when (result) {
                        is HistoryMonthResultDomainModel.Loaded -> MonthSkyViewState.Ready(
                            historyMonthPresentationMapper.toPresentation(result.month)
                        )

                        HistoryMonthResultDomainModel.Failed -> MonthSkyViewState.Unavailable
                    }
                )
            }
        )
    }

    fun onMonthAction() = emitNavigationEvent(MonthSkyPresentationNavigationEvent.OnViewMonth)

    fun onDayAction(day: LocalDate) =
        emitNavigationEvent(MonthSkyPresentationNavigationEvent.OnViewDay(day))
}
