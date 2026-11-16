package com.migrely.app.designsystem.snackbar

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import androidx.compose.ui.test.requestFocus
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val MESSAGE = "Migraine started"
private const val UNDO = "Undo the start"
private const val CONTENT = "Content"
private const val UNDO_MILLIS = 5_000L
private const val LINE_DELTA = 0.01f

/** The message clocks, moved by the test together with the Compose clock. */
private class TestMessageClock : MessageClock {
    var wall = 1_793_161_943_447L
    var elapsed = 50_000_000L

    override fun wallMillis(): Long = wall

    override fun elapsedRealtimeMillis(): Long = elapsed
}

/** The window's focus, which the notification shade, another app or a system dialog takes. */
private class TestWindowInfo : WindowInfo {
    override var isWindowFocused by mutableStateOf(true)
}

/**
 * [rememberMessageCountdown] in a host, on the Compose clock (BRD D182 (2), EC-UX-53, EC-UX-55): keyboard focus holds
 * the message, the window losing focus resumes it at once, the line goes on from where it stopped, and the countdown
 * adds nothing to the semantics tree.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w400dp-h800dp")
class MessageCountdownHostTest {
    @get:Rule
    val rule = createComposeRule()

    private val clock = TestMessageClock()
    private val window = TestWindowInfo()
    private var shown by mutableStateOf<String?>(null)
    private var timeLeft: () -> Float = { -1f }
    private var withCountdown by mutableStateOf(true)
    private var composedWithCountdown = false

    private fun show(line: Boolean = false) {
        rule.mainClock.autoAdvance = false
        shown = MESSAGE
        rule.setContent {
            CompositionLocalProvider(
                LocalMessageClock provides clock,
                LocalWindowInfo provides window
            ) {
                MigrelyTheme { Screen(line) }
            }
        }
        rule.mainClock.advanceTimeByFrame()
        rule.waitForIdle()
    }

    @Composable
    private fun Screen(line: Boolean) {
        composedWithCountdown = withCountdown
        val countdown = rememberMessageCountdown(
            message = shown,
            timeoutMillis = UNDO_MILLIS,
            undoActionWallMillis = clock.wall,
            onExpired = { shown = null }
        )
        Column {
            Button(onClick = {}) { Text(CONTENT) }
            MigrelySnackbarHost(
                message = shown?.takeUnless { countdown.isOver },
                reduceMotion = true,
                countdown = countdown.takeIf { withCountdown }
            ) { text ->
                MigrelySnackbar(
                    text = text,
                    tone = SnackbarTone.Confirmation,
                    action = SnackbarAction("Undo", UNDO) {},
                    baseDurationMillis = UNDO_MILLIS,
                    dismissLabel = "Dismiss",
                    onDismiss = {},
                    timeLeft = if (line) countdown.rememberTimeLeft().also(::keepLine) else null
                )
            }
        }
    }

    private fun keepLine(line: () -> Float) {
        timeLeft = line
    }

    /** Time passes on the screen: the phone's clocks and the Compose clock together. */
    private fun passTime(millis: Long) {
        clock.wall += millis
        clock.elapsed += millis
        rule.mainClock.advanceTimeBy(millis)
        rule.waitForIdle()
    }

    private fun focusUndo() {
        rule.onNodeWithContentDescription(UNDO).requestFocus()
        rule.waitForIdle()
    }

    private fun focusContent() {
        rule.onNodeWithText(CONTENT).requestFocus()
        rule.waitForIdle()
    }

    private fun assertShown(shownNow: Boolean) {
        if (shownNow) {
            rule.onNodeWithText(MESSAGE).assertExists()
        } else {
            rule.onNodeWithText(MESSAGE).assertDoesNotExist()
        }
    }

    @Test
    fun `Keyboard focus on Undo holds the message, and it leaves with its time left`() {
        show()
        passTime(1_000)
        focusUndo()
        passTime(30_000)
        assertShown(true)

        focusContent()
        passTime(4_200)
        assertShown(true)
        passTime(200)

        assertShown(false)
    }

    @Test
    fun `The window losing focus resumes at once, with the floor and no grace`() {
        show()
        passTime(4_500)
        focusUndo()
        passTime(10_000)

        window.isWindowFocused = false
        rule.waitForIdle()
        passTime(2_900)
        assertShown(true)
        passTime(200)

        assertShown(false)
    }

    @Test
    fun `After a 0_5 s resume the line goes on from where it stopped, with no jump`() {
        show(line = true)
        passTime(4_500)
        assertEquals(0.1f, timeLeft(), LINE_DELTA)
        focusUndo()
        passTime(1_000)
        assertEquals(0.1f, timeLeft(), LINE_DELTA)

        focusContent()
        passTime(300)
        assertEquals(0.1f, timeLeft(), LINE_DELTA)
        passTime(1_500)

        assertEquals(0.05f, timeLeft(), LINE_DELTA)
    }

    @Test
    fun `The countdown adds nothing to the semantics tree`() {
        withCountdown = false
        show()
        val without = rule.onRoot(useUnmergedTree = true).printToString()

        rule.runOnUiThread { withCountdown = true }
        rule.waitForIdle()
        rule.mainClock.advanceTimeByFrame()
        rule.waitForIdle()

        assertTrue("the countdown was never given to the host", composedWithCountdown)
        assertEquals(without, rule.onRoot(useUnmergedTree = true).printToString())
    }
}
