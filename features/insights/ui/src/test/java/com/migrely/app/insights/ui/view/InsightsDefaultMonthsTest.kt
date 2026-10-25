package com.migrely.app.insights.ui.view

import com.migrely.app.patterns.domain.PatternsReadiness
import org.junit.Assert.assertEquals
import org.junit.Test

class InsightsDefaultMonthsTest {
    @Test
    fun `Insights open on the months the paywall after the 5th migraine looks at`() {
        assertEquals(PatternsReadiness.DEFAULT_MONTHS, DEFAULT_MONTHS)
    }
}
