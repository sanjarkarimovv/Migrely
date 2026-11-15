package com.migrely.app.attack.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.migrely.app.attack.ui.model.AttackDetailsViewStateUiModel
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// The cursor line's clearance from the keyboard's top and from the top bar (BRD D180 (2), ux U1).
private val MARGIN = 8.dp

// OutlinedTextField's padding around its text, with no label: where the text layout starts inside the field.
private val FIELD_PADDING = 16.dp

// A tap on the note's third line at 200%, well inside it.
private val TAP_INSIDE = 136.dp

// 996 units, four short of the limit, in words, as a long note is written.
private val LONG_NOTE = buildString {
    while (length < 990) append("woke up with it again ")
}.take(996)

/**
 * The note's cursor line stays above the keyboard (BRD D180 (2), EC-UX-39, EC-UX-44, #141): with a 250 dp keyboard at
 * 200% font, after typing, a focus that keeps the cursor at the end, a tap in the first lines, a paste, and around an
 * autosave, the line with the cursor shows whole, 8 dp clear of the keyboard and below the top bar.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h700dp")
class NoteKeyboardTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private var saved by mutableStateOf("")
    private var draft by mutableStateOf<String?>(null)

    private val note get() = rule.onNode(hasSetTextAction())
    private val content get() = rule.onNode(hasScrollAction())

    private fun show(savedNote: String) {
        saved = savedNote
        rule.runOnUiThread { rule.activity.drawEdgeToEdge() }
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                MigrelyTheme {
                    AttackDetailsContents(
                        viewState = AttackDetailsViewStateUiModel.Ready(
                            previewDetails().copy(note = saved)
                        ),
                        timeUiFormatter = previewTimeFormatter,
                        labels = AttackLabels.Default,
                        localState = AttackDetailsLocalState(
                            timeError = null,
                            safetyCardVisible = false,
                            onDismissSafetyCard = {},
                            onTimeErrorShown = {},
                            noteDraft = draft,
                            onNoteDraft = { draft = it }
                        ),
                        actions = previewActions.copy(onNote = { saved = it })
                    )
                }
            }
        }
        rule.waitForIdle()
        content.performScrollToNode(hasSetTextAction())
        rule.waitForIdle()
    }

    private fun openKeyboard() {
        rule.runOnUiThread { rule.activity.setKeyboard(KEYBOARD_DP) }
        rule.waitForIdle()
    }

    // The window's height, not a root's: a tap adds the cursor handle's popup as a second root.
    private fun keyboardTop(): Dp {
        val view = rule.activity.window.decorView
        return (view.height / view.resources.displayMetrics.density).dp - KEYBOARD_DP.dp
    }

    private fun selection(): TextRange =
        note.fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange]

    private fun layout(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        note.fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action!!(results)
        return results.first()
    }

    /** The line with the cursor, in dp, in the root's coordinates. */
    private fun cursorLine(): Rect = lineAt(selection().end)

    /** The line holding [offset], in dp, in the root's coordinates. */
    private fun lineAt(offset: Int): Rect {
        val field = note.getUnclippedBoundsInRoot()
        val cursor = layout().getCursorRect(offset)
        val density = rule.activity.resources.displayMetrics.density
        val top = field.top.value + FIELD_PADDING.value + cursor.top / density
        val bottom = field.top.value + FIELD_PADDING.value + cursor.bottom / density
        return Rect(0f, top, 0f, bottom)
    }

    private fun scroll(): Float = content.fetchSemanticsNode()
        .config[SemanticsProperties.VerticalScrollAxisRange].value()

    private fun assertCursorLineShows(step: String) {
        val line = cursorLine()
        val keyboardTop = keyboardTop().value
        val viewportTop = content.getUnclippedBoundsInRoot().top.value
        assertTrue(
            "$step: line ${line.top}..${line.bottom}, keyboard at $keyboardTop",
            line.bottom + MARGIN.value <= keyboardTop + 0.5f
        )
        assertTrue(
            "$step: line ${line.top}..${line.bottom}, content's top at $viewportTop",
            line.top >= viewportTop + MARGIN.value - 0.5f
        )
    }

    @Test
    fun `Typing past the keyboard keeps the cursor line above it`() {
        show("")
        note.requestFocus()
        openKeyboard()

        repeat(12) { index ->
            note.performTextInput("line $index of a note ")
            rule.waitForIdle()
            assertCursorLineShows("after typing chunk $index")
        }
    }

    @Test
    fun `A focus that keeps the cursor at the end shows the last line`() {
        show(LONG_NOTE)

        note.requestFocus()
        openKeyboard()

        assertEquals(TextRange(LONG_NOTE.length), selection())
        assertCursorLineShows("after focus")
    }

    @Test
    fun `A tap in the first lines keeps the cursor there and the page off the note's end`() {
        show(LONG_NOTE)
        note.performTouchInput { click(Offset(width / 2f, (FIELD_PADDING + MARGIN).toPx())) }
        rule.waitForIdle()
        val tapped = selection()
        openKeyboard()

        assertTrue("cursor went to $tapped", tapped.end < LONG_NOTE.length / 4)
        assertEquals(tapped, selection())
        assertCursorLineShows("after the tap")
        val end = lineAt(LONG_NOTE.length)
        assertTrue("the page went to the note's end at ${end.top}", end.top > keyboardTop().value)
    }

    @Test
    fun `Typing inside a long note keeps the cursor line above the keyboard`() {
        show(LONG_NOTE.take(700))
        note.performTouchInput { click(Offset(width / 2f, TAP_INSIDE.toPx())) }
        rule.waitForIdle()
        openKeyboard()

        repeat(10) { index ->
            note.performTextInput("then it got worse $index ")
            rule.waitForIdle()
            assertCursorLineShows("after typing chunk $index")
        }
    }

    @Test
    fun `Moving the cursor under the keyboard brings its line up just enough`() {
        show(LONG_NOTE)
        note.performTouchInput { click(Offset(width / 2f, TAP_INSIDE.toPx())) }
        rule.waitForIdle()
        openKeyboard()
        val keyboardTop = keyboardTop().value

        note.performTextInputSelection(TextRange(selection().end + 150))
        rule.waitForIdle()

        assertCursorLineShows("after moving down")
        assertTrue(
            "scrolled more than needed: line ends at ${cursorLine().bottom}",
            cursorLine().bottom >= keyboardTop - MARGIN.value - 2f
        )
    }

    @Test
    fun `Moving the cursor above the content brings its line down below the top bar`() {
        show(LONG_NOTE)
        note.requestFocus()
        openKeyboard()

        note.performTextInputSelection(TextRange(0))
        rule.waitForIdle()

        assertCursorLineShows("after moving up")
    }

    @Test
    fun `A paste keeps the cursor line above the keyboard`() {
        show("")
        note.requestFocus()
        openKeyboard()

        note.performTextInput(LONG_NOTE.take(600))
        rule.waitForIdle()

        assertCursorLineShows("after the paste")
    }

    @Test
    fun `An autosave moves neither the cursor nor the page`() {
        show(LONG_NOTE.take(900))
        note.performTouchInput { click(Offset(width / 2f, TAP_INSIDE.toPx())) }
        rule.waitForIdle()
        openKeyboard()
        val tapped = selection().end
        note.performTextInput("abc")
        rule.waitForIdle()
        val scrolled = scroll()

        rule.mainClock.advanceTimeBy(1_000)
        rule.waitForIdle()
        assertEquals("the autosave did not run", draft, saved)
        note.performTextInput("def")
        rule.waitForIdle()

        assertEquals(TextRange(tapped + 6), selection())
        assertEquals(scrolled, scroll(), 0.5f)
        assertEquals("abcdef", draft!!.substring(tapped, tapped + 6))
    }

    private fun setKeyboard(heightDp: Int) {
        rule.runOnUiThread { rule.activity.setKeyboard(heightDp) }
        rule.waitForIdle()
    }

    /** Scrolls the content to its top, as the user does by hand or the safety card's reveal does (D167). */
    private fun scrollToTop() {
        content.performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, -10_000f) }
        rule.waitForIdle()
    }

    @Test
    fun `Closing the keyboard after scrolling away from the note leaves the page there`() {
        show(LONG_NOTE)
        note.requestFocus()
        openKeyboard()
        scrollToTop()
        val scrolled = scroll()

        setKeyboard(0)

        assertEquals("the page scrolled back", scrolled, scroll(), 0.5f)
    }

    @Test
    fun `A keyboard that closes and opens again keeps the top of the page in view`() {
        show(LONG_NOTE)
        note.requestFocus()
        openKeyboard()
        scrollToTop()

        setKeyboard(0)
        setKeyboard(KEYBOARD_DP)

        assertEquals("the top left the view", 0f, scroll(), 0.5f)
    }
}
