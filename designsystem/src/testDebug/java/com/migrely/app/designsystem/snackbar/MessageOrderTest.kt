package com.migrely.app.designsystem.snackbar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.withKeyDown
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val TEXT = "Migraine ended"
private const val OTHER = "Undone."
private const val BACK = "back"
private const val INSIGHTS = "insights"
private const val FIRST = "first"
private const val SECOND = "second"
private const val DISMISS = "Dismiss"

/** What a stop is called in these tests: a tag, or "Dismiss" for the message's control. */
private val stopNames = listOf(BACK, INSIGHTS, FIRST, SECOND)

/**
 * The order of a screen with a message (BRD D178 (4), EC-UX-36, ux rules 1–3): the top bar, then the message's text and
 * its control, then the content, for Tab, Shift+Tab and TalkBack; a message never takes focus, and focus clears when
 * it leaves or is replaced.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w400dp-h800dp")
class MessageOrderTest {
    @get:Rule
    val rule = createComposeRule()

    /** Two posts of the same text are two messages. */
    private data class Posted(val text: String, val number: Int)

    private var message by mutableStateOf<Posted?>(Posted(TEXT, 1))
    private lateinit var focusManager: FocusManager

    private fun showScreen(withTopBar: Boolean = true) {
        rule.setContent {
            focusManager = LocalFocusManager.current
            MigrelyTheme {
                MessageLayout(
                    topBar = {
                        if (withTopBar) {
                            Row {
                                TextButton(onClick = {}, Modifier.testTag(BACK)) { Text("Back") }
                                TextButton(onClick = {}, Modifier.testTag(INSIGHTS)) {
                                    Text("Insights")
                                }
                            }
                        }
                    },
                    message = {
                        MigrelySnackbarHost(
                            message = message,
                            reduceMotion = false,
                            topOffset = MESSAGE_OFFSET_BELOW_TOP_BAR
                        ) {
                            MigrelySnackbar(
                                text = it.text,
                                tone = SnackbarTone.Confirmation,
                                action = null,
                                baseDurationMillis = 8_000L,
                                dismissLabel = DISMISS,
                                onDismiss = { message = null }
                            )
                        }
                    },
                    content = {
                        Column {
                            TextButton(onClick = {}, Modifier.testTag(FIRST)) { Text("First") }
                            TextButton(onClick = {}, Modifier.testTag(SECOND)) { Text("Second") }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        rule.waitForIdle()
    }

    /** The name of the focused stop, or null when nothing is focused. */
    private fun focused(): String? {
        val nodes = rule.onAllNodes(isFocused()).fetchSemanticsNodes()
        if (nodes.isEmpty()) return null
        val config = nodes.single().config
        if (DISMISS in config.getOrElse(SemanticsProperties.ContentDescription) { emptyList() }) {
            return DISMISS
        }
        return stopNames.first { name ->
            rule.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.TestTag, name))
                .fetchSemanticsNodes()
                .any { it.id == nodes.single().id }
        }
    }

    private fun move(direction: FocusDirection, times: Int): List<String?> = (1..times).map {
        rule.runOnIdle { focusManager.moveFocus(direction) }
        rule.waitForIdle()
        focused()
    }

    private fun node(tag: String): SemanticsNodeInteraction =
        rule.onNode(SemanticsMatcher.expectValue(SemanticsProperties.TestTag, tag))

    @Test
    fun `Tab from the top goes through the bar, the message's control, then the content`() {
        showScreen()

        assertEquals(listOf(BACK, INSIGHTS, DISMISS, FIRST, SECOND), move(FocusDirection.Next, 5))
    }

    @Test
    fun `A hardware Tab key follows the same order`() {
        showScreen()
        node(BACK).requestFocus()

        val stops = (1..3).map {
            rule.onRoot().performKeyInput { pressKey(Key.Tab) }
            rule.waitForIdle()
            focused()
        }

        assertEquals(listOf(INSIGHTS, DISMISS, FIRST), stops)
    }

    @Test
    fun `Shift+Tab from the content goes back through the message's control to the bar`() {
        showScreen()
        node(FIRST).requestFocus()

        val stops = (1..3).map {
            rule.onRoot().performKeyInput { withKeyDown(Key.ShiftLeft) { pressKey(Key.Tab) } }
            rule.waitForIdle()
            focused()
        }

        assertEquals(listOf(DISMISS, INSIGHTS, BACK), stops)
    }

    @Test
    fun `With no top bar the message's control is the first stop`() {
        showScreen(withTopBar = false)

        assertEquals(listOf(DISMISS, FIRST), move(FocusDirection.Next, 2))
    }

    @Test
    fun `TalkBack's order puts the bar, then the message, then the content`() {
        showScreen()

        // TalkBack follows traversalIndex inside the screen's traversal group, and the text before its control.
        val order = listOf(
            hasText("Back"),
            hasText(TEXT),
            hasContentDescription(DISMISS),
            hasText("First")
        )
            .map { matcher -> traversalPosition(matcher) }

        assertEquals(order.sorted(), order)
        assertEquals(order.distinct(), order)
    }

    @Test
    fun `A message that appears does not move focus`() {
        message = null
        showScreen()
        node(SECOND).requestFocus()

        message = Posted(TEXT, 2)
        rule.waitForIdle()

        assertEquals(SECOND, focused())
    }

    @Test
    fun `When the focused message leaves, focus clears and does not go into the content`() {
        showScreen()
        rule.onNode(hasContentDescription(DISMISS)).requestFocus()

        message = null
        rule.waitForIdle()

        assertEquals(null, focused())
    }

    @Test
    fun `When the focused message is replaced, focus clears and is not carried over`() {
        showScreen()
        rule.onNode(hasContentDescription(DISMISS)).requestFocus()

        message = Posted(OTHER, 2)
        rule.waitForIdle()

        assertEquals(null, focused())
    }

    @Test
    fun `After the focused message leaves, Tab starts again at the first stop`() {
        showScreen()
        rule.onNode(hasContentDescription(DISMISS)).requestFocus()
        message = null
        rule.waitForIdle()

        assertEquals(listOf(BACK), move(FocusDirection.Next, 1))
    }

    @Test
    fun `From its first exit frame a leaving message is out of the semantics tree`() {
        showScreen()
        rule.mainClock.autoAdvance = false

        message = null
        Snapshot.sendApplyNotifications()
        rule.mainClock.advanceTimeByFrame()

        // The merged tree, which is what Android's accessibility tree is built from.
        val texts = rule.onAllNodes(hasText(TEXT)).fetchSemanticsNodes()
        val controls = rule.onAllNodes(hasContentDescription(DISMISS)).fetchSemanticsNodes()
        assertTrue("text nodes $texts, controls $controls", texts.isEmpty() && controls.isEmpty())
    }

    @Test
    fun `From its first exit frame a leaving message is not a Tab stop`() {
        showScreen()
        node(INSIGHTS).requestFocus()
        rule.mainClock.autoAdvance = false

        message = null
        Snapshot.sendApplyNotifications()
        rule.mainClock.advanceTimeByFrame()
        rule.runOnIdle { focusManager.moveFocus(FocusDirection.Next) }
        rule.mainClock.advanceTimeByFrame()

        assertEquals(FIRST, focused())
    }

    /**
     * Where the node matching [matcher] comes in TalkBack's order: the traversal indexes of its traversal groups, from
     * the root down, then its own place among its siblings.
     */
    private fun traversalPosition(matcher: SemanticsMatcher): String {
        val node = rule.onNode(matcher, useUnmergedTree = true).fetchSemanticsNode()
        val path = generateSequence(node) { it.parent }.toList().reversed()
        return path.joinToString("/") { step ->
            val index = step.config.getOrElse(SemanticsProperties.TraversalIndex) { 0f }
            val siblings = step.parent?.children.orEmpty()
            "%08.2f:%03d".format(index + 1_000f, siblings.indexOfFirst { it.id == step.id })
        }
    }
}
