package com.migrely.app.settings.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val DELETE = "Delete everything"
private const val SHORT_KEYBOARD_DP = 200

// The input line's clearance from the keyboard's top (BRD D180 (8), ux U7).
private val MARGIN = 8.dp

// An outlined field with a label: its text starts 16 dp below its box, which lies half the floating label's line
// (bodySmall's 16 sp at the font scale) below the field's top.
private val TEXT_PADDING = 16.dp
private const val LABEL_LINE_SP = 16f
private const val FONT_SCALE = 2f

/**
 * Delete all data with the keyboard open (BRD D180 (8), EC-UX-40, ux U7): the Delete row rides above the keyboard only
 * while the input line plus 8 dp still shows above it, decided once from the keyboard's final height, so the row never
 * jumps while the keyboard moves; otherwise the row stays at the bottom, behind the keyboard, and shows again with the
 * field once the keyboard closes.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DeleteKeyboardTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val field get() = rule.onNode(hasSetTextAction())

    private fun show() {
        rule.runOnUiThread { rule.activity.drawEdgeToEdge() }
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, FONT_SCALE)) {
                MigrelyTheme {
                    DeleteAllDataContents(
                        typed = "",
                        word = "DELETE",
                        confirmed = false,
                        deleting = false,
                        failed = false,
                        actions = DeleteAllDataActions({}, {}, {})
                    )
                }
            }
        }
        rule.waitForIdle()
        field.performClick()
        rule.waitForIdle()
    }

    private fun setKeyboard(heightDp: Int) {
        rule.runOnUiThread { rule.activity.setKeyboard(heightDp) }
        rule.waitForIdle()
    }

    private fun windowBottom(): Float {
        val view = rule.activity.window.decorView
        return view.height / view.resources.displayMetrics.density
    }

    private fun row(): DpRect = rule.onNodeWithText(DELETE).getUnclippedBoundsInRoot()

    /** The bottom of the field's input line, in dp in the root. */
    private fun inputLineBottom(): Float {
        val results = mutableListOf<TextLayoutResult>()
        field.fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action!!(results)
        val density = rule.activity.resources.displayMetrics.density
        val line = results.first().getLineBottom(0) / density
        val labelHalf = LABEL_LINE_SP * FONT_SCALE / 2
        return field.getUnclippedBoundsInRoot().top.value + labelHalf + TEXT_PADDING.value + line
    }

    private fun assertInputLineAbove(edge: Float, what: String) {
        val line = inputLineBottom()
        assertTrue("input line ends at $line, $what at $edge", line + MARGIN.value <= edge + 0.5f)
    }

    private val body get() = rule.onNode(hasScrollAction())

    private fun scroll(): Float =
        body.fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange].value()

    /**
     * With the field focused and a [keyboardDp] keyboard open, scrolls the body to its end, closes the keyboard (in
     * steps if [animated]) and returns the field's top before and at every step after (BRD D189, ux 5.4).
     */
    private fun fieldTopsAcrossAClose(keyboardDp: Int, animated: Boolean): List<Float> {
        show()
        setKeyboard(keyboardDp)
        body.performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, END) }
        rule.waitForIdle()
        assertTrue("the body fits above the keyboard", scroll() > 0f)
        val before = field.getUnclippedBoundsInRoot().top.value
        if (!animated) {
            setKeyboard(0)
            return listOf(before, field.getUnclippedBoundsInRoot().top.value)
        }
        val keyboard = KeyboardAnimation(rule.activity, keyboardDp, 0)
        rule.runOnUiThread { keyboard.start() }
        val tops = FRACTIONS.map { fraction ->
            rule.runOnUiThread { keyboard.frame(fraction) }
            rule.waitForIdle()
            field.getUnclippedBoundsInRoot().top.value
        }
        rule.runOnUiThread { keyboard.end() }
        rule.waitForIdle()
        return listOf(before) + tops + field.getUnclippedBoundsInRoot().top.value
    }

    private fun assertStill(tops: List<Float>) =
        tops.forEach { assertEquals("the field moved: $tops", tops.first(), it, 0.5f) }

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `With room the row rides above the keyboard under the input line`() {
        show()

        setKeyboard(KEYBOARD_DP)

        val keyboardTop = windowBottom() - KEYBOARD_DP
        assertTrue(
            "row ${row()} under the keyboard at $keyboardTop",
            row().bottom.value <= keyboardTop + 0.5f
        )
        assertInputLineAbove(row().top.value, "the row")
    }

    @Test
    @Config(sdk = [35], qualifiers = "w800dp-h360dp")
    fun `In a short window the row stays behind the keyboard and the input line shows`() {
        show()
        val rowBefore = row()

        setKeyboard(SHORT_KEYBOARD_DP)

        assertEquals("the row moved", rowBefore, row())
        assertInputLineAbove(windowBottom() - SHORT_KEYBOARD_DP, "the keyboard")
    }

    @Test
    @Config(sdk = [35], qualifiers = "w800dp-h360dp")
    fun `In a short window Done shows the field and the row together`() {
        show()
        setKeyboard(SHORT_KEYBOARD_DP)

        setKeyboard(0)

        assertTrue("row ${row()} past the window", row().bottom.value <= windowBottom() + 0.5f)
        assertTrue(
            "the field ${field.getUnclippedBoundsInRoot()} under the row ${row()}",
            field.getUnclippedBoundsInRoot().bottom <= row().top
        )
    }

    @Test
    @Config(sdk = [35], qualifiers = "w800dp-h360dp")
    fun `An opening keyboard that leaves no room never moves the row`() {
        show()
        val rowBefore = row().top
        val keyboard = KeyboardAnimation(rule.activity, 0, SHORT_KEYBOARD_DP)
        rule.runOnUiThread { keyboard.start() }

        val rowTops = FRACTIONS.map { fraction ->
            rule.runOnUiThread { keyboard.frame(fraction) }
            rule.waitForIdle()
            row().top
        }
        rule.runOnUiThread { keyboard.end() }
        rule.waitForIdle()

        assertEquals(FRACTIONS.map { rowBefore }, rowTops)
        assertEquals(rowBefore, row().top)
    }

    @Test
    @Config(sdk = [35], qualifiers = "w800dp-h360dp")
    fun `A closing keyboard that left no room never moves the row`() {
        show()
        val rowBefore = row().top
        setKeyboard(SHORT_KEYBOARD_DP)
        val keyboard = KeyboardAnimation(rule.activity, SHORT_KEYBOARD_DP, 0)
        rule.runOnUiThread { keyboard.start() }

        val rowTops = FRACTIONS.map { fraction ->
            rule.runOnUiThread { keyboard.frame(fraction) }
            rule.waitForIdle()
            row().top
        }
        rule.runOnUiThread { keyboard.end() }
        rule.waitForIdle()

        assertEquals(FRACTIONS.map { rowBefore }, rowTops)
        assertEquals(rowBefore, row().top)
    }

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `An opening keyboard that leaves room carries the row up with it`() {
        show()
        val keyboard = KeyboardAnimation(rule.activity, 0, KEYBOARD_DP)
        rule.runOnUiThread { keyboard.start() }

        val rowBottoms = FRACTIONS.map { fraction ->
            rule.runOnUiThread { keyboard.frame(fraction) }
            rule.waitForIdle()
            row().bottom.value to windowBottom() - KEYBOARD_DP * fraction
        }
        rule.runOnUiThread { keyboard.end() }
        rule.waitForIdle()

        // Above the keyboard's top at every frame, and never down again.
        rowBottoms.forEach { (row, keyboardTop) ->
            assertTrue(
                "row $row, keyboard $keyboardTop",
                row <= keyboardTop + 0.5f
            )
        }
        assertEquals(rowBottoms.map { it.first }.sortedDescending(), rowBottoms.map { it.first })
    }

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `With the row riding, closing the keyboard near the end leaves the field where it is`() =
        assertStill(fieldTopsAcrossAClose(KEYBOARD_DP, animated = false))

    @Test
    @Config(sdk = [35], qualifiers = "w800dp-h360dp")
    fun `With the row behind the keyboard, closing it near the end leaves the field where it is`() =
        assertStill(fieldTopsAcrossAClose(SHORT_KEYBOARD_DP, animated = false))

    @Test
    @Config(sdk = [35], qualifiers = "w800dp-h360dp")
    fun `With the row behind, a closing keyboard never moves the field near the end`() =
        assertStill(fieldTopsAcrossAClose(SHORT_KEYBOARD_DP, animated = true))

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `With the row riding, a closing keyboard never moves the field near the end`() =
        assertStill(fieldTopsAcrossAClose(KEYBOARD_DP, animated = true))

    private companion object {
        val FRACTIONS = listOf(0.25f, 0.5f, 0.75f, 1f)

        // Past any body's end: the scroll stops there.
        const val END = 10_000f
    }
}
