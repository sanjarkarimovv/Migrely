package com.migrely.app.designsystem.input

import androidx.activity.ComponentActivity
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.layout.MigrelyInsets
import com.migrely.app.designsystem.snackbar.MessageLayout
import com.migrely.app.designsystem.theme.MigrelyTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// QA's API 36 360 dp window (`wm size 945x2100`, landscape): the status bar, and Gboard's landscape keyboard, in px.
private const val STATUS_PX = 63
private const val KEYBOARD_PX = 627

private const val BACK = "Back"
private val BELOW = 300.dp
private const val TOLERANCE = 0.5f

/**
 * Closing the keyboard while the top bar is aside never scrolls (BRD D189, D192, EC-UX-75; ux R7; design D5; task 4.1):
 * the bar comes back over the keyboard's own frames, the scroll offset holds at every frame with no scroll in progress,
 * the page and the field move down by exactly the bar's height, and the keyboard room fills the rest. A page built as the
 * screens build theirs: [MessageLayout] with the space, the visible part ending at the keyboard, the room after the
 * scroll.
 */
@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w800dp-h360dp-land-420dpi", fontScale = 2f)
class TypingSpaceCloseTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val scroll = ScrollState(0)
    private var value by mutableStateOf(TextFieldValue("a note"))

    private val field get() = rule.onNode(hasSetTextAction())

    private fun show() {
        rule.runOnUiThread { rule.activity.drawEdgeToEdge() }
        rule.setContent {
            MigrelyTheme {
                val space = rememberTypingSpace()
                MessageLayout(
                    message = {},
                    topBar = {
                        TopAppBar(
                            title = { Text("Title") },
                            navigationIcon = {
                                IconButton(onClick = {}) {
                                    Icon(MigrelyIcons.Back, contentDescription = BACK)
                                }
                            },
                            windowInsets = MigrelyInsets.bars.only(
                                WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                            )
                        )
                    },
                    typingSpace = space,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val insets = MigrelyInsets.barsAndKeyboard.only(WindowInsetsSides.Bottom)
                    val room = rememberKeyboardRoom(scroll, insets)
                    Column(
                        Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(insets)
                            .then(space.scrollingPage(scroll))
                            .then(room.modifier)
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(Modifier.height(400.dp))
                        CursorFollowingTextField(
                            value = value,
                            onValueChange = { value = it },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(BELOW))
                    }
                }
            }
        }
        rule.waitForIdle()
        rule.runOnUiThread { rule.activity.setInsets(keyboardPx = 0, statusPx = STATUS_PX) }
        rule.waitForIdle()
        field.requestFocus()
        rule.waitForIdle()
    }

    private fun keyboardAnimation(from: Int, to: Int) =
        KeyboardAnimation(rule.activity, STATUS_PX, from, to) { rule.waitForIdle() }

    private fun backInTree(): Boolean =
        rule.onAllNodes(hasContentDescription(BACK)).fetchSemanticsNodes().isNotEmpty()

    private fun density() = rule.activity.resources.displayMetrics.density

    private fun fieldTopPx() = field.getUnclippedBoundsInRoot().top.value * density()

    private fun pageTopPx() = rule.onNode(hasScrollAction()).fetchSemanticsNode().boundsInRoot.top

    /** Opens the keyboard (the bar steps aside), then puts the page [belowPx] px short of its end. */
    private fun openAndScrollShortOfTheEnd(belowPx: Int) {
        show()
        val opening = keyboardAnimation(from = 0, to = KEYBOARD_PX)
        opening.start()
        opening.progress(1f)
        opening.end()
        assertFalse("the bar did not step aside", backInTree())
        runBlocking { scroll.scrollTo(scroll.maxValue - belowPx) }
        rule.waitForIdle()
    }

    /** Closes the keyboard frame by frame: the offset holds and no scroll runs at any frame. */
    private fun closeWithoutAScroll() {
        val offset = scroll.value
        val closing = keyboardAnimation(from = KEYBOARD_PX, to = 0)
        closing.start()
        listOf(0.25f, 0.5f, 0.75f, 1f).forEach { fraction ->
            closing.progress(fraction)
            assertEquals("the offset at $fraction of the close", offset, scroll.value)
            assertFalse("a scroll ran at $fraction of the close", scroll.isScrollInProgress)
        }
        closing.end()
        assertEquals("the offset after the close", offset, scroll.value)
    }

    private fun assertMovedDownByTheBar(fieldBefore: Float, pageBefore: Float) {
        assertTrue("the bar did not come back", backInTree())
        val bar = pageTopPx() - pageBefore
        assertTrue("the page did not move down by a bar: $bar px", bar > STATUS_PX)
        assertEquals(
            "the field moved by other than the bar's $bar px",
            fieldBefore + bar,
            fieldTopPx(),
            TOLERANCE
        )
    }

    @Test
    fun `At the page's end closing brings the bar back and moves the field down by its height`() {
        openAndScrollShortOfTheEnd(belowPx = 0)
        val fieldBefore = fieldTopPx()
        val pageBefore = pageTopPx()

        closeWithoutAScroll()

        assertMovedDownByTheBar(fieldBefore, pageBefore)
    }

    @Test
    fun `Short of the end closing still never scrolls`() {
        openAndScrollShortOfTheEnd(belowPx = SHORT_OF_THE_END_PX)
        val fieldBefore = fieldTopPx()
        val pageBefore = pageTopPx()

        closeWithoutAScroll()

        assertMovedDownByTheBar(fieldBefore, pageBefore)
    }

    @Test
    fun `A keyboard that jumps shut never scrolls either`() {
        openAndScrollShortOfTheEnd(belowPx = 0)
        val offset = scroll.value
        val fieldBefore = fieldTopPx()
        val pageBefore = pageTopPx()

        rule.runOnUiThread { rule.activity.setInsets(keyboardPx = 0, statusPx = STATUS_PX) }
        rule.waitForIdle()

        assertEquals("the offset after the close", offset, scroll.value)
        assertMovedDownByTheBar(fieldBefore, pageBefore)
    }

    private companion object {
        // Less than the keyboard's height below the view, and no whole number of the close's steps.
        const val SHORT_OF_THE_END_PX = 170
    }
}
