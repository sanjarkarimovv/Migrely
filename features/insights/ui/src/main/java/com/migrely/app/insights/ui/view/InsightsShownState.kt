package com.migrely.app.insights.ui.view

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import com.migrely.app.insights.ui.mapper.InsightsShownStateResolver
import com.migrely.app.insights.ui.model.InsightsViewStateUiModel

/**
 * The chosen months and the insights shown for them, in one place, so a result is always weighed against the choice
 * made last, even when it comes before the screen redraws. A copy of the choice taken at composition lagged a frame
 * behind, and an answer for the new months that came first was dropped as a late one (QA #104).
 */
@Stable
internal class InsightsShownState(months: Int, private val resolver: InsightsShownStateResolver) {
    var months by mutableIntStateOf(months)
        private set

    var shown by mutableStateOf<InsightsViewStateUiModel>(InsightsViewStateUiModel.Loading)
        private set

    fun choose(months: Int) {
        this.months = months
    }

    /**
     * Takes [answer] when it is for the chosen months (see [InsightsShownStateResolver]). Called, like [choose], on the
     * main thread only, so the two never interleave.
     */
    fun take(answer: InsightsViewStateUiModel) {
        shown = resolver.resolve(shown, answer, months)
    }

    companion object {
        /** Keeps the chosen months; the insights are read again after a restore. */
        fun saver(resolver: InsightsShownStateResolver) = Saver<InsightsShownState, Int>(
            save = { it.months },
            restore = { InsightsShownState(it, resolver) }
        )
    }
}
