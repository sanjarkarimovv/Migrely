package com.migrely.app.designsystem.snackbar

import android.app.UiAutomation
import android.graphics.Rect
import android.os.SystemClock
import android.util.Log
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

/** One focus event the probe saw, with the window's focus at that moment. */
private data class ProbeEvent(val type: Int, val inMessage: Boolean, val windowFocused: Boolean) {
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
 * path. Every event is logged under the tag "T64Probe" with `isWindowFocused`. TalkBack's own menu is checked by hand on
 * the device, from the same log.
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
    private var probeOn by mutableStateOf(true)

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

        val kept = (1..ROUNDS).count {
            moveFocus(TEXT) && awaitInMessage(true) && run {
                val from = events.size
                // Compose clears the text before it focuses the control; the clear must still read inside.
                moveFocus(CONTROL) && awaitFocusedEvent(from) &&
                    events.drop(from).all { it.inMessage } &&
                    moveFocus(CONTENT) && awaitInMessage(false)
            }
        }

        assertEquals(ROUNDS, kept)
    }

    /** What TalkBack reads, node by node, is the same with and without the probe. */
    @Test
    fun theProbeChangesNothingTalkBackReads() {
        show()
        val withProbe = tree(automation.rootInActiveWindow)

        probeOn = false
        rule.waitForIdle()
        instrumentation.waitForIdleSync()

        assertEquals(withProbe, tree(automation.rootInActiveWindow))
    }

    private fun show() {
        rule.setContent {
            val view = LocalView.current
            if (probeOn) {
                DisposableEffect(view) {
                    val holder = view.parent as ViewGroup
                    holder.accessibilityDelegate =
                        AccessibilityFocusProbe(::isInMessage) { type, inside ->
                            val event = ProbeEvent(type, inside, view.hasWindowFocus())
                            events += event
                            Log.i(TAG, event.toString())
                        }
                    onDispose { holder.accessibilityDelegate = null }
                }
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
            node.actionList.map { it.id },
            bounds
        ).joinToString(" | ")
        val children = (0 until node.childCount).map { tree(node.getChild(it), depth + 1) }
        return (listOf(line) + children).joinToString("\n")
    }

    private fun isInMessage(node: AccessibilityNodeInfo): Boolean =
        messageInScreen.contains(Rect().also(node::getBoundsInScreen))

    /** One round: focus onto [target], seen inside the message, then onto the content, seen outside. */
    private fun rounds(target: String): Int = (1..ROUNDS).count {
        moveFocus(target) && awaitInMessage(true) && moveFocus(CONTENT) && awaitInMessage(false)
    }

    private fun moveFocus(label: String): Boolean = find(automation.rootInActiveWindow, label)
        ?.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS) == true

    /** Compose does not search its nodes by text, so this walks the window's tree. */
    private fun find(node: AccessibilityNodeInfo?, label: String): AccessibilityNodeInfo? {
        if (node == null) return null
        val matches = node.text?.toString() == label || node.contentDescription?.toString() == label
        return if (matches) {
            node
        } else {
            (0 until node.childCount).firstNotNullOfOrNull { find(node.getChild(it), label) }
        }
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
