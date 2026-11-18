package com.migrely.app.designsystem.input

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.layout.MigrelyInsets
import com.migrely.app.designsystem.theme.MigrelyTheme
import kotlin.math.roundToInt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// The status bar QA measured on API 36 (24 dp at density 2.625), and Attack details' top bar at 200% (74 dp).
private const val STATUS_PX = 63
private val BAR = 74.dp

// Content above and below the note, so the page scrolls both ways.
private val ABOVE = 400.dp
private val BELOW = 400.dp

// OutlinedTextField's padding around its text, with no label: where the text layout starts inside the field.
private val FIELD_PADDING = 16.dp

// The cursor line's clearance, 8 dp (ux U1), in px at density 2.625.
private const val MARGIN_PX = 21

private const val LINES = 9
private const val TOP_BAR = "topBar"
private const val TOLERANCE = 0.5f

/**
 * The line being typed is never cut while only its margins are short (BRD D192, EC-UX-73, ux R2 reduced, task 2.0): in a
 * visible part H with L <= H < L + 2M the note's cursor line shows whole; where H >= L + M the margins stay as the follow
 * gives them today (4 and 21 px at H 137 on API 36), never evened out. Density 2.625 and the system's nonlinear 200% font,
 * as on QA's API 36 emulator, so L is the line Android lays out (112 px), not a constant. The visible part is set by the
 * keyboard, computed from the measured line and the page's measured top.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w914dp-h411dp-land-420dpi", fontScale = 2f)
class TypedLineMarginsTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private var value by mutableStateOf(TextFieldValue(""))

    private val note get() = rule.onNode(hasSetTextAction())
    private val page get() = rule.onNode(hasScrollAction())

    private fun show() {
        rule.runOnUiThread { rule.activity.drawEdgeToEdge() }
        rule.setContent {
            MigrelyTheme {
                val space = rememberTypingSpace()
                Column(Modifier.fillMaxSize()) {
                    // Stands for the top bar: the page's visible part starts under it, as on the four screens.
                    Box(
                        Modifier
                            // First: the bar's bounds are what the space lets it take.
                            .testTag(TOP_BAR)
                            .then(space.barModifier)
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .height(BAR)
                    )
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            // As the screens: the visible part ends at the keyboard's top (BRD D180 (2)).
                            .windowInsetsPadding(
                                MigrelyInsets.barsAndKeyboard.only(WindowInsetsSides.Bottom)
                            )
                            .then(space.pageModifier)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(Modifier.height(ABOVE))
                        ProvideTypingSpace(space) {
                            CursorFollowingTextField(
                                value = value,
                                onValueChange = { value = it },
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(Modifier.height(BELOW))
                    }
                }
            }
        }
        rule.waitForIdle()
        // After the content is attached: attaching dispatches the window's own insets again.
        setInsets(keyboardPx = 0)
    }

    private fun setInsets(keyboardPx: Int) {
        rule.runOnUiThread {
            rule.activity.setInsets(keyboardPx = keyboardPx, statusPx = STATUS_PX)
        }
        rule.waitForIdle()
    }

    private fun layout(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        note.fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action!!(results)
        return results.first()
    }

    private fun density() = rule.activity.resources.displayMetrics.density

    /** The line holding the cursor, in px, in the root's coordinates. */
    private fun cursorLine(): Rect {
        // Unclipped: the node's own bounds are cut to the visible part.
        val field = note.getUnclippedBoundsInRoot().top.value * density()
        val offset = note.fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange].end
        val cursor = layout().getCursorRect(offset)
        val top = field + FIELD_PADDING.value * density() + cursor.top
        return Rect(0f, top, 0f, top + cursor.height)
    }

    private fun visiblePart(): Rect = page.fetchSemanticsNode().boundsInRoot

    /**
     * Opens a keyboard that leaves a visible part of the line's height L plus [extraPx], both measured from layout, the
     * note focused and empty. Returns L.
     */
    private fun openKeyboardLeaving(extraPx: Int): Float {
        show()
        note.requestFocus()
        rule.waitForIdle()
        val line = layout().getCursorRect(0).height
        val root = rule.onRoot().fetchSemanticsNode().size.height
        setInsets(keyboardPx = root - (visiblePart().top + line + extraPx).roundToInt())
        assertEquals("the visible part", line + extraPx, visiblePart().height, TOLERANCE)
        return line
    }

    private fun typeLines() {
        repeat(LINES) { index ->
            note.performTextInput(if (index == 0) "line 1" else "\nline ${index + 1}")
            rule.waitForIdle()
        }
    }

    private fun moveCursorToStart() {
        note.performTextInputSelection(TextRange(0))
        rule.waitForIdle()
    }

    private fun assertBarStays(step: String) {
        val bar = rule.onNodeWithTag(TOP_BAR, useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertEquals(
            "$step: the top bar's height",
            BAR.value + STATUS_PX / density(),
            (bar.bottom - bar.top).value,
            TOLERANCE
        )
    }

    private fun assertLineWhole(step: String) {
        assertBarStays(step)
        val line = cursorLine()
        val visible = visiblePart()
        val cutAbove = (visible.top - line.top).coerceAtLeast(0f)
        val cutBelow = (line.bottom - visible.bottom).coerceAtLeast(0f)
        assertTrue(
            "$step: line ${line.top}..${line.bottom} (${line.height} px) in the visible part " +
                "${visible.top}..${visible.bottom} (${visible.height} px): " +
                "cut by $cutAbove px above and $cutBelow px below",
            cutAbove <= TOLERANCE && cutBelow <= TOLERANCE
        )
    }

    @Test
    fun `In the band the line typed at the end shows whole`() {
        openKeyboardLeaving(extraPx = BAND_EXTRA)

        typeLines()

        assertLineWhole("cursor at the end")
    }

    @Test
    fun `In the band the line at the start shows whole`() {
        openKeyboardLeaving(extraPx = BAND_EXTRA)
        typeLines()

        moveCursorToStart()

        assertLineWhole("cursor at the start")
    }

    private fun assertMargins(step: String, above: Int, below: Int) {
        assertBarStays(step)
        val line = cursorLine()
        val visible = visiblePart()
        assertEquals(
            "$step: margin above the line",
            above.toFloat(),
            line.top - visible.top,
            TOLERANCE
        )
        assertEquals(
            "$step: margin below the line",
            below.toFloat(),
            visible.bottom - line.bottom,
            TOLERANCE
        )
    }

    @Test
    fun `Above the band typing down keeps today's margins`() {
        openKeyboardLeaving(extraPx = QA_411_EXTRA)

        typeLines()

        // QA's API 36 411 dp cell: 21 px above, 4 px below, never evened out.
        assertMargins("cursor at the end", above = MARGIN_PX, below = QA_411_EXTRA - MARGIN_PX)
    }

    @Test
    fun `Above the band moving up keeps today's margins`() {
        openKeyboardLeaving(extraPx = QA_411_EXTRA)
        typeLines()

        moveCursorToStart()

        assertMargins("cursor at the start", above = QA_411_EXTRA - MARGIN_PX, below = MARGIN_PX)
    }

    private companion object {
        // H = 137 px, L = 112 px: QA's API 36 Attack details cell at 411 dp, where the line is whole today.
        const val QA_411_EXTRA = 25

        // H = L + 10 px: inside the band L <= H < L + M, where today's follow is predicted to cut the line (ux 2.2).
        const val BAND_EXTRA = 10
    }
}
