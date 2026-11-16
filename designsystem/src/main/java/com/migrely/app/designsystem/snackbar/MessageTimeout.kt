package com.migrely.app.designsystem.snackbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalAccessibilityManager

/**
 * How long any message with a duration stays, on every screen (BRD EC-UX-16, D182 (1)): [baseDurationMillis] stretched
 * to the user's Android "Time to take action", or null when the message stays until the user closes it. Every message
 * has a control, Undo or Dismiss, so the timeout for taking action applies to all of them, never the shorter one for
 * reading. This is the only place that asks Android for it (MessageCallSitesTest).
 */
@Composable
fun messageTimeoutMillis(baseDurationMillis: Long?): Long? {
    val accessibilityManager = LocalAccessibilityManager.current
    return baseDurationMillis?.let { base ->
        accessibilityManager?.calculateRecommendedTimeoutMillis(
            originalTimeoutMillis = base,
            containsIcons = true,
            containsText = true,
            containsControls = true
        ) ?: base
    }
}
