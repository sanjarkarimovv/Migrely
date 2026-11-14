package com.migrely.app.home.ui.view

import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.migrely.app.designsystem.snackbar.MESSAGE_OFFSET_BELOW_TOP_BAR
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.home.ui.mapper.HomeMessageBehaviourUiMapper
import com.migrely.app.home.ui.mapper.SkyAppearanceUiMapper
import com.migrely.app.home.ui.model.HomeMessageUiModel
import com.migrely.app.home.ui.model.HomeViewStateUiModel
import com.migrely.app.home.ui.model.SkyUiModel
import com.migrely.app.home.ui.model.TodayUiModel
import com.migrely.app.home.ui.notification.HomeMessagePresenter
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Home has no top bar, so the message's control is the first stop, even in portrait Today where it is drawn below the
 * Settings row (BRD D178 (4), EC-UX-36, ux Q2, O11).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class HomeMessageOrderTest {
    @get:Rule
    val rule = createComposeRule()

    private val presenter = HomeMessagePresenter()
    private lateinit var focusManager: FocusManager

    private fun tab() {
        rule.runOnIdle { focusManager.moveFocus(FocusDirection.Next) }
        rule.waitForIdle()
    }

    @Test
    fun `Tab goes to the message's Undo first, then Settings`() {
        rule.setContent {
            focusManager = LocalFocusManager.current
            MigrelyTheme {
                val navController = rememberNavController()
                HomeContents(
                    viewState = HomeViewStateUiModel.Today(TodayUiModel(SkyUiModel.Calm, 0, null)),
                    nowMillis = 0L,
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
                            topOffset = MESSAGE_OFFSET_BELOW_TOP_BAR
                        )
                    },
                    messageBottom = { 0.dp }
                )
            }
        }
        rule.waitForIdle()
        rule.runOnIdle { presenter.show(HomeMessageUiModel.AttackEnded("a")) }
        rule.waitForIdle()

        tab()
        rule.onNodeWithContentDescription("Undo the end").assertIsFocused()
        tab()
        rule.onNodeWithContentDescription("Settings").assertIsFocused()
    }
}
