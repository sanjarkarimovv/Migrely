package com.migrely.app.designsystem.snackbar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val TEXT = "Couldn't save. Your phone's storage is full."
private const val OTHER = "Saved."
private const val FRAMES = 30

/**
 * A message is never in the accessibility tree while fully transparent, so TalkBack never announces it empty (BRD D178
 * (5), EC-UX-37, QA TB12, #183). Android leaves out a node that Compose reports as transparent, and a live region added
 * in that frame is spoken with no text.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MessageNeverTransparentTest {
    @get:Rule
    val rule = createComposeRule()

    /** Two posts of the same text are two messages, as a failed retry is. */
    private data class Posted(val text: String, val number: Int)

    private var message by mutableStateOf<Posted?>(null)

    private fun show(first: Posted?) {
        message = first
        rule.setContent {
            MigrelyTheme {
                MigrelySnackbarHost(message, reduceMotion = false) {
                    MigrelySnackbar(it.text, SnackbarTone.Error, null, null, "Dismiss", {})
                }
            }
        }
        rule.waitForIdle()
        rule.mainClock.autoAdvance = false
    }

    /** The frames, over the next [FRAMES], in which a node with [text] is in the tree and fully transparent. */
    private fun transparentFramesAfter(next: Posted?, text: String): Int {
        message = next
        Snapshot.sendApplyNotifications()
        return (1..FRAMES).count {
            rule.mainClock.advanceTimeByFrame()
            rule.onAllNodes(hasText(text), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .any { it.isFullyTransparent() }
        }
    }

    @Test
    fun `A message that arrives when none shows is never transparent`() {
        show(first = null)

        assertEquals(0, transparentFramesAfter(Posted(TEXT, 1), TEXT))
    }

    @Test
    fun `A retry with the same text is never transparent`() {
        show(first = Posted(TEXT, 1))

        assertEquals(0, transparentFramesAfter(Posted(TEXT, 2), TEXT))
    }

    @Test
    fun `A replacement with other text is never transparent`() {
        show(first = Posted(TEXT, 1))

        assertEquals(0, transparentFramesAfter(Posted(OTHER, 2), OTHER))
    }

    @Test
    fun `A replacement during the arrival is never transparent`() {
        show(first = null)
        message = Posted(TEXT, 1)
        Snapshot.sendApplyNotifications()
        repeat(3) { rule.mainClock.advanceTimeByFrame() }

        assertEquals(0, transparentFramesAfter(Posted(OTHER, 2), OTHER))
    }
}

/**
 * Compose's own test of what Android's accessibility tree leaves out (`SemanticsNode.isTransparent`, internal in
 * compose-ui). Read by reflection: if a Compose update renames it, these tests fail loudly instead of passing.
 */
private fun SemanticsNode.isFullyTransparent(): Boolean =
    SemanticsNode::class.java.getMethod("isTransparent\$ui_release").invoke(this) as Boolean
