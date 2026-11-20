package com.migrely.app.designsystem.input

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// A visible part 200 px tall, its top at 0; a field 100 px tall.
private const val VISIBLE = 200
private const val FIELD = 100f

/**
 * Whether a field still shows in the visible part, so it stays in the accessibility tree (#259, decision (a')):
 * Compose rounds a node's clipped bounds to whole pixels and drops a node whose bounds round to nothing, so a field
 * shows only with at least one pixel inside.
 */
class TypingSpaceShowsInPageTest {
    @Test
    fun `A field inside the visible part shows`() {
        assertTrue(showsInPage(50f, FIELD, VISIBLE))
    }

    @Test
    fun `A field with one pixel above the bottom edge shows`() {
        assertTrue(showsInPage(199f, FIELD, VISIBLE))
    }

    @Test
    fun `A field with less than a pixel above the bottom edge does not show`() {
        assertFalse(showsInPage(199.5f, FIELD, VISIBLE))
    }

    @Test
    fun `A field wholly below the visible part does not show`() {
        assertFalse(showsInPage(240f, FIELD, VISIBLE))
    }

    @Test
    fun `A field with less than a pixel below the top edge does not show`() {
        assertFalse(showsInPage(-99.5f, FIELD, VISIBLE))
    }

    @Test
    fun `A field taller than the visible part and covering it shows`() {
        assertTrue(showsInPage(-50f, 400f, VISIBLE))
    }
}
