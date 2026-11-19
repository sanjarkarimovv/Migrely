package com.migrely.app.designsystem.input

import org.junit.Assert.assertEquals
import org.junit.Test

// A visible part 200 px tall, its top at 0.
private const val VISIBLE = 200

/**
 * How far the page must scroll for an area to show in its visible part (BRD D194, design D2; task 3.1): 0 where it
 * shows, the distance below or above, its top first where it is taller than the part. In px from the visible part's
 * top, as the page measures it in its own layout pass.
 */
class TypingSpaceScrollToShowTest {
    @Test
    fun `An area inside the visible part needs no scroll`() {
        assertEquals(0f, scrollToShow(50f, 150f, VISIBLE))
    }

    @Test
    fun `An area below the visible part scrolls by what is below`() {
        assertEquals(40f, scrollToShow(150f, 240f, VISIBLE))
    }

    @Test
    fun `An area above the visible part scrolls back by what is above`() {
        assertEquals(-30f, scrollToShow(-30f, 50f, VISIBLE))
    }

    @Test
    fun `An area taller than the visible part shows its top first`() {
        assertEquals(150f, scrollToShow(150f, 400f, VISIBLE))
    }

    @Test
    fun `An area exactly as tall as the visible part shows whole`() {
        assertEquals(10f, scrollToShow(10f, 210f, VISIBLE))
    }
}
