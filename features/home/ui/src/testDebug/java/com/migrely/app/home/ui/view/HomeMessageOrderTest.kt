package com.migrely.app.home.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.home.ui.mapper.HomeMessageBehaviourUiMapper
import com.migrely.app.home.ui.mapper.SkyAppearanceUiMapper
import com.migrely.app.home.ui.model.AttackInProgressUiModel
import com.migrely.app.home.ui.model.HomeMessageUiModel
import com.migrely.app.home.ui.model.HomeViewStateUiModel
import com.migrely.app.home.ui.model.IntensityLevelUiModel
import com.migrely.app.home.ui.model.SkyUiModel
import com.migrely.app.home.ui.model.TodayUiModel
import com.migrely.app.home.ui.notification.HomeMessagePresenter
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val UNDO = "Undo the end"
private const val SETTINGS = "Settings"
private const val PERIOD = "Period today"
private const val TRIAL = "trial banner"
private val TODAY = HomeViewStateUiModel.Today(TodayUiModel(SkyUiModel.Calm, 0, null))
private val NO_PERIOD =
    HomeViewStateUiModel.Today(TodayUiModel(SkyUiModel.Calm, 0, null, periodOffered = false))
private val ATTACK = HomeViewStateUiModel.AttackInProgress(
    AttackInProgressUiModel("a", 0L, "Asia/Tashkent", IntensityLevelUiModel.Moderate, false)
)

