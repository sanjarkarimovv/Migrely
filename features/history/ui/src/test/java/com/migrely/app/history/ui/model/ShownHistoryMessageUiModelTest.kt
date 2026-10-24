package com.migrely.app.history.ui.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ShownHistoryMessageUiModelTest {
    private val shown = ShownHistoryMessageUiModel(HistoryMessageUiModel.Undone, 1_000L)

    @Test
    fun `Given time left when asked then the rest of the timeout`() {
        assertEquals(3_000L, shown.remainingMillis(nowEpochMillis = 3_000L, timeoutMillis = 5_000L))
    }

    @Test
    fun `Given the timeout has passed when asked then zero`() {
        assertEquals(0L, shown.remainingMillis(nowEpochMillis = 9_000L, timeoutMillis = 5_000L))
    }
}
