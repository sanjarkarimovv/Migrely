package com.migrely.app.home.ui.view

import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.snackbar.MESSAGE_OFFSET_BELOW_TOP_BAR
import org.junit.Assert.assertEquals
import org.junit.Test

internal class MessageTopOffsetTest {
    @Test
    fun `The offset below a top bar is 56 dp`() {
        assertEquals(56.dp, MESSAGE_OFFSET_BELOW_TOP_BAR)
    }

    @Test
    fun `On Today in a tall window a message moves below the Settings row`() {
        assertEquals(
            MESSAGE_OFFSET_BELOW_TOP_BAR,
            messageTopOffset(HomeMode.Today, compactHeight = false)
        )
    }

    @Test
    fun `On Today in a short window a message stays clear of Migraine started`() {
        assertEquals(0.dp, messageTopOffset(HomeMode.Today, compactHeight = true))
    }

    @Test
    fun `In attack mode a message keeps its own place in any window`() {
        assertEquals(0.dp, messageTopOffset(HomeMode.Attack, compactHeight = false))
        assertEquals(0.dp, messageTopOffset(HomeMode.Attack, compactHeight = true))
    }
}
