package com.migrely.app.home.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.home.ui.mapper.SkyAppearanceUiMapper
import com.migrely.app.home.ui.model.HomeViewStateUiModel
import com.migrely.app.home.ui.model.SkyUiModel
import com.migrely.app.home.ui.model.TodayUiModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Home has no text field, so it ignores the keyboard (BRD D180 (3), EC-UX-42, #145): a 250 dp keyboard inset, open or
 * left behind by Android 8 after a copy, leaves "Migraine started" where it was.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class HomeKeyboardTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `A keyboard inset leaves Home Today as it was`() {
        rule.runOnUiThread { rule.activity.drawEdgeToEdge() }
        rule.setContent {
            MigrelyTheme {
                HomeContents(
                    viewState = HomeViewStateUiModel.Today(TodayUiModel(SkyUiModel.Calm, 0, null)),
                    nowMillis = 0L,
                    reduceMotion = true,
                    skyAppearanceUiMapper = SkyAppearanceUiMapper(),
                    timeUiFormatter = previewFormatter,
                    actions = previewActions,
                    messages = {},
                    messageBottom = { 0.dp }
                )
            }
        }
        rule.waitForIdle()
        val before = rule.onNodeWithText("Migraine started").getUnclippedBoundsInRoot()

        rule.runOnUiThread { rule.activity.setKeyboard(KEYBOARD_DP) }
        rule.waitForIdle()

        assertEquals(before, rule.onNodeWithText("Migraine started").getUnclippedBoundsInRoot())
    }
}
