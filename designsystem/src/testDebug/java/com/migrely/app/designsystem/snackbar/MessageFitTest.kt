package com.migrely.app.designsystem.snackbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val MESSAGE = "message"
private val OFFSET = 100.dp

// The host's own 8 dp above and below the message.
private val HOST_PADDING = 8.dp

/**
 * The fit rule (BRD D178 (6), ux F5, rule 6): a message is always whole inside the safe area. It rises only as far as it
 * must, never above the safe top plus 8 dp, and where it fits it stays exactly where the offset puts it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w600dp-h300dp")
class MessageFitTest {
    @get:Rule
    val rule = createComposeRule()

    private fun showMessage(height: Dp) {
        rule.setContent {
            MigrelyTheme {
                Box(Modifier.fillMaxSize()) {
                    MigrelySnackbarHost(message = height, reduceMotion = true, topOffset = OFFSET) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(it)
                                .testTag(MESSAGE)
                        )
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    private fun screenHeight(): Dp = rule.onRoot().getUnclippedBoundsInRoot().bottom

    private fun messageBounds() = rule.onNodeWithTag(MESSAGE).getUnclippedBoundsInRoot()

    @Test
    fun `A message that fits sits where the offset puts it`() {
        showMessage(60.dp)

        assertEquals((HOST_PADDING + OFFSET).value, messageBounds().top.value, 0.5f)
    }

    @Test
    fun `A message too tall for its place rises just enough to end inside the screen`() {
        showMessage(250.dp)

        val bounds = messageBounds()
        assertEquals((screenHeight() - HOST_PADDING).value, bounds.bottom.value, 0.5f)
        assertEquals((screenHeight() - HOST_PADDING - 250.dp).value, bounds.top.value, 0.5f)
    }

    @Test
    fun `A message taller than the screen rises no higher than 8 dp below the safe top`() {
        showMessage(400.dp)

        assertEquals(HOST_PADDING.value, messageBounds().top.value, 0.5f)
    }
}
