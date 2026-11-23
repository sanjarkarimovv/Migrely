package com.migrely.app.attack.ui.view

import androidx.compose.ui.platform.AccessibilityManager
import androidx.compose.ui.test.MainTestClock
import com.migrely.app.designsystem.snackbar.MessageClock

/**
 * The message clocks moving with the Compose test clock, as a phone's clocks move with real time. Robolectric's own
 * clocks stand still while the Compose clock advances, so a test that times a message by the Compose clock needs this.
 */
internal class ComposeMessageClock(private val mainClock: MainTestClock) : MessageClock {
    private val wallStart = System.currentTimeMillis()
    private val composeStart = mainClock.currentTime

    override fun wallMillis(): Long = wallStart + mainClock.currentTime - composeStart

    override fun elapsedRealtimeMillis(): Long =
        COMPOSE_ELAPSED_START_MILLIS + mainClock.currentTime - composeStart

    private companion object {
        const val COMPOSE_ELAPSED_START_MILLIS = 1_000_000L
    }
}

/** Android's two timeouts set apart (BRD D182 (1)): 20 s to take action, 8 s to read. Each only stretches a base. */
internal object SplitTimeouts : AccessibilityManager {
    private const val INTERACTIVE_MILLIS = 20_000L
    private const val READING_MILLIS = 8_000L

    override fun calculateRecommendedTimeoutMillis(
        originalTimeoutMillis: Long,
        containsIcons: Boolean,
        containsText: Boolean,
        containsControls: Boolean
    ): Long =
        maxOf(originalTimeoutMillis, if (containsControls) INTERACTIVE_MILLIS else READING_MILLIS)
}
