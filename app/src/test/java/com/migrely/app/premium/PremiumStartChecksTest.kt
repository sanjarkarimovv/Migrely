package com.migrely.app.premium

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumStartChecksTest {
    private var refreshes = 0
    private var messages = 0
    private val classUnderTest = PremiumStartChecks({ refreshes++ }, { messages++ })

    @Test
    fun `Given the app comes back then Premium is checked each time`() {
        classUnderTest.onResume()
        classUnderTest.onResume()

        assertEquals(2, refreshes)
    }

    @Test
    fun `Given the first screen shows then Play's messages are asked for once`() {
        classUnderTest.onFirstScreen(startAttack = false)
        classUnderTest.onFirstScreen(startAttack = false)

        assertEquals(1, messages)
        assertTrue(classUnderTest.paymentMessagesAsked)
    }

    @Test
    fun `Given the app opened to start an attack then Play's messages never come`() {
        classUnderTest.onFirstScreen(startAttack = true)
        classUnderTest.onFirstScreen(startAttack = false)

        assertEquals(0, messages)
    }

    @Test
    fun `Given they were asked before a rotation then they are not asked again`() {
        classUnderTest.paymentMessagesAsked = true

        classUnderTest.onFirstScreen(startAttack = false)

        assertEquals(0, messages)
    }
}
