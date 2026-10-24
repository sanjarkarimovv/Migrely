package com.migrely.app.attack.ui.model

import org.junit.Assert.assertEquals
import org.junit.Test

private const val MINUTE = 60_000L
private const val HOUR = 60 * MINUTE
private const val NOW = 100 * HOUR

class PastAttackDraftUiModelTest {
    private val draft = PastAttackDraftUiModel(NOW - 2 * HOUR, NOW, "Asia/Tashkent")

    @Test
    fun `Given an earlier start when moving it then the end follows and the duration stays`() {
        val actual = draft.withStart(NOW - 20 * HOUR, latestEndEpochMillis = NOW)

        assertEquals(
            PastAttackDraftUiModel(NOW - 20 * HOUR, NOW - 18 * HOUR, "Asia/Tashkent"),
            actual
        )
    }

    @Test
    fun `Given a later start when moving it then the end stops at the latest end`() {
        val actual = draft.withStart(NOW - 42 * MINUTE, latestEndEpochMillis = NOW)

        assertEquals(NOW, actual.endEpochMillis)
        assertEquals(NOW - 42 * MINUTE, actual.startEpochMillis)
    }

    @Test
    fun `Given a start in the last minute when moving it then the end stays a minute after`() {
        val actual = draft.withStart(NOW, latestEndEpochMillis = NOW)

        assertEquals(NOW + MINUTE, actual.endEpochMillis)
    }
}
