package com.migrely.app.designsystem.snackbar

import android.app.UiAutomation
import android.graphics.Rect
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.migrely.app.designsystem.theme.MigrelyTheme
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.roundToInt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEXT = "Migraine deleted"
private const val CONTROL = "Undo the deletion"
private const val CONTENT = "Content"
private const val ROUNDS = 10
private const val WAIT_MILLIS = 2_000L
private const val POLL_MILLIS = 20L
private const val TAG = "T64Probe"
private const val MANUAL_HOLD_MILLIS = 90_000L

// The grace of design D2: a move inside the message may read outside for less than this.
private const val GRACE_MILLIS = 300L

// Where accessibility focus sits, which TalkBack moves on its own; not something the probe could change.
private val FOCUS_ACTIONS = setOf(
    AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS,
    AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS
)

/** What the screen holds on its ComposeView: the probe, nothing, or the negative control that adds a description. */
private enum class Holder { Probe, Nothing, Describing }

/** One focus event the probe saw, with the window's focus at that moment. */
private data class ProbeEvent(
    val type: Int,
    val inMessage: Boolean,
    val windowFocused: Boolean,
    val atMillis: Long = SystemClock.uptimeMillis()
) {
    override fun toString() = AccessibilityEvent.eventTypeToString(type) +
        " inMessage=$inMessage windowFocused=$windowFocused"
}

/**
 * T64 task 3.1 spike (BRD D182 (2), design D3): can the app see TalkBack's focus on a message's text and control with
 * public APIs only? Pass bar: 10 of 10 on each.
 *
 * It runs only with TalkBack on and skips itself otherwise. Compose sends accessibility-focus events only when an
 * enabled accessibility service is listed, and UiAutomation is never listed, so without TalkBack there is nothing to
 * see. UiAutomation moves the focus, as a TalkBack swipe does, without suppressing TalkBack, so Compose takes its real
 * path. Every event is logged under the tag "T64Probe" with `isWindowFocused`. TalkBack's own menu is checked by hand
 * on the device, from the same log.
 */
