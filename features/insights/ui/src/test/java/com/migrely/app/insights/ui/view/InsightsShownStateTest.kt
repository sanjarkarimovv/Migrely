package com.migrely.app.insights.ui.view

import com.migrely.app.insights.ui.mapper.InsightsShownStateResolver
import com.migrely.app.insights.ui.model.InsightsPatternsUiModel
import com.migrely.app.insights.ui.model.InsightsUiModel
import com.migrely.app.insights.ui.model.InsightsViewStateUiModel
import org.junit.Assert.assertEquals
import org.junit.Test

private fun ready(months: Int) = InsightsViewStateUiModel.Ready(
    InsightsUiModel(months, InsightsPatternsUiModel.NotEnough(months, 0), emptyList())
)

class InsightsShownStateTest {
    private val classUnderTest =
        InsightsShownState(months = 6, resolver = InsightsShownStateResolver())

    @Test
    fun `Given 3 months chosen when its answer comes before a redraw then it is shown`() {
        classUnderTest.take(ready(6))

        classUnderTest.choose(3)
        classUnderTest.take(ready(3))

        assertEquals(3, classUnderTest.months)
        assertEquals(ready(3), classUnderTest.shown)
    }

    @Test
    fun `Given 3 months chosen when 6 months answers late then 3 months stays`() {
        classUnderTest.take(ready(6))
        classUnderTest.choose(3)
        classUnderTest.take(ready(3))

        classUnderTest.take(ready(6))

        assertEquals(ready(3), classUnderTest.shown)
    }

    @Test
    fun `Given nothing answered yet then loading shows`() {
        assertEquals(InsightsViewStateUiModel.Loading, classUnderTest.shown)
        assertEquals(6, classUnderTest.months)
    }
}
