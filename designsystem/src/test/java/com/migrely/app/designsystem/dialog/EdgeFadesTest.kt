package com.migrely.app.designsystem.dialog

import org.junit.Assert.assertEquals
import org.junit.Test

/** A scrolling dialog area fades the edges with more content beyond them, and no other (ux F6). */
class EdgeFadesTest {
    @Test
    fun `Given more below then only the bottom fades`() {
        assertEquals(EdgeFades(top = false, bottom = true), edgeFades(false, true))
    }

    @Test
    fun `Given more above then only the top fades`() {
        assertEquals(EdgeFades(top = true, bottom = false), edgeFades(true, false))
    }

    @Test
    fun `Given nothing beyond either edge then nothing fades`() {
        assertEquals(EdgeFades(top = false, bottom = false), edgeFades(false, false))
    }

    @Test
    fun `Given more on both sides then both fade`() {
        assertEquals(EdgeFades(top = true, bottom = true), edgeFades(true, true))
    }
}
