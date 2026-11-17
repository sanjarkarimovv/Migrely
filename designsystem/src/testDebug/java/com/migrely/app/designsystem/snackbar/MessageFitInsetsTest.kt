package com.migrely.app.designsystem.snackbar

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val MESSAGE = "message"
private val OFFSET = 100.dp
private val HEIGHT = 60.dp

// The host's own 8 dp above the message.
private val HOST_TOP = 8.dp

/**
 * The fit rule keeps a message inside the system bars and the cutout, not above the keyboard (review 9.2 (d)): an
 * open keyboard must not lift a message that fits, while a bottom bar that leaves it no room still does.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w600dp-h300dp")
class MessageFitInsetsTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun showWithBottomInset(type: Int) {
        rule.runOnUiThread { WindowCompat.setDecorFitsSystemWindows(rule.activity.window, false) }
        rule.setContent {
            MigrelyTheme {
                Box(Modifier.fillMaxSize()) {
                    MigrelySnackbarHost(message = HEIGHT, reduceMotion = true, topOffset = OFFSET) {
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
        rule.runOnUiThread {
            val view = rule.activity.window.decorView
            val px = (250 * view.resources.displayMetrics.density).toInt()
            val insets = WindowInsetsCompat.Builder()
                .setInsets(type, Insets.of(0, 0, 0, px))
                .setVisible(type, true)
                .build()
            ViewCompat.dispatchApplyWindowInsets(view, insets)
        }
        rule.waitForIdle()
    }

    private fun messageTop() = rule.onNodeWithTag(MESSAGE).getUnclippedBoundsInRoot().top.value

    @Test
    fun `An open keyboard does not lift a message that fits the screen`() {
        showWithBottomInset(WindowInsetsCompat.Type.ime())

        assertEquals((HOST_TOP + OFFSET).value, messageTop(), 0.5f)
    }

    @Test
    fun `A bottom system bar that leaves no room still lifts it`() {
        showWithBottomInset(WindowInsetsCompat.Type.navigationBars())

        assertEquals(HOST_TOP.value, messageTop(), 0.5f)
    }
}
