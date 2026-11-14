package com.migrely.app.home.ui.view

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.home.ui.mapper.HomeMessageBehaviourUiMapper
import com.migrely.app.home.ui.mapper.SkyAppearanceUiMapper
import com.migrely.app.home.ui.model.HomeMessageUiModel
import com.migrely.app.home.ui.model.HomeViewStateUiModel
import com.migrely.app.home.ui.model.SkyUiModel
import com.migrely.app.home.ui.model.TodayUiModel
import com.migrely.app.home.ui.notification.HomeMessagePresenter
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val SETTINGS = "Settings"
private const val MIGRAINE_STARTED = "Migraine started"

// Wednesday, 30 September: a long date, which wraps at 200% in half a short window and on a 360 dp phone.
private val WEDNESDAY = ZonedDateTime.parse(
    "2026-09-30T12:00:00+05:00[Asia/Tashkent]"
).toInstant().toEpochMilli()
private val MESSAGE = SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion)

/**
 * Where a message sits on Home Today (BRD D178 (6), EC-UX-38, ux P1–P6, F5, F6; #88): below the measured Settings row,
 * over the start column in a short window, whole on screen, and still while the column scrolls.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HomePlacementTest {
    @get:Rule
    val rule = createComposeRule()

    private val presenter = HomeMessagePresenter()

    private fun showToday(
        message: HomeMessageUiModel,
        fontScale: Float = 1f,
        rtl: Boolean = false
    ) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale),
                LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
                MigrelyTheme {
                    val navController = rememberNavController()
                    var settingsRowHeight by remember { mutableStateOf(Dp.Unspecified) }
                    HomeContents(
                        viewState = HomeViewStateUiModel.Today(
                            TodayUiModel(SkyUiModel.Calm, 0, null)
                        ),
                        nowMillis = WEDNESDAY,
                        reduceMotion = true,
                        skyAppearanceUiMapper = SkyAppearanceUiMapper(),
                        timeUiFormatter = previewFormatter,
                        actions = previewActions,
                        messages = {
                            HomeMessages(
                                presenter = presenter,
                                navController = navController,
                                behaviourMapper = HomeMessageBehaviourUiMapper(),
                                timeUiFormatter = previewFormatter,
                                reduceMotion = true,
                                onUndoStart = {},
                                onUndoEnd = {},
                                onUndoDelete = {},
                                onMessageBottomChanged = {},
                                topOffset = messageTopOffset(HomeMode.Today, settingsRowHeight)
                            )
                        },
                        messageBottom = { 0.dp },
                        onSettingsRowHeight = { settingsRowHeight = it }
                    )
                }
            }
        }
        rule.waitForIdle()
        rule.runOnIdle { presenter.show(message) }
        rule.waitForIdle()
    }

    private fun message(): DpRect = rule.onNode(MESSAGE).getUnclippedBoundsInRoot()

    private fun settings(): DpRect =
        rule.onNodeWithContentDescription(SETTINGS).getUnclippedBoundsInRoot()

    private fun migraineStarted(): DpRect =
        rule.onNodeWithText(MIGRAINE_STARTED).getUnclippedBoundsInRoot()

    private fun screen(): DpRect = rule.onRoot().getUnclippedBoundsInRoot()

    private fun assertBelowSettings() {
        assertTrue(
            "message ${message()} over Settings ${settings()}",
            message().top >= settings().bottom
        )
    }

    private fun assertClearOfMigraineStarted(rtl: Boolean = false) {
        if (rtl) {
            assertTrue(
                "message ${message()} over ${migraineStarted()}",
                message().left >= migraineStarted().right
            )
        } else {
            assertTrue(
                "message ${message()} over ${migraineStarted()}",
                message().right <= migraineStarted().left
            )
        }
    }

    private fun assertWholeOnScreen() {
        assertTrue("message ${message()} past ${screen()}", message().bottom <= screen().bottom)
        val dismiss = rule.onNodeWithContentDescription("Dismiss").getUnclippedBoundsInRoot()
        assertTrue("Dismiss $dismiss past ${screen()}", dismiss.bottom <= screen().bottom)
    }

    @Test
    @Config(sdk = [35], qualifiers = "w891dp-h411dp")
    fun `At 411 dp, 100 percent, Undo lies over the start column below Settings`() {
        showToday(HomeMessageUiModel.AttackEnded("a"))

        assertBelowSettings()
        assertClearOfMigraineStarted()
    }

    @Test
    @Config(sdk = [35], qualifiers = "w891dp-h411dp")
    fun `At 411 dp, 200 percent, Undo lies over the start column below Settings`() {
        showToday(HomeMessageUiModel.AttackEnded("a"), fontScale = 2f)

        assertBelowSettings()
        assertClearOfMigraineStarted()
    }

    @Test
    @Config(sdk = [35], qualifiers = "w891dp-h411dp")
    fun `At 411 dp right to left, Undo lies over the start column on the right`() {
        showToday(HomeMessageUiModel.AttackEnded("a"), rtl = true)

        assertBelowSettings()
        assertClearOfMigraineStarted(rtl = true)
    }

    @Test
    @Config(sdk = [35], qualifiers = "w640dp-h274dp")
    fun `At 274 dp, 100 percent, Undo lies over the start column below Settings`() {
        showToday(HomeMessageUiModel.AttackEnded("a"))

        assertBelowSettings()
        assertClearOfMigraineStarted()
    }

    @Test
    @Config(sdk = [35], qualifiers = "w640dp-h274dp")
    fun `At 274 dp, 200 percent, Undo still lies below Settings`() {
        showToday(HomeMessageUiModel.AttackEnded("a"), fontScale = 2f)

        assertBelowSettings()
        assertClearOfMigraineStarted()
        assertTrue("message ${message()} past ${screen()}", message().bottom <= screen().bottom)
    }

    @Test
    @Config(sdk = [35], qualifiers = "w640dp-h274dp")
    fun `At 274 dp, 200 percent, storage full is whole and clear of Migraine started`() {
        showToday(HomeMessageUiModel.StorageFull, fontScale = 2f)

        assertWholeOnScreen()
        assertClearOfMigraineStarted()
    }

    @Test
    @Config(sdk = [35], qualifiers = "w640dp-h274dp")
    fun `At 274 dp RTL, 200 percent, storage full is whole and clear of Migraine started`() {
        showToday(HomeMessageUiModel.StorageFull, fontScale = 2f, rtl = true)

        assertWholeOnScreen()
        assertClearOfMigraineStarted(rtl = true)
    }

    @Test
    @Config(sdk = [35], qualifiers = "w360dp-h780dp")
    fun `On a 360 dp phone at 200 percent with a wrapped date, Undo lies below it and Settings`() {
        showToday(HomeMessageUiModel.AttackEnded("a"), fontScale = 2f)

        val date = rule.onNodeWithText("Wednesday, 30 September").getUnclippedBoundsInRoot()
        assertTrue("date $date not wrapped", date.bottom - date.top > 50.dp)
        assertTrue("message ${message()} over the date $date", message().top >= date.bottom)
        assertBelowSettings()
    }

    @Test
    @Config(sdk = [35], qualifiers = "w891dp-h411dp")
    fun `Scrolling the start column does not move the message`() {
        showToday(HomeMessageUiModel.AttackEnded("a"), fontScale = 2f)
        val before = message()

        rule.onAllNodes(hasScrollAction()).onFirst().performTouchInput { swipeUp() }
        rule.waitForIdle()

        assertEquals(before.top.value, message().top.value, 0.5f)
    }
}
