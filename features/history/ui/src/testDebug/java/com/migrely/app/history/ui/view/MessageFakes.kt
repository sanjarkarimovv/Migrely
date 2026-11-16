package com.migrely.app.history.ui.view

import androidx.compose.ui.platform.AccessibilityManager
import androidx.compose.ui.test.MainTestClock
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
