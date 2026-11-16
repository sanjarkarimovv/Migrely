package com.migrely.app.home.ui.view

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.home.ui.mapper.SkyAppearanceUiMapper
import com.migrely.app.home.ui.model.SkyUiModel
import com.migrely.app.home.ui.model.TodayUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ADD_PAST = "Add a past migraine"
private const val LOG = "Log medication"

/**
 * The quiet links keep their Tab order, "Add a past migraine" then "Log medication", side by side
 * and stacked (BRD D181 (5)). Legacy graphics, because Robolectric's native mode gives the window
 * no focus; legacy text measures no width, so only a button's 58 dp minimum counts, and the stacked
 * case needs a window under 148 dp. Each test checks its layout first. [QuietLinksTest] measures
 * the real words.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
class QuietLinksOrderTest {
    @get:Rule
    val rule = createComposeRule()

    private lateinit var focusManager: FocusManager

    private fun show(fontScale: Float) = rule.setContent {
        focusManager = LocalFocusManager.current
        val density = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
            MigrelyTheme {
                TodayContents(
                    today = TodayUiModel(SkyUiModel.Calm, 0, null, periodOffered = false),
                    dayTitle = "Today",
                    nowMillis = 0L,
                    skyAppearanceUiMapper = SkyAppearanceUiMapper(),
                    timeUiFormatter = previewFormatter,
                    onStartAttack = {},
                    onAttackDetails = {},
                    onAddPastAttack = {}
                )
            }
        }
    }

    private fun move(direction: FocusDirection) {
        rule.runOnIdle { focusManager.moveFocus(direction) }
        rule.waitForIdle()
    }

    private fun left(label: String) = rule.onNodeWithText(label).getUnclippedBoundsInRoot().left

    /** From nothing focused, Shift+Tab reaches the last control, "Log medication", first. */
    private fun assertTabOrder() {
        move(FocusDirection.Previous)
        rule.onNodeWithText(LOG).assertIsFocused()
        move(FocusDirection.Previous)
        rule.onNodeWithText(ADD_PAST).assertIsFocused()
        move(FocusDirection.Next)
        rule.onNodeWithText(LOG).assertIsFocused()
    }

    @Test
    fun `Side by side, Tab goes Add a past migraine, then Log medication`() {
        show(fontScale = 1f)
        assertNotEquals(left(ADD_PAST).value, left(LOG).value, 0.5f)

        assertTabOrder()
    }

    @Test
    @Config(qualifiers = "w140dp-h800dp")
    fun `Stacked, Tab goes Add a past migraine, then Log medication`() {
        show(fontScale = 2f)
        assertEquals(left(ADD_PAST).value, left(LOG).value, 0.5f)

        assertTabOrder()
    }
}
