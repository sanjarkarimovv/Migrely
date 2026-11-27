package com.migrely.app.attack.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performSemanticsAction
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat
import com.migrely.app.attack.ui.model.AttackDetailsViewStateUiModel
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
private const val ADD_PAIN = "Schmerzstärke hinzufügen"
private const val CHANGE_TIME = "Zeit ändern"

// Longer than Compose's 100 ms between accessibility checks, several times over.
private const val SETTLE_MILLIS = 2_000L

/**
 * #279: the pain dialog in German at 200% in a 640 dp wide landscape window, 340 dp high here for the 360 dp
 * emulator's system bars, so the time row shows only in part, as on the device. From the slider TalkBack asks for the
 * time row to be shown and the area scrolls to its end. With TalkBack on before the dialog opens, as a TalkBack user
 * opens it, that scroll names no pane again, so TalkBack says the time row next (BRD D181, D193). QA's walk turned
 * TalkBack on over the open dialog: Compose then names the pane at that first change (Compose 1.9.3 checks panes only
 * on a change), which TalkBack said before the time row. The last test pins that.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "de-w640dp-h340dp-land-xxhdpi", fontScale = 2f)
class PainDialogPaneEventTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun openPainDialog(talkBackFirst: Boolean) {
        rule.runOnUiThread {
            rule.activity.drawEdgeToEdge()
            if (talkBackFirst) rule.activity.turnOnTalkBack()
        }
        rule.setContent {
            MigrelyTheme {
                AttackDetailsContents(
                    viewState = AttackDetailsViewStateUiModel.Ready(previewDetails()),
                    timeUiFormatter = previewTimeFormatter,
                    labels = AttackLabels.Default,
                    localState = AttackDetailsLocalState(
                        timeError = null,
                        safetyCardVisible = false,
                        onDismissSafetyCard = {},
                        onTimeErrorShown = {}
                    ),
                    actions = previewActions
                )
            }
        }
        rule.onNode(hasContentDescription(ADD_PAIN))
            .performSemanticsAction(SemanticsActions.OnClick)
        settle()
        if (!talkBackFirst) {
            rule.runOnUiThread { rule.activity.turnOnTalkBack() }
            settle()
        }
    }

    private fun settle() {
        rule.mainClock.advanceTimeBy(SETTLE_MILLIS)
        rule.waitForIdle()
    }

    private fun paneEvents(): List<String> {
        var events = emptyList<String>()
        rule.runOnUiThread { events = rule.activity.paneEvents() }
        return events
    }

    private fun scrolledPx(): Float = rule.onNode(hasScrollAction() and hasAnyAncestor(isDialog()))
        .fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange].value()

    /**
     * TalkBack's move from the slider to the time row, which shows only in part: it asks for the row to be shown. The
     * pane events that makes Compose send. The row must be in TalkBack's tree and the area must scroll.
     */
    private fun paneEventsOfShowingTheTimeRow(): List<String> {
        val before = paneEvents().size
        val scrolled = scrolledPx()
        val row = rule.onNode(hasContentDescription(CHANGE_TIME, substring = true))
            .fetchSemanticsNode().id
        rule.runOnUiThread {
            val tree = TalkBackTree.ofLatestDialog()
            check(tree.node(row) != null) { "the time row is not in TalkBack's tree" }
            tree.perform(row, AccessibilityActionCompat.ACTION_SHOW_ON_SCREEN.id)
        }
        settle()
        check(scrolledPx() > scrolled) { "the area did not scroll" }
        return paneEvents().drop(before)
    }

    @Test
    fun `With TalkBack on before the dialog opens, its pane is named once, by its title`() {
        openPainDialog(talkBackFirst = true)

        assertEquals(listOf("appeared: $TITLE"), paneEvents())
    }

    @Test
    fun `With TalkBack on before the dialog opens, showing the time row names no pane again`() {
        openPainDialog(talkBackFirst = true)

        assertEquals(emptyList<String>(), paneEventsOfShowingTheTimeRow())
    }

    @Test
    fun `TalkBack turned on over the open dialog hears its pane only when the time row is shown`() {
        openPainDialog(talkBackFirst = false)
        assertEquals("nothing when TalkBack comes on", emptyList<String>(), paneEvents())

        assertEquals(listOf("appeared: $TITLE"), paneEventsOfShowingTheTimeRow())
    }
}