@RunWith(AndroidJUnit4::class)
class TalkBackFocusSpikeTest {
    @get:Rule
    val rule = createComposeRule()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val automation: UiAutomation =
        instrumentation.getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES)
    private val events = CopyOnWriteArrayList<ProbeEvent>()
    private val messageInScreen = Rect()
    private var holder by mutableStateOf(Holder.Probe)

    @Before
    fun talkBackIsOn() {
        val manager = instrumentation.targetContext
            .getSystemService(AccessibilityManager::class.java)
        assumeTrue(
            "TalkBack is off: the spike needs a device with TalkBack on",
            manager.isEnabled && manager.isTouchExplorationEnabled
        )
    }

    @Test
    fun theProbeSeesFocusOnTheText() {
        show()

        assertEquals(ROUNDS, rounds(TEXT))
    }

    @Test
    fun theProbeSeesFocusOnTheControl() {
        show()

        assertEquals(ROUNDS, rounds(CONTROL))
    }

    @Test
    fun fromTheTextToTheControlFocusNeverLeavesTheMessage() {
        show()

        // A move sends two clears and then the focus; between them the probe reads no focus for a moment, which the
        // grace covers. A round keeps the message when that moment is shorter than the grace and it ends inside.
        val kept = (1..ROUNDS).count {
            moveFocus(TEXT) && awaitInMessage(true) && systemFocusOn(TEXT) && run {
                val from = events.size
                val moved = moveFocus(CONTROL) && awaitFocusedEvent(from) &&
                    awaitInMessage(true) && systemFocusOn(CONTROL)
                val outside = longestOutsideMillis(events.drop(from))
                Log.i(TAG, "move round: moved=$moved outsideMillis=$outside")
                moved && outside < GRACE_MILLIS && moveFocus(CONTENT) && awaitInMessage(false)
            }
        }

        assertEquals(ROUNDS, kept)
    }

    /**
     * For checks by hand: holds the screen for [MANUAL_HOLD_MILLIS] while real TalkBack gestures and TalkBack's menu
     * are used from outside, and the probe logs. Runs only with the instrumentation argument `t64Manual=true`.
     */
    @Test
    fun holdForChecksByHand() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("t64Manual") == "true")
        show()
        Log.i(TAG, "manual hold: start")

        SystemClock.sleep(MANUAL_HOLD_MILLIS)

        Log.i(TAG, "manual hold: end")
    }

    /** What TalkBack reads, node by node, is the same with and without the probe. */
    @Test
    fun theProbeChangesNothingTalkBackReads() {
        show()
        val withProbe = treeWith(Holder.Probe)

        assertEquals(withProbe, treeWith(Holder.Nothing))
    }

    /** The negative control: a delegate that adds a description changes what TalkBack reads, and the check sees it. */
    @Test
    fun theCheckSeesADelegateThatAddsADescription() {
        show()
        val plain = treeWith(Holder.Nothing)

        assertNotEquals(plain, treeWith(Holder.Describing))
    }

    private fun treeWith(newHolder: Holder): String {
        holder = newHolder
        rule.waitForIdle()
        instrumentation.waitForIdleSync()
        return tree(automation.rootInActiveWindow)
    }

    private fun longestOutsideMillis(moveEvents: List<ProbeEvent>): Long {
        var longest = 0L
        var outsideSince: Long? = null
        moveEvents.forEach { event ->
            val since = outsideSince
            if (!event.inMessage && since == null) outsideSince = event.atMillis
            if (event.inMessage && since != null) {
                longest = maxOf(longest, event.atMillis - since)
                outsideSince = null
            }
        }
        return if (outsideSince == null) longest else Long.MAX_VALUE
    }

    private fun show() {
        rule.setContent {
            val view = LocalView.current
            DisposableEffect(view, holder) {
                val composeView = view.parent as ViewGroup
                composeView.accessibilityDelegate = when (holder) {
                    Holder.Probe -> AccessibilityFocusProbe(::isInMessage) { type, inside ->
                        val event = ProbeEvent(type, inside, view.hasWindowFocus())
                        events += event
                        Log.i(TAG, event.toString())
                    }
                    Holder.Nothing -> null
                    Holder.Describing -> DescribingDelegate()
                }
                onDispose { composeView.accessibilityDelegate = null }
            }
            MigrelyTheme {
                Column {
                    Button(onClick = {}) { Text(CONTENT) }
                    Box(
                        Modifier.onGloballyPositioned { coordinates ->
                            val origin = IntArray(2).also(view::getLocationOnScreen)
                            val bounds = coordinates.boundsInWindow()
                            messageInScreen.set(
                                origin[0] + bounds.left.roundToInt(),
                                origin[1] + bounds.top.roundToInt(),
                                origin[0] + bounds.right.roundToInt(),
                                origin[1] + bounds.bottom.roundToInt()
                            )
                        }
                    ) {
                        MigrelySnackbar(
                            text = TEXT,
                            tone = SnackbarTone.Information,
                            action = SnackbarAction("Undo", spokenLabel = CONTROL, onClick = {}),
                            baseDurationMillis = null,
                            dismissLabel = "Dismiss",
                            onDismiss = {}
                        )
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    private fun tree(node: AccessibilityNodeInfo?, depth: Int = 0): String {
        if (node == null) return ""
        node.refresh()
        val bounds = Rect().also(node::getBoundsInScreen)
        val line = "  ".repeat(depth) + listOf(
            node.className,
            node.text,
            node.contentDescription,
            node.actionList.map { it.id }.filterNot { it in FOCUS_ACTIONS },
            bounds
        ).joinToString(" | ")
        val children = (0 until node.childCount).map { tree(node.getChild(it), depth + 1) }
        return (listOf(line) + children).joinToString("\n")
    }

    private fun isInMessage(node: AccessibilityNodeInfo): Boolean =
        messageInScreen.contains(Rect().also(node::getBoundsInScreen))

    /** One round: focus onto [target], seen inside the message, then onto the content, seen outside. */
    private fun rounds(target: String): Int = (1..ROUNDS).count {
        moveFocus(target) && awaitInMessage(true) && systemFocusOn(target) &&
            moveFocus(CONTENT) && awaitInMessage(false) && systemFocusOn(CONTENT)
    }

    /**
     * Asks for accessibility focus on [label]. The action's own result is only logged: on one run it came back false
     * 2 s after the focus had been given and seen. The round is judged by the probe, checked against Android's own
     * record in [systemFocusOn].
     */
    private fun moveFocus(label: String): Boolean {
        val node = automation.rootInActiveWindow.findByLabel(label)
        val result = node?.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS)
        Log.i(TAG, "move to '$label': found=${node != null} result=$result")
        return node != null
    }

    private fun systemFocusOn(label: String): Boolean = await {
        automation.rootInActiveWindow.findByLabel(label)?.also {
            it.refresh()
        }?.isAccessibilityFocused ==
            true
    }

    private fun awaitInMessage(expected: Boolean): Boolean = await {
        events.lastOrNull { it.type == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED }
            ?.inMessage == expected
    }

    private fun awaitFocusedEvent(from: Int): Boolean = await {
        events.drop(from).any { it.type == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED }
    }

    private fun await(condition: () -> Boolean): Boolean {
        val end = SystemClock.uptimeMillis() + WAIT_MILLIS
        while (SystemClock.uptimeMillis() < end) {
            instrumentation.waitForIdleSync()
            if (condition()) return true
            SystemClock.sleep(POLL_MILLIS)
        }
        return condition()
    }
}

/** The negative control's delegate: it gives the ComposeView a description, which TalkBack would read. */
private class DescribingDelegate : View.AccessibilityDelegate() {
    override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(host, info)
        info.contentDescription = "probe"
    }
}
