package com.migrely.app.home.ui.view

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.snackbar.MESSAGE_OFFSET_BELOW_TOP_BAR
import org.junit.Assert.assertEquals
import org.junit.Test

/** Where a message sits on home (BRD D158, D52, D178 (6), ux Q6): 10 dp below the measured Settings row on Today. */
internal class MessageTopOffsetTest {
    @Test
    fun `The offset below a top bar is 56 dp`() {
        assertEquals(56.dp, MESSAGE_OFFSET_BELOW_TOP_BAR)
    }

    @Test
    fun `On Today before the Settings row is measured a message moves below a 64 dp row`() {
        assertEquals(
            MESSAGE_OFFSET_BELOW_TOP_BAR,
            messageTopOffset(HomeMode.Today, settingsRowHeight = Dp.Unspecified)
        )
    }

    @Test
    fun `On Today at 100 percent the 54 dp row keeps the fixed offset as the least`() {
        assertEquals(MESSAGE_OFFSET_BELOW_TOP_BAR, messageTopOffset(HomeMode.Today, 54.dp))
    }

    @Test
    fun `On Today with a taller row a message moves 10 dp below it, the host adding 8`() {
        assertEquals(69.dp, messageTopOffset(HomeMode.Today, 67.dp))
        assertEquals(104.dp, messageTopOffset(HomeMode.Today, 102.dp))
    }

    @Test
    fun `While loading a message sits as on Today`() {
        assertEquals(104.dp, messageTopOffset(HomeMode.Loading, 102.dp))
    }

    @Test
    fun `In attack mode a message keeps its own place whatever the row`() {
        assertEquals(0.dp, messageTopOffset(HomeMode.Attack, Dp.Unspecified))
        assertEquals(0.dp, messageTopOffset(HomeMode.Attack, 102.dp))
    }
}
