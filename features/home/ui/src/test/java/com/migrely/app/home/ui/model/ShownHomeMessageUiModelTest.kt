package com.migrely.app.home.ui.model

import org.junit.Assert.assertEquals
import org.junit.Test

private const val SHOWN_AT = 1_000_000L
private const val TIMEOUT = 5_000L

class ShownHomeMessageUiModelTest {
    private val classUnderTest =
        ShownHomeMessageUiModel(HomeMessageUiModel.AttackStarted("a"), SHOWN_AT)

    @Test
    fun `Given the message just appeared then the whole timeout is left`() {
        assertEquals(TIMEOUT, classUnderTest.remainingMillis(SHOWN_AT, TIMEOUT))
    }

    @Test
    fun `Given 2 seconds passed, as after a rotation, then 3 seconds are left`() {
        assertEquals(3_000L, classUnderTest.remainingMillis(SHOWN_AT + 2_000L, TIMEOUT))
    }

    @Test
    fun `Given the app returns after the timeout then nothing is left`() {
        assertEquals(0L, classUnderTest.remainingMillis(SHOWN_AT + 60_000L, TIMEOUT))
    }
}
