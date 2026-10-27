package com.migrely.app.designsystem.snackbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val DELETED = "Migraine deleted"
private const val BENEATH = "beneath"

/**
 * A tap aimed at a message that is still coming or has just gone must not reach the controls under it, such as a top
 * bar's actions (QA #52).
 */
@RunWith(AndroidJUnit4::class)
class MigrelySnackbarHostTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var message by mutableStateOf<String?>(null)
    private var tapsBeneath = 0

    @Test
    fun aTapAsAMessageArrivesDoesNotReachWhatIsBeneathIt() {
        message = DELETED
        show()
        val messageCentre = messageCentre()
        message = null
        composeTestRule.mainClock.advanceTimeBy(1_500)
        composeTestRule.mainClock.autoAdvance = false
        message = DELETED
        // A few frames into the slide in, where a finger aiming at the message lands.
        composeTestRule.mainClock.advanceTimeBy(64)

        tapBeneathAt(messageCentre)

        assertEquals(0, tapsBeneath)
    }

    @Test
    fun aTapJustAfterAMessageLeavesDoesNotReachWhatIsBeneathIt() {
        message = DELETED
        show()
        val messageCentre = messageCentre()
        composeTestRule.mainClock.autoAdvance = false
        message = null
        // The fade out is over and the message is gone.
        composeTestRule.mainClock.advanceTimeBy(300)

        tapBeneathAt(messageCentre)

        assertEquals(0, tapsBeneath)
    }

    @Test
    fun whatIsBeneathWorksAgainSoonAfterTheMessageLeaves() {
        message = DELETED
        show()
        val messageCentre = messageCentre()
        message = null
        composeTestRule.mainClock.advanceTimeBy(1_500)

        tapBeneathAt(messageCentre)

        assertEquals(1, tapsBeneath)
    }

    private fun show() {
        composeTestRule.setContent {
            MigrelyTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    // What lies under a message at the top of the screen, such as a top bar's actions.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .testTag(BENEATH)
                            .clickable { tapsBeneath++ }
                    )
                    MigrelySnackbarHost(message = message, reduceMotion = false) { text ->
                        MigrelySnackbar(
                            text = text,
                            tone = SnackbarTone.Confirmation,
                            action = SnackbarAction("Undo", "Undo the deletion") {},
                            dismissLabel = "Dismiss",
                            onDismiss = {}
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun messageCentre(): Offset {
        val bounds = composeTestRule.onNodeWithText(DELETED).fetchSemanticsNode().boundsInRoot
        return bounds.center
    }

    private fun tapBeneathAt(position: Offset) {
        composeTestRule.onNodeWithTag(BENEATH).performTouchInput { click(position) }
        composeTestRule.mainClock.advanceTimeByFrame()
    }
}
