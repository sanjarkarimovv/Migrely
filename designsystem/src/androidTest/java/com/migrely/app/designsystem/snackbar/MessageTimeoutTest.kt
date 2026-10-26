package com.migrely.app.designsystem.snackbar

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.AccessibilityManager
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val READING_MILLIS = 8_000L
private const val TIME_TO_TAKE_ACTION_MILLIS = 20_000L

/** Android's accessibility timeout: the time to take action, for messages with controls (QA #56). */
private class FakeAccessibilityManager : AccessibilityManager {
    override fun calculateRecommendedTimeoutMillis(
        originalTimeoutMillis: Long,
        containsIcons: Boolean,
        containsText: Boolean,
        containsControls: Boolean
    ): Long = if (containsControls) {
        maxOf(originalTimeoutMillis, TIME_TO_TAKE_ACTION_MILLIS)
    } else {
        originalTimeoutMillis
    }
}

/** A message stays for its base duration, or longer when the user's accessibility timeout asks (BRD EC-UX-16). */
@RunWith(AndroidJUnit4::class)
class MessageTimeoutTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun timeout(manager: AccessibilityManager?, baseDurationMillis: Long?): Long? {
        var timeout: Long? = -1L
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAccessibilityManager provides manager) {
                timeout = messageTimeoutMillis(baseDurationMillis)
            }
        }
        composeTestRule.waitForIdle()
        return timeout
    }

    @Test
    fun aLongerTimeToTakeActionStretchesAMessage() {
        assertEquals(
            TIME_TO_TAKE_ACTION_MILLIS,
            timeout(FakeAccessibilityManager(), READING_MILLIS)
        )
    }

    @Test
    fun withoutAnAccessibilityManagerAMessageKeepsItsBaseDuration() {
        assertEquals(READING_MILLIS, timeout(null, READING_MILLIS))
    }

    @Test
    fun aMessageWithoutABaseDurationStaysUntilClosed() {
        assertEquals(null, timeout(FakeAccessibilityManager(), null))
    }
}
