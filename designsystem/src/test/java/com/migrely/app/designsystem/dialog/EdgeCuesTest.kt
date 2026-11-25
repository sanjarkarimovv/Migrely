package com.migrely.app.designsystem.dialog

import org.junit.Assert.assertEquals
import org.junit.Test

/** A scrolling dialog area marks the edges with more content beyond them, and no other: fade and divider (ux F6). */
class EdgeCuesTest {
    @Test
    fun `Given more below then only the bottom shows the cues`() {
        assertEquals(EdgeCues(top = false, bottom = true), edgeCues(false, true))
    }

    @Test
    fun `Given more above then only the top shows the cues`() {
        assertEquals(EdgeCues(top = true, bottom = false), edgeCues(true, false))
    }

    @Test
    fun `Given nothing beyond either edge then no edge shows the cues`() {
        assertEquals(EdgeCues(top = false, bottom = false), edgeCues(false, false))
    }

    @Test
    fun `Given more on both sides then both edges show the cues`() {
        assertEquals(EdgeCues(top = true, bottom = true), edgeCues(true, true))
    }
}
