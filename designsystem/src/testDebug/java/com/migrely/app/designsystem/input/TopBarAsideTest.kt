package com.migrely.app.designsystem.input

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// QA's measured line at 200% on API 26 and API 36 (#245 comment 6970307531), and Gboard's landscape keyboard.
private const val LINE = 112
private const val KEYBOARD = 627
private const val SYMBOLS = 700

/**
 * The top bar steps aside only when the line being typed does not fit under it (BRD D192, EC-UX-74, EC-UX-77; ux R5,
 * R9; task 2.1), decided once per keyboard opening. The visible parts are QA's cells: API 36 at 360 dp (Attack details
 * 61 px, Edit and Log name 87 px), API 36 at 411 dp (137 px) and API 26 at 360 dp (156 px).
 */
class TopBarAsideTest {
    private val bar = TopBarAside()

    private fun open(visible: Int, line: Int? = LINE) =
        bar.update(KEYBOARD, KEYBOARD, visible, line)

    @Test
    fun `Attack details in the 360 dp window steps the bar aside`() {
        open(visible = 61)

        assertTrue(bar.aside)
    }

    @Test
    fun `A one-line field in the 360 dp window steps the bar aside`() {
        open(visible = 87)

        assertTrue(bar.aside)
    }

    @Test
    fun `The 411 dp window keeps the bar`() {
        open(visible = 137)

        assertFalse(bar.aside)
    }

    @Test
    fun `API 26 at 360 dp keeps the bar`() {
        // A 64 dp constant (168 px at density 2.625) would move it here (SW4's mutation).
        open(visible = 156)

        assertFalse(bar.aside)
    }

    @Test
    fun `A line exactly as tall as the visible part keeps the bar`() {
        open(visible = LINE)

        assertFalse(bar.aside)
    }

    @Test
    fun `Nothing is decided before the line is known`() {
        open(visible = 61, line = null)
        assertFalse(bar.aside)

        open(visible = 61)

        assertTrue("decided once the line came", bar.aside)
    }

    @Test
    fun `Cursor moves, more lines and scrolling while aside change nothing`() {
        open(visible = 61)

        // The visible part grows as the bar leaves, and the line under the cursor changes.
        bar.update(KEYBOARD, KEYBOARD, visible = 255, line = LINE)
        bar.update(KEYBOARD, KEYBOARD, visible = 255, line = null)

        assertTrue(bar.aside)
    }

    @Test
    fun `A keyboard that changes height while aside keeps the bar aside`() {
        open(visible = 61)

        bar.update(SYMBOLS, KEYBOARD, visible = 400, line = LINE)
        bar.update(SYMBOLS, SYMBOLS, visible = 400, line = LINE)
        bar.update(KEYBOARD, KEYBOARD, visible = 400, line = LINE)

        assertTrue(bar.aside)
    }

    @Test
    fun `A keyboard that changes height keeps the bar where it is`() {
        open(visible = 137)

        bar.update(SYMBOLS, SYMBOLS, visible = 64, line = LINE)

        assertFalse(bar.aside)
    }

    @Test
    fun `A message while aside keeps the bar aside`() {
        open(visible = 61)

        // A message's clearance shortens the visible part; it is not a new opening.
        bar.update(KEYBOARD, KEYBOARD, visible = 10, line = LINE)
        bar.update(KEYBOARD, KEYBOARD, visible = 255, line = LINE)

        assertTrue(bar.aside)
    }

    @Test
    fun `The bar stays aside while the keyboard closes and comes back once it is gone`() {
        open(visible = 61)

        bar.update(0, KEYBOARD / 2, visible = 400, line = LINE)
        assertTrue("back before the keyboard is gone", bar.aside)

        bar.update(0, 0, visible = 400, line = LINE)
        assertFalse(bar.aside)
    }

    @Test
    fun `A new opening decides again`() {
        open(visible = 61)
        bar.update(0, 0, visible = 400, line = LINE)

        open(visible = 137)

        assertFalse(bar.aside)
    }

    @Test
    fun `With no keyboard inset the bar never steps aside`() {
        // A hardware or floating keyboard: focus and a line, but no bottom inset.
        bar.update(0, 0, visible = 10, line = LINE)

        assertFalse(bar.aside)
    }
}
