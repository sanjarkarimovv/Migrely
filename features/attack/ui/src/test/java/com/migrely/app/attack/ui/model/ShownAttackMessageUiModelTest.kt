package com.migrely.app.attack.ui.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ShownAttackMessageUiModelTest {
    private val classUnderTest = ShownAttackMessageUiModel(
        AttackMessageUiModel.Undone,
        shownAtEpochMillis = 10_000L
    )

    @Test
    fun `Given 3 seconds passed then 5 of 8 seconds are left`() {
        assertEquals(5_000L, classUnderTest.remainingMillis(13_000L, 8_000L))
    }

    @Test
    fun `Given the time passed then nothing is left`() {
        assertEquals(0L, classUnderTest.remainingMillis(60_000L, 8_000L))
    }
}
