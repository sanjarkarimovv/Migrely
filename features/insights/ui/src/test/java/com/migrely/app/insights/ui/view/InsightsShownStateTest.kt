package com.migrely.app.insights.ui.view

import androidx.compose.runtime.saveable.SaverScope
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
    fun `Given 3 then 12 chosen quickly when 3 answers first then 12 shows once it answers`() {
        classUnderTest.take(ready(6))
        classUnderTest.choose(3)
        classUnderTest.choose(12)

        classUnderTest.take(ready(3))
        assertEquals(ready(6), classUnderTest.shown)

        classUnderTest.take(ready(12))
        assertEquals(ready(12), classUnderTest.shown)
    }

    @Test
    fun `Given the screen opens when nothing has answered yet then loading shows`() {
        assertEquals(InsightsViewStateUiModel.Loading, classUnderTest.shown)
        assertEquals(6, classUnderTest.months)
    }

    @Test
    fun `Given 3 months chosen when restored after a rotation then 3 months stay`() {
        val saver = InsightsShownState.saver(InsightsShownStateResolver())
        classUnderTest.take(ready(6))
        classUnderTest.choose(3)

        val saved = checkNotNull(with(saver) { SaverScope { true }.save(classUnderTest) })
        val restored = checkNotNull(saver.restore(saved))

        assertEquals(3, restored.months)
        assertEquals(InsightsViewStateUiModel.Loading, restored.shown)
    }
}
