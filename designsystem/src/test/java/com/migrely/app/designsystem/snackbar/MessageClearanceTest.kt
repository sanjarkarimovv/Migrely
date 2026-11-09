package com.migrely.app.designsystem.snackbar

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

// Plain dp values: JUnit cannot pass the value class Dp to a parameterized constructor.
@RunWith(Parameterized::class)
internal class MessageClearanceTest(
    private val case: String,
    private val messageBottomDp: Float,
    private val contentTopDp: Float,
    private val expectedDp: Float
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            // The message ends 60 dp below the content's top, under a 24 dp status bar (QA #26).
            arrayOf("message over the content", 84f, 24f, 60f),
            arrayOf("no message", 0f, 24f, 0f),
            arrayOf("message above the content", 20f, 24f, 0f),
            // The first frame, before the content is placed, must not guess (review of D52).
            arrayOf("content not placed yet", 84f, Float.NaN, 0f)
        )
    }

    @Test
    fun `When a message shows over the content then the content starts below it`() {
        assertEquals(expectedDp.dp, messageClearance(messageBottomDp.dp, contentTopDp.dp))
    }
}
