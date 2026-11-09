package com.migrely.app.ui.main

import org.junit.Assert.assertEquals
import org.junit.Test

/** What a tap on the visit reminder does (design D6, BRD EC-REM-12, review 4). */
class VisitTapRuleTest {
    private val classUnderTest = VisitTapRule()

    @Test
    fun `Given an attack when the reminder is tapped then the app only comes to the front`() {
        assertEquals(VisitTapAction.BringToFront, classUnderTest.actionFor(attackInProgress = true))
    }

    @Test
    fun `Given no attack when the reminder is tapped then the report opens through History`() {
        assertEquals(
            VisitTapAction.OpenReportThroughHistory,
            classUnderTest.actionFor(attackInProgress = false)
        )
    }
}
