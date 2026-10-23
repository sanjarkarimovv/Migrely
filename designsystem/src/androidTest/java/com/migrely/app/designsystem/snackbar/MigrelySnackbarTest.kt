package com.migrely.app.designsystem.snackbar

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.migrely.app.designsystem.theme.MigrelyTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val STARTED = "Migraine started"
private const val STORAGE_FULL = "Couldn't save. Your phone's storage is full."
private const val MOVE_REJECTED = "Your previous migraine ended at 3:56 PM."

@RunWith(AndroidJUnit4::class)
class MigrelySnackbarTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var actionClicks = 0
    private var dismissals = 0

    @Test
    fun confirmationWithUndoInvokesTheActionThroughItsSpokenLabel() {
        show(STARTED, SnackbarTone.Confirmation, undo = true, timeLeft = 0.6f)

        composeTestRule.onNodeWithText(STARTED).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Undo the start").performClick()

        assertEquals(1, actionClicks)
        assertEquals(0, dismissals)
        saveScreenshot("confirmation_undo")
    }

    @Test
    fun informationIsAnnouncedPolitelyAndCanBeClosed() {
        show(MOVE_REJECTED, SnackbarTone.Information)

        composeTestRule.onNode(liveRegion(LiveRegionMode.Polite)).assertExists()
        composeTestRule.onNodeWithContentDescription("Dismiss").performClick()

        assertEquals(1, dismissals)
        saveScreenshot("information")
    }

    @Test
    fun errorIsAnnouncedAtOnce() {
        show(STORAGE_FULL, SnackbarTone.Error)

        composeTestRule.onNode(liveRegion(LiveRegionMode.Assertive)).assertExists()
        saveScreenshot("error")
    }

    @Test
    fun swipingSidewaysDismissesWithoutTheAction() {
        show(STARTED, SnackbarTone.Confirmation, undo = true)

        composeTestRule.onNodeWithText(STARTED).performTouchInput { swipeRight() }
        composeTestRule.waitForIdle()

        assertEquals(1, dismissals)
        assertEquals(0, actionClicks)
    }

    @Test
    fun actionStaysBesideShortTextAtNormalSize() {
        show(STARTED, SnackbarTone.Confirmation, undo = true)

        val text = composeTestRule.onNodeWithText(STARTED).getUnclippedBoundsInRoot()
        val action = composeTestRule.onNodeWithContentDescription(
            "Undo the start"
        ).getUnclippedBoundsInRoot()

        assertTrue("action should be on the text's row", action.top < text.bottom)
    }

    @Test
    fun actionMovesBelowTheTextAtDoubleFontSize() {
        show(STORAGE_FULL, SnackbarTone.Error, fontScale = 2f)

        val text = composeTestRule.onNodeWithText(STORAGE_FULL).getUnclippedBoundsInRoot()
        val close = composeTestRule.onNodeWithContentDescription(
            "Dismiss"
        ).getUnclippedBoundsInRoot()

        composeTestRule.onNodeWithText(STORAGE_FULL).assertIsDisplayed()
        assertTrue("close button should sit below the text", close.top >= text.bottom - 1.dp)
        saveScreenshot("error_font_200")
    }

    @Test
    fun confirmationAtDoubleFontSize() {
        show(STARTED, SnackbarTone.Confirmation, undo = true, timeLeft = 0.3f, fontScale = 2f)

        composeTestRule.onNodeWithContentDescription("Undo the start").assertIsDisplayed()
        saveScreenshot("confirmation_font_200")
    }

    private fun show(
        text: String,
        tone: SnackbarTone,
        undo: Boolean = false,
        timeLeft: Float? = null,
        fontScale: Float = 1f
    ) {
        composeTestRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                MigrelyTheme {
                    Backdrop {
                        MigrelySnackbar(
                            text = text,
                            tone = tone,
                            action = if (undo) {
                                SnackbarAction("Undo", "Undo the start") {
                                    actionClicks++
                                }
                            } else {
                                null
                            },
                            dismissLabel = "Dismiss",
                            onDismiss = { dismissals++ },
                            timeLeft = timeLeft?.let { fraction -> { fraction } }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun Backdrop(content: @Composable () -> Unit) {
        Box(
            modifier = Modifier
                .width(360.dp)
                .background(Color(0xFF0F1115))
                .padding(16.dp)
        ) { content() }
    }

    private fun liveRegion(mode: LiveRegionMode) =
        SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, mode)

    /** Saves the rendered snackbar for visual review; pull with `adb pull` from the app's external files directory. */
    private fun saveScreenshot(name: String) {
        val bitmap = composeTestRule.onRoot().captureToImage().asAndroidBitmap()
        val directory = File(
            InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null),
            "snackbar"
        ).apply { mkdirs() }
        File(directory, "$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
