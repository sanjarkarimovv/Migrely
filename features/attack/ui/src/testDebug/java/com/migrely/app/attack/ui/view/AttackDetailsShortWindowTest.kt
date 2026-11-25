package com.migrely.app.attack.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import com.migrely.app.attack.ui.R
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

// OutlinedTextField's padding around its text, with no label: where the text layout starts inside the field.
private val FIELD_PADDING = 16.dp

// 8 dp at density 2.625 (ux U1), less a pixel of rounding.
private const val MARGIN_PX = 20f
private const val LINES = 9

/**
 * The note's line being typed shows whole on Attack details in QA's short window (BRD D192, EC-UX-72; #245; task 3.1):
 * API 36 with Gboard at 200% landscape, 360 dp tall, where on v1.3.1 only 40 of the line's 112 px showed under the top
 * bar. The bar steps aside while the keyboard is up, and the line shows with 8 dp on both sides; in the 411 dp window the
 * bar stays. The system's own nonlinear 200%, so the line is the one Android lays out.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], fontScale = 2f)
class AttackDetailsShortWindowTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private var draft by mutableStateOf<String?>(null)

    private val note get() = rule.onNode(hasSetTextAction())
    private val content get() = rule.onNode(hasScrollAction())

    private fun show() {
        rule.runOnUiThread { rule.activity.drawEdgeToEdge() }
        rule.setContent {
            MigrelyTheme {
                AttackDetailsContents(
                    viewState = AttackDetailsViewStateUiModel.Ready(
                        previewDetails().copy(note = "")
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
                    actions = previewActions
                )
            }
        }
        rule.waitForIdle()
        setKeyboard(null)
        // As QA's harness: the page brought to the note before the tap, no drag after the keyboard opens.
        content.performScrollToNode(hasSetTextAction())
        rule.waitForIdle()
    }

    private fun setKeyboard(top: Int?) {
        rule.runOnUiThread { rule.activity.setShortWindowInsets(top) }
        rule.waitForIdle()
    }

    private fun typeNineLines(keyboardTop: Int) {
        note.requestFocus()
        setKeyboard(keyboardTop)
        repeat(LINES) { index ->
            note.performTextInput(if (index == 0) "line 1" else "\nline ${index + 1}")
            rule.waitForIdle()
        }
    }

    private fun layout(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        note.fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action!!(results)
        return results.first()
    }

    private fun density() = rule.activity.resources.displayMetrics.density

    /** The line holding the cursor, in px, in the root's coordinates. */
    private fun cursorLine(): Rect {
        val field = note.getUnclippedBoundsInRoot().top.value * density()
        val offset = note.fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange].end
        val cursor = layout().getCursorRect(offset)
        val top = field + FIELD_PADDING.value * density() + cursor.top
        return Rect(0f, top, 0f, top + cursor.height)
    }

    private fun assertLineShowsWithMargins(step: String) {
        val line = cursorLine()
        val visible = content.fetchSemanticsNode().boundsInRoot
        assertTrue(
            "$step: line ${line.top}..${line.bottom} (${line.height} px) in the visible part " +
                "${visible.top}..${visible.bottom} (${visible.height} px)",
            line.top - visible.top >= MARGIN_PX && visible.bottom - line.bottom >= MARGIN_PX
        )
    }

    private fun backInTree(): Boolean {
        val back = rule.activity.getString(R.string.attack_navigate_back)
        return rule.onAllNodes(hasContentDescription(back)).fetchSemanticsNodes().isNotEmpty()
    }

    @Test
    @Config(qualifiers = WINDOW_360)
    fun `In the 360 dp window the bar steps aside and the line typed at the end shows whole`() {
        show()
        typeNineLines(KEYBOARD_TOP_360_PX)

        assertLineShowsWithMargins("cursor at the end")
        assertTrue("Back is in the tree with the bar aside", !backInTree())
    }

    @Test
    @Config(qualifiers = WINDOW_360)
    fun `In the 360 dp window the line at the start shows whole`() {
        show()
        typeNineLines(KEYBOARD_TOP_360_PX)

        note.performTextInputSelection(TextRange(0))
        rule.waitForIdle()

        assertLineShowsWithMargins("cursor at the start")
    }

    @Test
    @Config(qualifiers = WINDOW_360)
    fun `In the 360 dp window the line in the middle shows whole`() {
        show()
        typeNineLines(KEYBOARD_TOP_360_PX)

        note.performTextInputSelection(TextRange(layout().getLineStart(LINES / 2)))
        rule.waitForIdle()

        assertLineShowsWithMargins("cursor in the middle")
    }

    @Test
    @Config(qualifiers = WINDOW_411)
    fun `In the 411 dp window the bar stays and the line shows whole`() {
        show()
        val barBottom = content.fetchSemanticsNode().boundsInRoot.top
        typeNineLines(KEYBOARD_TOP_411_PX)

        val line = cursorLine()
        val visible = content.fetchSemanticsNode().boundsInRoot
        assertTrue("Back left the tree", backInTree())
        assertEquals("the bar moved", barBottom, visible.top, 0.5f)
        assertTrue(
            "line ${line.top}..${line.bottom} in ${visible.top}..${visible.bottom}",
            line.top >= visible.top && line.bottom <= visible.bottom
        )
    }
}
