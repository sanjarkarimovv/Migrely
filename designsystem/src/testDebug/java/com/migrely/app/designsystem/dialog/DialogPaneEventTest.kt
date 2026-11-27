package com.migrely.app.designsystem.dialog

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.test.talkback.TalkBackTree
import com.migrely.app.test.talkback.paneEvents
import com.migrely.app.test.talkback.turnOnTalkBack
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val TITLE = "Wie stark war es?"
private const val ANSWERS = 8

// Longer than Compose's 100 ms between accessibility checks, several times over.
private const val SETTLE_MILLIS = 2_000L

// Material's name for a dialog's pane when the dialog names none itself.
private const val MATERIAL_PANE = "Dialog"

private const val PINS_COMPOSE =
    "pins Compose 1.9.3 behaviour (panes are checked only on a change); see the class KDoc"

/**
 * Compose names a dialog's pane to TalkBack once, when the pane first shows with TalkBack on (#279, BRD D181): a later
 * change in the dialog, such as TalkBack's scroll to its next item, sends no second PANE_APPEARED, which would make
 * TalkBack say the title again and cut the item it moved to. With TalkBack turned on over a dialog already open,
 * Compose names the pane at the first change after that instead (Compose 1.9.3 checks panes only on a change); the
 * two "turned on over an open dialog" tests pin that, so a Compose upgrade that changes it shows here. They are also
 * the controls of the "does not name the pane again" tests: the same action does make Compose send a pane event, and
 * it is heard. A plain dialog has no pane at all, so TalkBack names its window instead.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(
    sdk = [35],
    application = Application::class,
    qualifiers = "w640dp-h360dp-land-xxhdpi",
    fontScale = 2f
)
class DialogPaneEventTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private var level by mutableIntStateOf(1)

    /** Shows [dialog] with TalkBack on from the start, or turned on once the dialog shows. */
    private fun show(talkBackFirst: Boolean, dialog: @Composable () -> Unit) {
        if (talkBackFirst) rule.runOnUiThread { rule.activity.turnOnTalkBack() }
        rule.setContent { MigrelyTheme { dialog() } }
        settle()
        if (!talkBackFirst) {
            rule.runOnUiThread { rule.activity.turnOnTalkBack() }
            settle()
        }
    }

    private fun paneEvents(): List<String> {
        var events = emptyList<String>()
        rule.runOnUiThread { events = rule.activity.paneEvents() }
        return events
    }

    /** The pane events [action] makes Compose send. */
    private fun paneEventsOf(action: () -> Unit): List<String> {
        val before = paneEvents().size
        action()
        return paneEvents().drop(before)
    }

    private fun settle() {
        rule.mainClock.advanceTimeBy(SETTLE_MILLIS)
        rule.waitForIdle()
    }

    @Composable
    private fun ScrollingDialog() = MigrelyScrollingAlertDialog(
        title = TITLE,
        onDismissRequest = {},
        confirmButton = { TextButton(onClick = {}) { Text("Hinzufügen") } },
        dismissButton = { TextButton(onClick = {}) { Text("Abbrechen") } },
        wide = true
    ) {
        Text("$level/10")
        repeat(ANSWERS) { index ->
            TextButton(
                onClick = {},
                modifier = Modifier.heightIn(min = 48.dp)
            ) { Text("Antwort ${index + 1}") }
        }
    }

    @Composable
    private fun AlertDialog() = MigrelyAlertDialog(
        onDismissRequest = {},
        confirmButton = { TextButton(onClick = {}) { Text("OK") } },
        title = { Text(TITLE) },
        text = { Text("$level/10") }
    )

    @Composable
    private fun PlainDialog() = MigrelyDialog(onDismissRequest = {}) { Text("$level/10") }

    private fun scrolledPx(): Float = rule.onNode(hasScrollAction()).fetchSemanticsNode()
        .config[SemanticsProperties.VerticalScrollAxisRange].value()

    /** TalkBack's scroll when its next item lies below the visible part. It must move the area. */
    private fun scrollForward() {
        val before = scrolledPx()
        val area = rule.onNode(hasScrollAction()).fetchSemanticsNode().id
        rule.runOnUiThread {
            TalkBackTree.ofLatestDialog()
                .perform(area, AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD)
        }
        settle()
        check(scrolledPx() > before) { "the area did not scroll" }
    }

    private fun changeLevel() {
        rule.runOnIdle { level++ }
        settle()
    }

    @Test
    fun `With TalkBack on, the scrolling dialog's pane is named once, when it shows`() {
        show(talkBackFirst = true) { ScrollingDialog() }

        assertEquals(listOf("appeared: $TITLE"), paneEvents())
    }

    @Test
    fun `With TalkBack on, TalkBack's scroll in the dialog does not name the pane again`() {
        show(talkBackFirst = true) { ScrollingDialog() }

        assertEquals(emptyList<String>(), paneEventsOf(::scrollForward))
    }

    @Test
    fun `With TalkBack on, a change in an alert dialog does not name the pane again`() {
        show(talkBackFirst = true) { AlertDialog() }

        assertEquals(emptyList<String>(), paneEventsOf(::changeLevel))
    }

    @Test
    fun `A plain dialog has no pane, so it sends no pane event, also on a change`() {
        show(talkBackFirst = true) { PlainDialog() }
        rule.onNode(isDialog()).assertExists()

        val panes = rule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.PaneTitle))
            .fetchSemanticsNodes()
        assertEquals(emptyList<String>(), panes.map { it.config[SemanticsProperties.PaneTitle] })
        assertEquals(emptyList<String>(), paneEvents() + paneEventsOf(::changeLevel))
    }

    @Test
    fun `TalkBack turned on over an open scrolling dialog hears the pane at the first scroll`() {
        show(talkBackFirst = false) { ScrollingDialog() }
        assertEquals(
            "nothing when TalkBack comes on; $PINS_COMPOSE",
            emptyList<String>(),
            paneEvents()
        )

        assertEquals(PINS_COMPOSE, listOf("appeared: $TITLE"), paneEventsOf(::scrollForward))
    }

    @Test
    fun `TalkBack turned on over an open alert dialog hears the pane at the first change`() {
        show(talkBackFirst = false) { AlertDialog() }
        assertEquals(
            "nothing when TalkBack comes on; $PINS_COMPOSE",
            emptyList<String>(),
            paneEvents()
        )

        assertEquals(PINS_COMPOSE, listOf("appeared: $MATERIAL_PANE"), paneEventsOf(::changeLevel))
    }
}
