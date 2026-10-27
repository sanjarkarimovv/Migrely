package com.migrely.app.insights.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.insights.domain.usecase.GetInsightsUseCase
import com.migrely.app.insights.presentation.mapper.InsightsPresentationMapper
import com.migrely.app.insights.presentation.model.InsightsPresentationNotification
import com.migrely.app.insights.presentation.model.InsightsViewState
import com.migrely.app.insights.presentation.navigation.InsightsPresentationNavigationEvent

/**
 * Insights (BRD INS-F1–F6, D87–D92). The UI keeps the chosen months, 3, 6 or 12, and passes them with each event; each
 * one reads the numbers again.
 */
class InsightsViewModel(
    private val getInsightsUseCase: GetInsightsUseCase,
    private val insightsPresentationMapper: InsightsPresentationMapper,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<InsightsViewState, InsightsPresentationNotification>(useCaseExecutor) {
    fun onEnter(months: Int) = load(months)

    /** The screen shows again, after a rotation or a return to the app, so a new day or entry is counted. */
    fun onResume(months: Int) = load(months)

    fun onPeriodAction(months: Int) = load(months)

    fun onBackAction() = emitNavigationEvent(InsightsPresentationNavigationEvent.OnBack)

    private fun load(months: Int) {
        getInsightsUseCase(
            value = months,
            onResult = { updateViewState(insightsPresentationMapper.toPresentation(it)) }
        )
    }
}
