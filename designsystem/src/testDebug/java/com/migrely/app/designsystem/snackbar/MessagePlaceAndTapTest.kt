package com.migrely.app.designsystem.snackbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val TEXT = "The purchase didn't go through. Try again."
private const val UNDO = "Undo the deletion"
private const val DISMISS = "Dismiss"
private const val BENEATH = "beneath"
private const val BAR_ACTION = "barAction"
private const val FRAMES_TO_FINISH_A_TAP = 3

/** Where a message sits, how long its place is guarded, and what a tap on it does (BRD D158). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h800dp")
class MessagePlaceAndTapTest {
    @get:Rule
    val rule = createComposeRule()

    private var message by mutableStateOf<String?>(TEXT)
    private var topOffset by mutableStateOf(0.dp)
    private var tapsBeneath = 0
    private var tapsOnBarAction = 0
    private var dismissals = 0
    private var undos = 0

    private fun showHost(
        guardMillis: Long? = null,
        hasTimeout: Boolean = true,
        undo: Boolean = false
    ) {
        rule.setContent {
            MigrelyTheme {
                Box(Modifier.fillMaxSize()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .testTag(BENEATH)
                            .clickable { tapsBeneath++ }
                    )
                    // A control in the row the offset keeps clear, such as Close.
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(topOffset.coerceAtLeast(1.dp))
                            .testTag(BAR_ACTION)
                            .clickable { tapsOnBarAction++ }
                    )
                    if (guardMillis == null) {
                        MigrelySnackbarHost(message, reduceMotion = false, topOffset = topOffset) {
                            Message(it, hasTimeout, undo)
                        }
                    } else {
                        MigrelySnackbarHost(
                            message,
                            reduceMotion = false,
                            topOffset = topOffset,
                            guardMillis = guardMillis
                        ) {
                            Message(it, hasTimeout, undo)
                        }
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    @Composable
    private fun Message(text: String, hasTimeout: Boolean, undo: Boolean) {
        MigrelySnackbar(
            text = text,
            tone = SnackbarTone.Error,
            action = if (undo) SnackbarAction("Undo", UNDO) { undos++ } else null,
            hasTimeout = hasTimeout,
            dismissLabel = DISMISS,
            onDismiss = {
                dismissals++
                message = null
            }
        )
    }

    private fun messageCentre(): Offset =
        rule.onNodeWithText(TEXT).fetchSemanticsNode().boundsInRoot.center

    /**
     * Whether a tap on the message's place reaches what lies under it [millis] after it left. These
     * tests compare the two guards. Under Robolectric each measured about twice its length, so the
     * exact times are checked on a device in MigrelySnackbarHostTest.
     */
    private fun tapReachesBeneath(millis: Long): Boolean {
        val centre = messageCentre()
        rule.mainClock.autoAdvance = false
        message = null
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeBy(millis)
        rule.onNodeWithTag(BENEATH).performTouchInput { click(centre) }
        repeat(FRAMES_TO_FINISH_A_TAP) { rule.mainClock.advanceTimeByFrame() }
        return tapsBeneath == 1
    }

    @Test
    fun `Soon after a message leaves, the default guard swallows a tap`() {
        showHost()

        assertEquals(false, tapReachesBeneath(250))
    }

    @Test
    fun `Soon after a message leaves, a guard of 400 ms swallows a tap`() {
        showHost(guardMillis = 400)

        assertEquals(false, tapReachesBeneath(250))
    }

    @Test
    fun `A tap that a guard of 400 ms lets through, the default guard still swallows`() {
        showHost()

        assertEquals(false, tapReachesBeneath(1_000))
    }

    @Test
    fun `A guard of 400 ms has ended where the default has not`() {
        showHost(guardMillis = 400)

        assertEquals(true, tapReachesBeneath(1_000))
    }

    @Test
    fun `The default guard ends too`() {
        showHost()

        assertEquals(true, tapReachesBeneath(3_000))
    }

    @Test
    fun `The offset moves the message down by exactly that much`() {
        showHost()
        val without = rule.onNodeWithText(TEXT).getUnclippedBoundsInRoot().top

        topOffset = MESSAGE_OFFSET_BELOW_TOP_BAR
        rule.waitForIdle()

        assertEquals(
            (without + MESSAGE_OFFSET_BELOW_TOP_BAR).value,
            rule.onNodeWithText(TEXT).getUnclippedBoundsInRoot().top.value,
            1f
        )
    }

    @Test
    fun `A control in the row the offset clears answers the first tap under a message`() {
        topOffset = MESSAGE_OFFSET_BELOW_TOP_BAR
        showHost()

        rule.onNodeWithTag(BAR_ACTION).performClick()

        assertEquals(1, tapsOnBarAction)
        assertEquals(0, tapsBeneath)
        assertEquals(0, dismissals)
    }

    @Test
    fun `A control in that row answers just after the message goes`() {
        topOffset = MESSAGE_OFFSET_BELOW_TOP_BAR
        showHost(guardMillis = 400)
        rule.mainClock.autoAdvance = false

        message = null
        rule.mainClock.advanceTimeBy(100)
        rule.onNodeWithTag(BAR_ACTION).performClick()
        rule.mainClock.autoAdvance = true
        rule.waitForIdle()

        assertEquals(1, tapsOnBarAction)
    }

    @Test
    fun `A tap on a message with a timeout and no action dismisses it`() {
        showHost()

        rule.onNodeWithText(TEXT).performTouchInput { click() }
        rule.waitForIdle()

        assertEquals(1, dismissals)
        rule.onNodeWithText(TEXT).assertDoesNotExist()
    }

    @Test
    fun `That tap does not reach what lies under the message`() {
        showHost()

        rule.onNodeWithText(TEXT).performTouchInput { click() }
        rule.waitForIdle()

        assertEquals(0, tapsBeneath)
    }

    @Test
    fun `Its close button dismisses once, not twice`() {
        showHost()

        rule.onNodeWithContentDescription(DISMISS).performClick()
        rule.waitForIdle()

        assertEquals(1, dismissals)
    }

    @Test
    fun `A swipe on it dismisses once, not twice`() {
        showHost()

        rule.onNodeWithText(TEXT).performTouchInput { swipeRight() }
        rule.waitForIdle()

        assertEquals(1, dismissals)
    }

    @Test
    fun `A tap on the body of a message with Undo does nothing, and Undo still works`() {
        showHost(undo = true)

        rule.onNodeWithText(TEXT).performTouchInput { click() }
        rule.waitForIdle()

        assertEquals(0, dismissals)
        assertEquals(0, undos)
        rule.onNodeWithText(TEXT).assertExists()

        rule.onNodeWithContentDescription(UNDO).performClick()

        assertEquals(1, undos)
    }

    @Test
    fun `A tap on the body of a message with no timeout does nothing`() {
        showHost(hasTimeout = false)

        rule.onNodeWithText(TEXT).performTouchInput { click() }
        rule.waitForIdle()

        assertEquals(0, dismissals)
        rule.onNodeWithText(TEXT).assertExists()
    }

    @Test
    fun `A message with no timeout still closes with its button`() {
        showHost(hasTimeout = false)

        rule.onNodeWithContentDescription(DISMISS).performClick()
        rule.waitForIdle()

        assertEquals(1, dismissals)
    }

    @Test
    fun `A message with no timeout still closes with a swipe`() {
        showHost(hasTimeout = false)

        rule.onNodeWithText(TEXT).performTouchInput { swipeRight() }
        rule.waitForIdle()

        assertEquals(1, dismissals)
    }

    @Test
    fun `The body tap adds nothing a screen reader can act on`() {
        showHost()

        rule.onNodeWithText(TEXT).assertHasNoClickAction()
        rule.onNodeWithContentDescription(DISMISS).assertHasClickAction()
    }
}
