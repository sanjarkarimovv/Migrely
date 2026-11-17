package com.migrely.app.designsystem.snackbar

import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * The T64 spike's probe (task 3.1, design D3). It tells whether TalkBack's accessibility focus is on a message, using
 * public APIs only.
 * - Set as the accessibility delegate of the ComposeView that holds the AndroidComposeView, it sees each event that
 *   Compose sends up through `requestSendAccessibilityEvent`.
 * - On a focus or clear event it asks the AndroidComposeView's node provider which node holds accessibility focus
 *   (`findFocus(FOCUS_ACCESSIBILITY)`, which Compose implements), and [isInMessage] decides whether that node is the
 *   message's.
 *
 * It adds no semantics node, label or action and does not change the reading order: it is a View delegate, not a
 * Compose modifier, and every other delegate method keeps its default. It stays in the test source set until the
 * spike's result decides task 4.4.
 */
internal class AccessibilityFocusProbe(
    private val isInMessage: (AccessibilityNodeInfo) -> Boolean,
    private val onFocusEvent: (eventType: Int, inMessage: Boolean) -> Unit
) : View.AccessibilityDelegate() {
    override fun onRequestSendAccessibilityEvent(
        host: ViewGroup,
        child: View,
        event: AccessibilityEvent
    ): Boolean {
        val type = event.eventType
        if (type == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED ||
            type == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED
        ) {
            val focused = child.accessibilityNodeProvider
                ?.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY)
            onFocusEvent(type, focused != null && isInMessage(focused))
        }
        return super.onRequestSendAccessibilityEvent(host, child, event)
    }
}
