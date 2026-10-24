package com.migrely.app.home.ui.view

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

// Plain dp values: JUnit cannot pass the value class Dp to a parameterized constructor.
@RunWith(Parameterized::class)
internal class GuidanceTopPaddingTest(
    private val case: String,
    private val messageBottomDp: Float,
    private val columnTopDp: Float,
    private val expectedDp: Float
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            // The message ends 60 dp below the column's top, under a 24 dp status bar (QA #26).
            arrayOf("message over the column", 84f, 24f, 60f),
            arrayOf("no message", 0f, 24f, 0f),
            arrayOf("message above the column", 20f, 24f, 0f),
            // The first frame, before the column is placed, must not guess (review of D52).
            arrayOf("column not placed yet", 84f, Float.NaN, 0f)
        )
    }

    @Test
    fun `When a message shows over the guidance column then the column starts below it`() {
        assertEquals(expectedDp.dp, guidanceTopPadding(messageBottomDp.dp, columnTopDp.dp))
    }
}
