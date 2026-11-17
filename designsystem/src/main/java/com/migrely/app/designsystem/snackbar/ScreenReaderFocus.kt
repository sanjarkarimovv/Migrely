package com.migrely.app.designsystem.snackbar

import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import java.util.WeakHashMap

/**
 * Hears TalkBack's focus move on a ComposeView, with public APIs only (BRD D182 (2), T64 spike 3.1). Set as the
 * ComposeView's accessibility delegate, it sees each focus and clear event Compose sends up, and asks Compose's node
 * provider which node holds accessibility focus. Every other delegate method keeps its default, so it adds no node,
 * label or action and does not change the reading order. One probe serves every message host on its ComposeView.
 */
private class ScreenReaderFocusProbe : View.AccessibilityDelegate() {
    val listeners = mutableSetOf<(AccessibilityNodeInfo?) -> Unit>()

    override fun onRequestSendAccessibilityEvent(
        host: ViewGroup,
        child: View,
        event: AccessibilityEvent
    ): Boolean {
        val type = event.eventType
        if (listeners.isNotEmpty() &&
            (
                type == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED ||
                    type == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED
                )
        ) {
            val focused = child.accessibilityNodeProvider
                ?.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY)
            listeners.toList().forEach { it(focused) }
        }
        return super.onRequestSendAccessibilityEvent(host, child, event)
    }
}

private val probes = WeakHashMap<View, ScreenReaderFocusProbe>()

/**
 * The probe on [composeView], installed on first use and kept. Null when the view already has a delegate of its own,
 * which the probe would replace: such a screen pauses on keyboard focus only. Below API 29 a delegate cannot be read,
 * and Migrely sets none on a ComposeView.
 */
private fun probeOn(composeView: View): ScreenReaderFocusProbe? = probes.getOrPut(composeView) {
    val foreign = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
        composeView.accessibilityDelegate != null
    if (foreign) return null
    ScreenReaderFocusProbe().also { composeView.accessibilityDelegate = it }
}

/**
 * Tells [onChanged] whether TalkBack's focus is on the node whose screen bounds lie inside [messageInScreen]: the
 * message's text or its control.
 */
@Composable
internal fun ScreenReaderFocusEffect(messageInScreen: () -> Rect?, onChanged: (Boolean) -> Unit) {
    val composeView = LocalView.current.parent as? View ?: return
    val currentMessage by rememberUpdatedState(messageInScreen)
    val currentOnChanged by rememberUpdatedState(onChanged)
    DisposableEffect(composeView) {
        val probe = probeOn(composeView)
        val listener: (AccessibilityNodeInfo?) -> Unit = { node ->
            val message = currentMessage()
            val inside = node != null && message != null &&
                message.contains(Rect().also(node::getBoundsInScreen))
            currentOnChanged(inside)
        }
        probe?.listeners?.add(listener)
        onDispose { probe?.listeners?.remove(listener) }
    }
}

/** Whether TalkBack, or another touch-exploring screen reader, is on; it follows the setting while shown. */
@Composable
internal fun rememberScreenReaderOn(): State<Boolean> {
    val manager = LocalContext.current.getSystemService(AccessibilityManager::class.java)
    val on = remember(manager) { mutableStateOf(manager?.isTouchExplorationEnabled == true) }
    DisposableEffect(manager) {
        val listener = AccessibilityManager.TouchExplorationStateChangeListener { on.value = it }
        manager?.addTouchExplorationStateChangeListener(listener)
        onDispose { manager?.removeTouchExplorationStateChangeListener(listener) }
    }
    return on
}
