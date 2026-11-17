package com.migrely.app.designsystem.snackbar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
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

/**
 * A message that replaces another one takes its place at once, on every host (BRD D178 (5), D165 (3), QA TB12). Until
 * D178 this was the replaceInPlace option and the default cross-faded; the default case below was inverted on purpose,
 * because the spec changed, not to make a test pass.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ReplaceInPlaceTest {
    @get:Rule
    val rule = createComposeRule()

    /** Two posts of the same text are two messages, as a failed retry is. */
    private data class Posted(val text: String, val number: Int)

    private var message by mutableStateOf<Posted?>(Posted(TEXT, 1))

    private fun show() {
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

    /** The most nodes with the text in any frame while the second post replaces the first. */
    private fun mostNodesWhileReplacing(): Int {
        message = Posted(TEXT, 2)
        Snapshot.sendApplyNotifications()
        return (1..30).maxOf {
            rule.mainClock.advanceTimeByFrame()
            rule.onAllNodes(hasText(TEXT)).fetchSemanticsNodes().size
        }
    }

    // Inverted by D178: until then the default cross-faded, so two nodes showed for 200 ms.
    @Test
    fun `The new message takes the old one's place at once, with no cross-fade`() {
        show()

        assertEquals(1, mostNodesWhileReplacing())
    }

    @Test
    fun `A message that arrives when none shows still comes in as usual`() {
        message = null
        show()

        message = Posted(TEXT, 2)
        Snapshot.sendApplyNotifications()
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeBy(1_000)

        assertEquals(1, rule.onAllNodes(hasText(TEXT)).fetchSemanticsNodes().size)
    }
}
