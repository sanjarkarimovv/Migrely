package com.migrely.app.attack.ui.view

import android.graphics.Rect
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.migrely.app.attack.ui.model.AttackDetailsViewStateUiModel
import com.migrely.app.designsystem.snackbar.MESSAGE_OFFSET_BELOW_TOP_BAR
import com.migrely.app.designsystem.snackbar.MigrelySnackbar
import com.migrely.app.designsystem.snackbar.MigrelySnackbarHost
import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.test.talkback.TalkBackTree
import com.migrely.app.test.talkback.turnOnTalkBack
import kotlin.math.roundToInt
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val DISMISS = "Dismiss"
private const val TOUCH_TARGET_DP = 48

/**
 * On Attack details, exact bounds cover only the page's scroll (#255, design D4a): the top bar's Back and the message's
 * Dismiss, Material controls drawn at 40 dp, keep the 48 dp touch area Android widens them to. Exact bounds around
 * the whole screen would leave them at 40 dp.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class AttackDetailsExactBoundsScopeTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    /** The touch area Android exposes for the node described [description], in dp, as "width x height". */
    private fun touchArea(description: String): String {
        val id = rule.onNodeWithContentDescription(description).fetchSemanticsNode().id
        val bounds = Rect()
        rule.runOnUiThread { TalkBackTree(rule.activity).node(id)!!.getBoundsInScreen(bounds) }
        val density = rule.activity.resources.displayMetrics.density
        fun dp(px: Int) = (px / density).roundToInt()
        return "${dp(bounds.width())}x${dp(bounds.height())}"
    }

    @Test
    fun `Back and the message's Dismiss keep their widened 48 dp touch area`() {
        rule.runOnUiThread { rule.activity.turnOnTalkBack() }
        rule.setContent {
            MigrelyTheme {
                AttackDetailsContents(
                    viewState = AttackDetailsViewStateUiModel.Ready(previewDetails()),
                    timeUiFormatter = previewTimeFormatter,
                    labels = AttackLabels.Default,
                    localState = AttackDetailsLocalState(
                        timeError = null,
                        safetyCardVisible = false,
                        onDismissSafetyCard = {},
                        onTimeErrorShown = {}
                    ),
                    actions = previewActions,
                    messages = {
                        // Below the top bar, as the screen's own messages (AttackMessages).
                        MigrelySnackbarHost(
                            message = "Couldn't save.",
                            reduceMotion = true,
                            topOffset = MESSAGE_OFFSET_BELOW_TOP_BAR
                        ) {
                            MigrelySnackbar(it, SnackbarTone.Error, null, null, DISMISS, {})
                        }
                    }
                )
            }
        }
        val target = "${TOUCH_TARGET_DP}x$TOUCH_TARGET_DP"
        assertEquals(listOf(target, target), listOf(touchArea("Back"), touchArea(DISMISS)))
    }
}
