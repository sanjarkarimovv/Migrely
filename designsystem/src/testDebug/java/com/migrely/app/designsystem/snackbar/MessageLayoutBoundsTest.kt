package com.migrely.app.designsystem.snackbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val TEXT = "Couldn't save. Your phone's storage is full."
private val SCREEN = 800.dp
private const val ONE_PIXEL_DP = 0.5f

/**
 * [MessageLayout] changes the order, not the geometry (BRD D178 (4), task 5.3): the message, its reported bottom and the
 * content lie where 52c12ce5's `Box { Column { topBar; content }; messages() }` put them, at 100% and 200%.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w400dp-h1600dp")
class MessageLayoutBoundsTest {
    @get:Rule
    val rule = createComposeRule()

    private var oldBottom = 0.dp
    private var newBottom = 0.dp

    /** The old screen on top, the new one below it, each [SCREEN] high. */
    private fun showBoth(fontScale: Float) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                MigrelyTheme {
                    Column {
                        Box(Modifier.fillMaxWidth().height(SCREEN)) {
                            Box(Modifier.fillMaxSize()) {
                                Column(Modifier.fillMaxSize()) {
                                    Bar("oldBar")
                                    Content("oldContent")
                                }
                                Message("oldMessage") { oldBottom = it }
                            }
                        }
                        Box(Modifier.fillMaxWidth().height(SCREEN)) {
                            MessageLayout(
                                topBar = { Bar("newBar") },
                                message = { Message("newMessage") { newBottom = it } },
                                content = { Content("newContent") },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    @Composable
    private fun Bar(tag: String) {
        Box(Modifier.fillMaxWidth().height(64.dp).testTag(tag))
    }

    @Composable
    private fun Content(tag: String) {
        Box(Modifier.fillMaxSize().testTag(tag))
    }

    @Composable
    private fun Message(tag: String, onBottom: (Dp) -> Unit) {
        MigrelySnackbarHost(
            message = TEXT,
            reduceMotion = true,
            topOffset = MESSAGE_OFFSET_BELOW_TOP_BAR,
            onMessageBottomChanged = onBottom
        ) {
            Box(Modifier.testTag(tag)) {
                MigrelySnackbar(it, SnackbarTone.Error, null, null, "Dismiss", {})
            }
        }
    }

    private fun bounds(tag: String): DpRect = rule.onNodeWithTag(tag).getUnclippedBoundsInRoot()

    private fun assertSamePlace(old: String, new: String) {
        val before = bounds(old)
        val after = bounds(new)
        assertEquals("left of $new", before.left.value, after.left.value, ONE_PIXEL_DP)
        assertEquals("top of $new", before.top.value, (after.top - SCREEN).value, ONE_PIXEL_DP)
        assertEquals("right of $new", before.right.value, after.right.value, ONE_PIXEL_DP)
        assertEquals(
            "bottom of $new",
            before.bottom.value,
            (after.bottom - SCREEN).value,
            ONE_PIXEL_DP
        )
    }

    private fun assertSameGeometry() {
        assertSamePlace("oldBar", "newBar")
        assertSamePlace("oldMessage", "newMessage")
        assertSamePlace("oldContent", "newContent")
        assertEquals(oldBottom.value, (newBottom - SCREEN).value, ONE_PIXEL_DP)
    }

    @Test
    fun `At 100 percent the message, its bottom and the content lie where they did`() {
        showBoth(fontScale = 1f)

        assertSameGeometry()
    }

    @Test
    fun `At 200 percent the message, its bottom and the content lie where they did`() {
        showBoth(fontScale = 2f)

        assertSameGeometry()
    }
}
