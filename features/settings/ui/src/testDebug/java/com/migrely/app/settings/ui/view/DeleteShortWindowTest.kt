package com.migrely.app.settings.ui.view

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.settings.ui.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val DELETE = "Delete everything"

// Inside the field's outline, which is drawn in the cursor's colour while focused.
private val FIELD_INSIDE = 20.dp

// 8 dp at density 2.625 (ux U1), less a pixel of rounding.
private const val MARGIN_PX = 20f

/**
 * Delete all data decides its row first, then the top bar (BRD D180 (8), D192, EC-UX-72; ux R3 after D7; task 3.3), in
 * QA's API 36 Gboard geometry at 200% landscape: in the 360 dp window the row stays behind the keyboard and the word's
 * input line still does not fit under the bar, so the bar steps aside and the line shows whole; in the 411 dp window the
 * row stays behind and the line fits under the bar, so the bar stays.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], fontScale = 2f)
class DeleteShortWindowTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private var cursorColor = 0
    private var typed by mutableStateOf("")

    private val field get() = rule.onNode(hasSetTextAction())

    /** As QA's harness: a tap on the field, the keyboard, then typing; no drag after it opens. */
    private fun typeWithKeyboardAt(keyboardTop: Int) {
        rule.runOnUiThread { rule.activity.drawEdgeToEdge() }
        rule.setContent {
            MigrelyTheme {
                cursorColor = OutlinedTextFieldDefaults.colors().cursorColor.toArgb()
                DeleteAllDataContents(
                    typed = typed,
                    word = "DELETE",
                    confirmed = false,
                    deleting = false,
                    failed = false,
                    actions = DeleteAllDataActions({ typed = it }, {}, {})
                )
            }
        }
        rule.waitForIdle()
        setKeyboard(null)
        field.performClick()
        rule.waitForIdle()
        setKeyboard(keyboardTop)
        field.performTextInput("DEL")
        rule.waitForIdle()
    }

    private fun setKeyboard(top: Int?) {
        rule.runOnUiThread { rule.activity.setShortWindowInsets(top) }
        rule.waitForIdle()
    }

    private fun drawWindow(): Bitmap {
        val decor = rule.activity.window.decorView
        val bitmap = Bitmap.createBitmap(decor.width, decor.height, Bitmap.Config.ARGB_8888)
        rule.runOnUiThread { decor.draw(Canvas(bitmap)) }
        return bitmap
    }

    /**
     * The caret's rows on screen, in px, as QA measures it: Compose draws the caret over the whole line box, so the
     * longest vertical run of the cursor's colour inside the field (its outline, the same colour, left out) is the part
     * of the line being typed that shows. Null with no caret drawn.
     */
    private fun caretRows(): IntRange? {
        val density = rule.activity.resources.displayMetrics.density
        val bounds = field.getUnclippedBoundsInRoot()
        val inside = (FIELD_INSIDE.value * density).toInt()
        val window = drawWindow()
        val left = (bounds.left.value * density).toInt() + inside
        val right = (bounds.right.value * density).toInt() - inside
        var best: IntRange? = null
        (left until right).forEach { x ->
            var start = -1
            (0..window.height).forEach { y ->
                val caret = y < window.height && window.getPixel(x, y) == cursorColor
                if (caret && start < 0) start = y
                if (!caret && start >= 0) {
                    if (best == null || y - start > best!!.count()) best = start until y
                    start = -1
                }
            }
        }
        return best
    }

    private fun lineHeight(): Int {
        val results = mutableListOf<TextLayoutResult>()
        field.fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action!!(results)
        return (results.first().getLineBottom(0) - results.first().getLineTop(0)).toInt()
    }

    private fun assertInputLineShows(margin: Float) {
        val line = lineHeight()
        val caret = checkNotNull(caretRows()) { "no caret drawn" }
        val visible = rule.onNode(hasScrollAction()).fetchSemanticsNode().boundsInRoot
        assertTrue(
            "${caret.count()} of the line's $line px show, at ${caret.first}..${caret.last + 1}, " +
                "in the visible part ${visible.top}..${visible.bottom} (${visible.height} px)",
            caret.count() >= line - 1 &&
                caret.first - visible.top >= margin &&
                visible.bottom - (caret.last + 1) >= margin
        )
    }

    private fun assertRowBehindTheKeyboard(keyboardTop: Int) {
        val density = rule.activity.resources.displayMetrics.density
        val row = rule.onNodeWithText(DELETE).getUnclippedBoundsInRoot().top.value * density
        assertTrue("the row rides at $row, keyboard at $keyboardTop", row >= keyboardTop)
    }

    private fun backInTree(): Boolean {
        val back = rule.activity.getString(R.string.settings_back)
        return rule.onAllNodes(hasContentDescription(back)).fetchSemanticsNodes().isNotEmpty()
    }

    @Test
    @Config(qualifiers = WINDOW_360)
    fun `In the 360 dp window the row stays behind and the bar steps aside`() {
        typeWithKeyboardAt(KEYBOARD_TOP_360_PX)

        assertRowBehindTheKeyboard(KEYBOARD_TOP_360_PX)
        assertInputLineShows(MARGIN_PX)
        assertFalse("Back is in the tree with the bar aside", backInTree())
    }

    @Test
    @Config(qualifiers = WINDOW_411)
    fun `In the 411 dp window the row stays behind and the bar stays`() {
        typeWithKeyboardAt(KEYBOARD_TOP_411_PX)

        assertRowBehindTheKeyboard(KEYBOARD_TOP_411_PX)
        // Whole; the margins are the follow's, and Robolectric lays a field's line out lower than API 36 does.
        assertInputLineShows(margin = 0f)
        assertTrue("Back left the tree", backInTree())
        assertEquals(
            "the bar's bottom",
            rule.onNode(hasScrollAction()).fetchSemanticsNode().boundsInRoot.top,
            STATUS_PX + BAR_PX,
            1f
        )
    }

    private companion object {
        // The 64 dp bar at density 2.625.
        const val BAR_PX = 168f
    }
}
