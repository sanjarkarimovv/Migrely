package com.migrely.app.insights.ui.mapper

import com.migrely.app.insights.ui.model.InsightsPatternsUiModel
import com.migrely.app.insights.ui.model.InsightsUiModel
import com.migrely.app.insights.ui.model.InsightsViewStateUiModel
import org.junit.Assert.assertEquals
import org.junit.Test

private val LOADING = InsightsViewStateUiModel.Loading
private val UNAVAILABLE = InsightsViewStateUiModel.Unavailable

private fun ready(months: Int) = InsightsViewStateUiModel.Ready(
    InsightsUiModel(months, InsightsPatternsUiModel.NotEnough(months, 0), emptyList())
)

class InsightsShownStateResolverTest {
    private val classUnderTest = InsightsShownStateResolver()

    @Test
    fun `Given 3 then 12 chosen when 12 answers first then 12 stays after 3's late answer`() {
        val first = classUnderTest.resolve(LOADING, ready(12), months = 12)
        val late = classUnderTest.resolve(first, ready(3), months = 12)

        assertEquals(ready(12), first)
        assertEquals(ready(12), late)
    }

    @Test
    fun `Given nothing taken yet when another period answers then still loading`() {
        assertEquals(LOADING, classUnderTest.resolve(LOADING, ready(3), months = 12))
    }

    @Test
    fun `Given a new choice when it has not answered yet then the last taken stays`() {
        assertEquals(ready(6), classUnderTest.resolve(ready(6), ready(3), months = 12))
        assertEquals(ready(6), classUnderTest.resolve(ready(6), LOADING, months = 12))
        assertEquals(ready(12), classUnderTest.resolve(ready(6), ready(12), months = 12))
    }

    @Test
    fun `A failure shows unless the chosen months are already shown`() {
        assertEquals(UNAVAILABLE, classUnderTest.resolve(LOADING, UNAVAILABLE, months = 6))
        assertEquals(UNAVAILABLE, classUnderTest.resolve(ready(3), UNAVAILABLE, months = 6))
        assertEquals(ready(6), classUnderTest.resolve(ready(6), UNAVAILABLE, months = 6))
        assertEquals(ready(6), classUnderTest.resolve(UNAVAILABLE, ready(6), months = 6))
    }
}