/**
 * Home's keyboard order (BRD D178 (4), ux F10, revised Q2, O11, O14): on Today, Settings, then the message's control,
 * then the content, because the device starts at the topmost control; in attack mode the message comes first. TalkBack
 * stays message-first on both.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class HomeMessageOrderTest {
    @get:Rule
    val rule = createComposeRule()

    private val presenter = HomeMessagePresenter()
    private lateinit var focusManager: FocusManager

    private fun showHome(
        today: HomeViewStateUiModel = TODAY,
        message: HomeMessageUiModel = HomeMessageUiModel.AttackEnded("a"),
        trialBanner: @Composable () -> Unit = {},
        medicationPrompt: @Composable () -> Unit = {},
        visitCard: @Composable () -> Unit = {}
    ) {
        rule.setContent {
            focusManager = LocalFocusManager.current
            MigrelyTheme {
                HomeWithMessages(
                    navController = rememberNavController(),
                    viewState = today,
                    nowMillis = 0L,
                    reduceMotion = true,
                    skyAppearanceUiMapper = SkyAppearanceUiMapper(),
                    timeUiFormatter = previewFormatter,
                    actions = previewActions,
                    messageSetup = HomeMessageSetup(
                        presenter,
                        HomeMessageBehaviourUiMapper(),
                        onUndoStart = {},
                        onUndoEnd = {},
                        onUndoDelete = {}
                    ),
                    slots = HomeSlots({}, medicationPrompt, trialBanner, visitCard, {}, {})
                )
            }
        }
        rule.waitForIdle()
        rule.runOnIdle { presenter.show(message) }
        rule.waitForIdle()
    }

    private fun move(direction: FocusDirection) {
        rule.runOnIdle { focusManager.moveFocus(direction) }
        rule.waitForIdle()
    }

    @Test
    fun `On Today, from nothing focused, Tab goes to Settings, then to the message's control`() {
        showHome()

        move(FocusDirection.Next)
        rule.onNodeWithContentDescription(SETTINGS).assertIsFocused()
        move(FocusDirection.Next)
        rule.onNodeWithContentDescription(UNDO).assertIsFocused()
    }

    @Test
    fun `On Today, Tab after the message's control goes to the first control after Settings`() {
        showHome()
        move(FocusDirection.Next)
        move(FocusDirection.Next)

        move(FocusDirection.Next)

        rule.onNodeWithContentDescription(PERIOD).assertIsFocused()
    }

    @Test
    fun `On Today, Shift+Tab from the control after Settings goes to the message, then Settings`() {
        showHome()
        rule.onNodeWithContentDescription(PERIOD).requestFocus()

        move(FocusDirection.Previous)
        rule.onNodeWithContentDescription(UNDO).assertIsFocused()
        move(FocusDirection.Previous)
        rule.onNodeWithContentDescription(SETTINGS).assertIsFocused()
    }

    @Test
    fun `With the trial banner, Tab, Tab still reaches the message, then the banner`() {
        showHome(trialBanner = { TextButton(onClick = {}) { Text(TRIAL) } })

        move(FocusDirection.Next)
        move(FocusDirection.Next)
        rule.onNodeWithContentDescription(UNDO).assertIsFocused()
        move(FocusDirection.Next)
        rule.onNodeWithText(TRIAL).assertIsFocused()
    }

    @Test
    fun `With the trial banner, Shift+Tab from the banner goes to the message, then to Settings`() {
        showHome(trialBanner = { TextButton(onClick = {}) { Text(TRIAL) } })
        rule.onNodeWithText(TRIAL).requestFocus()

        move(FocusDirection.Previous)
        rule.onNodeWithContentDescription(UNDO).assertIsFocused()
        move(FocusDirection.Previous)
        rule.onNodeWithContentDescription(SETTINGS).assertIsFocused()
    }

    @Test
    @Config(sdk = [35], qualifiers = "w891dp-h411dp")
    fun `In a short window, Tab goes Settings, the message, then the start column`() {
        showHome()

        move(FocusDirection.Next)
        rule.onNodeWithContentDescription(SETTINGS).assertIsFocused()
        move(FocusDirection.Next)
        rule.onNodeWithContentDescription(UNDO).assertIsFocused()
        move(FocusDirection.Next)
        rule.onNodeWithContentDescription(PERIOD).assertIsFocused()
    }

    @Test
    fun `On Today, Shift+Tab from Settings wraps to the last control, not back to the message`() {
        showHome()
        rule.onNodeWithContentDescription(SETTINGS).requestFocus()

        move(FocusDirection.Previous)

        rule.onNodeWithText("Log medication").assertIsFocused()
    }

    // Review 9.3 F1: the slots' real shape, a row with the text taking the room and a close button at its end, then a
    // button below. Entering such a slot by its geometry lands on the lower button; Tab order lands on the close button.
    @Composable
    private fun CardShaped(name: String) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
        ) {
            Row {
                Text("$name text", Modifier.weight(1f).padding(top = 8.dp))
                IconButton(onClick = {}) {
                    Icon(MigrelyIcons.Close, contentDescription = "Close $name")
                }
            }
            TextButton(onClick = {
            }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Open $name") }
        }
    }

    private fun assertTabFromMessageReachesCloseThenOpen(name: String) {
        move(FocusDirection.Next)
        move(FocusDirection.Next)
        rule.onNodeWithContentDescription(UNDO).assertIsFocused()
        move(FocusDirection.Next)
        rule.onNodeWithContentDescription("Close $name").assertIsFocused()
        move(FocusDirection.Next)
        rule.onNodeWithText("Open $name").assertIsFocused()
    }

    @Test
    fun `With the real trial banner, Tab after the message reaches its close button, then Manage`() {
        showHome(trialBanner = { CardShaped("banner") })

        assertTabFromMessageReachesCloseThenOpen("banner")
    }

    @Test
    fun `With the medication prompt first, Tab after the message reaches its close button`() {
        showHome(today = NO_PERIOD, medicationPrompt = { CardShaped("prompt") })

        assertTabFromMessageReachesCloseThenOpen("prompt")
    }

    @Test
    fun `With the visit card first, Tab after the message reaches its close button`() {
        showHome(today = NO_PERIOD, visitCard = { CardShaped("visit") })

        assertTabFromMessageReachesCloseThenOpen("visit")
    }

    // Review 9.3 F2: a Tab past the last control goes back to Settings, not on to the message placed last for Tab.
    @Test
    fun `On Today, Tab past Log medication wraps to Settings, then the message`() {
        showHome()
        rule.onNodeWithText("Log medication").requestFocus()

        move(FocusDirection.Next)
        rule.onNodeWithContentDescription(SETTINGS).assertIsFocused()
        move(FocusDirection.Next)
        rule.onNodeWithContentDescription(UNDO).assertIsFocused()
    }

    @Test
    fun `With no message, Tab goes from Settings to the first control after it`() {
        showHome(message = HomeMessageUiModel.Saved)

        move(FocusDirection.Next)
        move(FocusDirection.Next)

        rule.onNodeWithContentDescription(PERIOD).assertIsFocused()
    }

    @Test
    fun `In attack mode, from nothing focused, the message's control is the first stop`() {
        showHome(today = ATTACK, message = HomeMessageUiModel.AttackStarted("a"))

        move(FocusDirection.Next)

        rule.onNodeWithContentDescription("Undo the start").assertIsFocused()
    }

    @Test
    fun `TalkBack still reads the message before the content on Today`() {
        showHome()

        val message = slotIndex(hasContentDescription(UNDO))
        val settings = slotIndex(hasContentDescription(SETTINGS))

        assertEquals(listOf(1f, 2f), listOf(message, settings))
    }

    /** The traversal index of the nearest traversal group around the node matching [matcher]. */
    private fun slotIndex(matcher: SemanticsMatcher): Float? =
        generateSequence(rule.onNode(matcher, useUnmergedTree = true).fetchSemanticsNode()) {
            it.parent
        }
            .firstOrNull { SemanticsProperties.TraversalIndex in it.config }
            ?.config
            ?.get(SemanticsProperties.TraversalIndex)
}
