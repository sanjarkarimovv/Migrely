package com.migrely.app.designsystem.input

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// A visible part 200 px tall before the keyboard's frame and 150 px after, its top at 0; a field 300 px tall whose
// line is 100 px tall.
private const val BEFORE = 200
private const val AFTER = 150
private const val FIELD = 300f
private const val LINE = 100f

/**
 * How much of a field shows, and whether a keyboard frame is about to hide it (#259, decision (a')): a field shows
 * with at least one pixel inside, since Compose drops a node whose clipped bounds round to nothing from the
 * accessibility tree; the keyboard hides it when it takes some of a field that showed and leaves less than its line.
 */
class TypingSpaceKeyboardHidesTest {
    @Test
    fun `What shows of a field is its part inside the visible part`() {
        assertEquals(60f, shownInPage(140f, FIELD, BEFORE))
        assertEquals(0f, shownInPage(240f, FIELD, BEFORE))
        assertEquals(BEFORE.toFloat(), shownInPage(-50f, 400f, BEFORE))
        assertEquals(30f, shownInPage(-270f, FIELD, BEFORE))
    }

    @Test
    fun `A keyboard leaving less than the line of a field that showed hides it`() {
        assertTrue(keyboardHides(100f, FIELD, LINE, BEFORE, AFTER))
    }

    @Test
    fun `A keyboard leaving at least the line hides nothing`() {
        assertFalse(keyboardHides(50f, FIELD, LINE, BEFORE, AFTER))
    }

    @Test
    fun `A field the person scrolled wholly away is not hidden by the keyboard`() {
        assertFalse(keyboardHides(240f, FIELD, LINE, BEFORE, AFTER))
    }

    @Test
    fun `A sliver under a pixel before the keyboard counts as already hidden`() {
        assertFalse(keyboardHides(199.5f, FIELD, LINE, BEFORE, AFTER))
        assertTrue(keyboardHides(199f, FIELD, LINE, BEFORE, AFTER))
    }

    @Test
    fun `A field cut at the top, which the keyboard does not reach, is not hidden by it`() {
        assertFalse(keyboardHides(-250f, FIELD, LINE, BEFORE, AFTER))
    }
}
