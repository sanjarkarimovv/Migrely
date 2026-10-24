package com.migrely.app.designsystem.snackbar

import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val STARTED = "Migraine started"

/** Where the message ends, so a screen can keep its own text clear of it (BRD D52). */
@RunWith(AndroidJUnit4::class)
class MigrelySnackbarHostTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var message by mutableStateOf<String?>(STARTED)
    private var bottom: Dp = (-1).dp

    private fun show(reduceMotion: Boolean) {
        composeTestRule.setContent {
            MigrelyTheme {
                MigrelySnackbarHost(
                    message = message,
                    reduceMotion = reduceMotion,
                    onMessageBottomChanged = { bottom = it }
                ) { text ->
                    Text(text, Modifier.height(40.dp))
                }
            }
        }
    }

    private fun assertBottomIsTheMessage() {
        composeTestRule.waitForIdle()
        val messageBottom =
            composeTestRule.onNodeWithText(STARTED).getUnclippedBoundsInRoot().bottom
        assertEquals(messageBottom.value, bottom.value, 0.5f)
    }

    @Test
    fun reportsTheMessageBottomAndZeroOnceItGoes() {
        show(reduceMotion = false)
        assertBottomIsTheMessage()

        message = null

        composeTestRule.waitForIdle()
        assertEquals(0.dp, bottom)
    }

    @Test
    fun withReduceMotionTheBottomChangesAtOnce() {
        show(reduceMotion = true)
        assertBottomIsTheMessage()
        composeTestRule.mainClock.autoAdvance = false

        message = null
        composeTestRule.mainClock.advanceTimeByFrame()
        composeTestRule.mainClock.advanceTimeByFrame()

        composeTestRule.runOnIdle { assertEquals(0.dp, bottom) }
    }
}
