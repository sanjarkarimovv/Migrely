package com.migrely.app.designsystem.snackbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val TEXT = "Couldn't save. Your phone's storage is full."
private const val FIRST_ROW = "firstRow"
private val BAR_HEIGHT = 64.dp

/** Content kept below a message: the push of BRD D52 and D165 (1). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h800dp")
class MessageClearanceLayoutTest {
    @get:Rule
    val rule = createComposeRule()

    private var message by mutableStateOf<String?>(TEXT)
    private var pushed by mutableStateOf(true)
    private var reportedBottom by mutableStateOf(0.dp)

    private fun show() {
        rule.setContent {
            MigrelyTheme {
                Box(Modifier.fillMaxSize()) {
                    Column(Modifier.fillMaxSize()) {
                        Spacer(Modifier.height(BAR_HEIGHT))
                        Column(
                            Modifier
                                .clearOfMessage { if (pushed) reportedBottom else 0.dp }
                                .verticalScroll(rememberScrollState())
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .testTag(FIRST_ROW)
                            )
                            Spacer(Modifier.height(2_000.dp))
                        }
                    }
                    MigrelySnackbarHost(
                        message = message,
                        reduceMotion = true,
                        topOffset = MESSAGE_OFFSET_BELOW_TOP_BAR,
                        onMessageBottomChanged = { reportedBottom = it }
                    ) {
                        MigrelySnackbar(
                            text = it,
                            tone = SnackbarTone.Error,
                            action = null,
                            baseDurationMillis = null,
                            dismissLabel = "Dismiss",
                            onDismiss = { message = null }
                        )
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    private fun rowTop(): Dp = rule.onNodeWithTag(FIRST_ROW).getUnclippedBoundsInRoot().top

    private fun messageBottom(): Dp = rule.onNodeWithText(TEXT).getUnclippedBoundsInRoot().bottom

    @Test
    fun `Given a message when pushed then the content starts below it`() {
        show()

        assertTrue("row ${rowTop()} under message ${messageBottom()}", rowTop() >= messageBottom())
        assertEquals(reportedBottom.value, rowTop().value, 0.5f)
    }

    @Test
    fun `Given a message when not pushed then the content lies under it as before`() {
        pushed = false
        show()

        assertEquals(BAR_HEIGHT.value, rowTop().value, 0.5f)
        assertTrue(rowTop() < messageBottom())
    }

    @Test
    fun `Given the message goes then the content returns to its place`() {
        show()

        message = null
        rule.waitForIdle()

        assertEquals(BAR_HEIGHT.value, rowTop().value, 0.5f)
    }
}
