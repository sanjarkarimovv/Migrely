package com.migrely.app.designsystem.snackbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalAccessibilityManager

/**
 * How long a message without "Undo" stays (BRD EC-UX-16): [baseDurationMillis] stretched to the user's Android
 * accessibility timeout ("Time to take action"), or null when the message stays until the user closes it. A message's
 * close button is a control, so the timeout for taking action stretches it as well as the one for reading.
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
