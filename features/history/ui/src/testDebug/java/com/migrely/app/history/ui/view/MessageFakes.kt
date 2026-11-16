package com.migrely.app.history.ui.view

import androidx.compose.ui.platform.AccessibilityManager
import com.migrely.app.designsystem.snackbar.MessageClock

/**
 * Android's two accessibility timeouts: "Time to take action" for messages with controls, and the reading one for the
 * rest. Like Android, each only stretches a base duration. The same fakes sit in each UI module's tests, which share
 * no test module.
 */
internal class FakeAccessibilityManager(
    private val interactiveMillis: Long,
    private val nonInteractiveMillis: Long = 0L
) : AccessibilityManager {
    override fun calculateRecommendedTimeoutMillis(
        originalTimeoutMillis: Long,
        containsIcons: Boolean,
        containsText: Boolean,
        containsControls: Boolean
    ): Long = maxOf(
        originalTimeoutMillis,
        if (containsControls) interactiveMillis else nonInteractiveMillis
    )
}

/** The message clocks, moved only by the test. Like a phone, both run together while the screen is locked. */
internal class FakeMessageClock(var wall: Long, var elapsed: Long = START_ELAPSED_MILLIS) :
    MessageClock {
    override fun wallMillis(): Long = wall

    override fun elapsedRealtimeMillis(): Long = elapsed

    fun advanceBy(millis: Long) {
        wall += millis
        elapsed += millis
    }

    private companion object {
        const val START_ELAPSED_MILLIS = 1_000_000L
    }
}
