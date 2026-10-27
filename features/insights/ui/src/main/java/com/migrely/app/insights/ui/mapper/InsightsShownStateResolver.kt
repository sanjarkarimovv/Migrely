package com.migrely.app.insights.ui.mapper

import com.migrely.app.insights.ui.model.InsightsViewStateUiModel

/**
 * Which insights the screen shows. A result is taken only when it is for the chosen months: after a quick change of
 * choice, an older request can answer last and must not replace the newer one. The screen shows loading only until the
 * first result it takes; after that the last one taken stays until the next. A failure is shown unless the chosen
 * months' insights are already there.
 */
class InsightsShownStateResolver {
    fun resolve(
        shown: InsightsViewStateUiModel,
        answer: InsightsViewStateUiModel,
        months: Int
    ): InsightsViewStateUiModel = when (answer) {
        is InsightsViewStateUiModel.Ready -> if (answer.insights.months == months) answer else shown
        InsightsViewStateUiModel.Unavailable -> if (isFor(shown, months)) shown else answer
        InsightsViewStateUiModel.Loading -> shown
    }

    private fun isFor(shown: InsightsViewStateUiModel, months: Int) =
        shown is InsightsViewStateUiModel.Ready && shown.insights.months == months
}
