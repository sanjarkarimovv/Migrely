package com.migrely.app.designsystem.snackbar

import android.view.accessibility.AccessibilityNodeInfo

/**
 * The first node in this tree whose text or description is [label]. Compose does not search its nodes by text, so this
 * walks the window's tree as UiAutomation sees it.
 */
internal fun AccessibilityNodeInfo?.findByLabel(label: String): AccessibilityNodeInfo? {
    if (this == null) return null
    val matches = text?.toString() == label || contentDescription?.toString() == label
    return if (matches) {
        this
    } else {
        (0 until childCount).firstNotNullOfOrNull { getChild(it).findByLabel(label) }
    }
}
