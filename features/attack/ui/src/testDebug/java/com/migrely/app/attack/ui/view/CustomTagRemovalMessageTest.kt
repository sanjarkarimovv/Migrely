package com.migrely.app.attack.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.migrely.app.attack.presentation.model.AttackDetailsPresentationNotification
import com.migrely.app.attack.presentation.model.CustomTagPresentationModel
import com.migrely.app.attack.presentation.model.TagKindPresentationModel
import com.migrely.app.attack.ui.mapper.AttackIntakeUiMapper
import com.migrely.app.attack.ui.mapper.AttackNotificationUiMapper
import com.migrely.app.attack.ui.mapper.AttackRangeUiMapper
import com.migrely.app.attack.ui.mapper.CustomTagUiMapper
import com.migrely.app.attack.ui.mapper.TagKindUiMapper
import com.migrely.app.attack.ui.mapper.TimeFieldUiMapper
import com.migrely.app.attack.ui.notification.AttackDetailsEventPresenter
import com.migrely.app.designsystem.snackbar.LocalMessageClock
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val CUSTOM_CHIP = "Jaw pain"
private const val REMOVE = "Remove from my list"
private const val REMOVED = "Removed from your list"
private const val DETAILS_ROUTE = "details"
private const val OTHER_ROUTE = "other"
private const val SETTLE_MILLIS = 1_000L

/** The preview's own symptom, as the screen hands it to the ViewModel. */
private val JAW_PAIN = CustomTagPresentationModel(
    "j",
    TagKindPresentationModel.Symptom,
    CUSTOM_CHIP,
    archived = false
)

/**
 * Every way to remove a custom tag says "Removed from your list", once and politely (BRD D199, EC-ATK-25, #179): the
 * menu item and TalkBack's custom action. The ViewModel is a mock that answers a removal with the notification the
 * real one sends (`AttackDetailsViewModelTest`), through the real notification mapper, so this proves the screen's
 * part: the chip hands over the tag, and the message host shows it as the screen's other messages.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h700dp")
class CustomTagRemovalMessageTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val events = AttackDetailsEventPresenter()
    private val dependencies = attackDetailsTestDependencies(events)

    /** The message clocks move with the Compose test clock, as an Undo message times itself by them (D182). */
    private val messageClock by lazy { ComposeMessageClock(rule.mainClock) }
    private val notifications = AttackNotificationUiMapper(
        events,
        TimeFieldUiMapper(),
        AttackRangeUiMapper(),
        AttackIntakeUiMapper(),
        CustomTagUiMapper(TagKindUiMapper())
    )
    private val removed = mutableListOf<CustomTagPresentationModel>()
    private lateinit var navController: NavHostController

    @Before
    fun holdTheClock() {
        rule.mainClock.autoAdvance = false
    }

    /**
     * Every scroll and touch runs here, with the clock running: a scroll waits for its own frames and never ends under
     * a held clock (it once ran a test JVM out of memory for minutes), and Robolectric can drop a tap there
     * (`SaveMessageTest`). The clock is held again right after, as it was, so an Undo message's countdown is not run to
     * its end by the waits. JUnit's timeout rule cannot guard this: it runs the test off Robolectric's main thread.
     */
    private fun <T> withClockRunning(input: () -> T): T {
        val before = rule.mainClock.autoAdvance
        rule.mainClock.autoAdvance = true
        try {
            return input()
        } finally {
            rule.mainClock.autoAdvance = before
        }
    }

    @Before
    fun answerRemovals() {
        doAnswer { call ->
            val tag = call.getArgument<CustomTagPresentationModel>(1)
            removed += tag
            notifications.toUi(
                AttackDetailsPresentationNotification.CustomTagRemoved(call.getArgument(0), tag)
            ).present()
        }.whenever(dependencies.attackDetailsViewModel).onArchiveCustomTagAction(any(), any())
    }

    private fun show() {
        rule.setContent {
            MigrelyTheme {
                CompositionLocalProvider(LocalMessageClock provides messageClock) {
                    navController = rememberNavController()
                    NavHost(navController, startDestination = OTHER_ROUTE) {
                        composable(OTHER_ROUTE) { Text("Home") }
                        composable(DETAILS_ROUTE) {
                            dependencies.AttackDetails(
                                attackId = "a",
                                announceMerge = false,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
        openDetails()
    }

    private fun openDetails() {
        rule.runOnUiThread { navController.navigate(DETAILS_ROUTE) }
        settle()
    }

    private fun settle() {
        rule.mainClock.advanceTimeBy(SETTLE_MILLIS)
        rule.waitForIdle()
    }

    private fun chip() =
        withClockRunning { rule.onNode(hasContentDescription(CUSTOM_CHIP)).performScrollTo() }

    private fun removeThroughTheMenu() {
        withClockRunning {
            chip().performTouchInput { longClick() }
            rule.waitForIdle()
            rule.onNode(hasText(REMOVE).and(hasAnyAncestor(isPopup())), useUnmergedTree = true)
                .performClick()
        }
        settle()
    }

    private fun removeWithTheCustomAction() {
        val action = chip().fetchSemanticsNode()
            .config[SemanticsActions.CustomActions]
            .single { it.label == REMOVE }
        rule.runOnUiThread { action.action() }
        settle()
    }

    /** The message shows once, as a polite live region, and the ViewModel got the chip's tag. */
    private fun assertRemovedSaidOnce() {
        assertEquals(listOf(JAW_PAIN), removed)
        val shown = rule.onAllNodes(hasText(REMOVED), useUnmergedTree = true).fetchSemanticsNodes()
        assertEquals("one message", 1, shown.size)
        val region = rule.onAllNodes(hasText(REMOVED)).fetchSemanticsNodes().single()
            .let { node -> generateSequence(node) { it.parent } }
            .firstNotNullOfOrNull { it.config.getOrNull(SemanticsProperties.LiveRegion) }
        assertEquals("announced politely", LiveRegionMode.Polite, region)
    }

    @Test
    fun `Removing from the menu says Removed from your list`() {
        show()

        removeThroughTheMenu()

        assertRemovedSaidOnce()
    }

    @Test
    fun `Removing with TalkBack's action says Removed from your list`() {
        show()

        removeWithTheCustomAction()

        assertRemovedSaidOnce()
    }

    @Test
    fun `Leaving the screen ends the message`() {
        show()
        removeWithTheCustomAction()
        rule.onNodeWithText(REMOVED).assertExists()

        rule.runOnUiThread { navController.popBackStack() }
        settle()
        openDetails()

        rule.onNodeWithText(REMOVED).assertDoesNotExist()
    }

    @Test
    fun `A rotation keeps the message as the screen's other messages`() {
        val restoration = StateRestorationTester(rule)
        restoration.setContent {
            MigrelyTheme {
                CompositionLocalProvider(LocalMessageClock provides messageClock) {
                    dependencies.AttackDetails(
                        attackId = "a",
                        announceMerge = false,
                        navController = NavController(rule.activity)
                    )
                }
            }
        }
        settle()
        removeWithTheCustomAction()

        restoration.emulateSavedInstanceStateRestore()
        settle()

        rule.onNodeWithText(REMOVED).assertExists()
    }
}
