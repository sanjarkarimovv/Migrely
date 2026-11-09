package com.migrely.app.attack.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.migrely.app.attack.ui.model.AttackDetailsEventUiModel
import com.migrely.app.attack.ui.model.AttackMessageUiModel
import com.migrely.app.attack.ui.model.IntensityPointUiModel
import com.migrely.app.attack.ui.notification.AttackDetailsEventPresenter
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val TITLE = "Sudden, worst-ever pain?"
private const val GOT_IT = "Got it"
private const val STORAGE_FULL = "Couldn't save. Your phone's storage is full."
private const val SAVED = "Saved."
private const val POINT_REMOVED = "Pain level removed"
private const val DISMISS = "Dismiss"
private const val DELETE = "Delete migraine"
private const val DETAILS_ROUTE = "details"
private const val OTHER_ROUTE = "other"
private const val SETTLE_MILLIS = 1_000L

/**
 * The details screen's messages with the first-10 safety card (BRD D165, EC-UX-33, EC-UX-34; #166, #167): a message
 * never covers the card, TalkBack hears the card first, and an error goes on a later save or when the visit ends.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h700dp")
class SaveMessageTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val events = AttackDetailsEventPresenter()
    private val dependencies = attackDetailsTestDependencies(events)
    private lateinit var navController: NavHostController

    @Before
    fun holdTheClock() {
        rule.mainClock.autoAdvance = false
    }

    private fun show(fontScale: Float = 1f) {
        rule.setContent {
            MigrelyTheme {
                val density = LocalDensity.current
                CompositionLocalProvider(
                    LocalDensity provides Density(density.density, fontScale)
                ) {
                    navController = rememberNavController()
                    NavHost(navController, startDestination = DETAILS_ROUTE) {
                        composable(DETAILS_ROUTE) {
                            dependencies.AttackDetails(
                                attackId = "a",
                                announceMerge = false,
                                navController = navController
                            )
                        }
                        composable(OTHER_ROUTE) { Text("Log medication") }
                    }
                }
            }
        }
        settle()
    }

    private fun settle(millis: Long = SETTLE_MILLIS) = rule.mainClock.advanceTimeBy(millis)

    /** A touch, with the clock running: under a held clock Robolectric dropped a tap on Dismiss, card or not. */
    private fun tap(node: SemanticsNodeInteraction) {
        rule.mainClock.autoAdvance = true
        node.performClick()
        rule.waitForIdle()
        rule.mainClock.autoAdvance = false
        settle()
    }

    /** Scrolls with the clock running: the scroll waits for its own frames. */
    private fun scrollDown() {
        rule.mainClock.autoAdvance = true
        content.performScrollToNode(hasText(DELETE))
        rule.waitForIdle()
        rule.mainClock.autoAdvance = false
    }

    private fun send(vararg sent: AttackDetailsEventUiModel) {
        sent.forEach(events::show)
        settle()
    }

    /** A first 10 on a full disk: the card's event, then the outcome's message, as the ViewModel sends them. */
    private fun failedFirstTen() = send(
        AttackDetailsEventUiModel.FirstMaximumRecorded,
        AttackDetailsEventUiModel.Message(AttackMessageUiModel.StorageFull)
    )

    private fun storageFull() =
        send(AttackDetailsEventUiModel.Message(AttackMessageUiModel.StorageFull))

    private fun saved() = send(AttackDetailsEventUiModel.Saved)

    private fun pointRemoved() = send(
        AttackDetailsEventUiModel.Message(
            AttackMessageUiModel.PointRemoved("a", IntensityPointUiModel(1L, 6))
        )
    )

    private val card: SemanticsNodeInteraction get() = rule.onNodeWithText(TITLE).onParent()
    private val content get() = rule.onNode(hasScrollAction())

    private fun message(text: String) = rule.onNode(
        SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion) and
            hasAnyDescendant(hasText(text))
    )

    private fun bounds(node: SemanticsNodeInteraction): DpRect = node.getUnclippedBoundsInRoot()

    private fun liveRegionOf(node: SemanticsNodeInteraction): LiveRegionMode =
        node.fetchSemanticsNode().config[SemanticsProperties.LiveRegion]

    private fun assertBelow(upper: DpRect, lowerTop: Dp, what: String) = assertTrue(
        "$what starts at $lowerTop, above the message's bottom ${upper.bottom}",
        lowerTop >= upper.bottom
    )

    /** Check 1: the card is whole, below the message, and nothing of the content is above the message's bottom. */
    private fun assertCardClearOfMessage(text: String = STORAGE_FULL) {
        val shown = bounds(message(text))
        assertBelow(shown, bounds(card).top, "the card")
        assertBelow(shown, bounds(content).top, "the content")
        rule.onNodeWithText(GOT_IT).assertExistsWithin(bounds(content))
    }

    private fun SemanticsNodeInteraction.assertExistsWithin(area: DpRect) {
        val own = getUnclippedBoundsInRoot()
        assertTrue("$own outside $area", own.top >= area.top && own.bottom <= area.bottom)
    }

    // D165 (1): push, don't cover.

    @Test
    fun `Given a failed first 10 at 100 percent then the message lies above the card`() {
        show()

        failedFirstTen()

        assertCardClearOfMessage()
    }

    @Test
    fun `Given a failed first 10 at 200 percent then the message lies above the card`() {
        show(fontScale = 2f)

        failedFirstTen()

        assertCardClearOfMessage()
    }

    @Test
    @Config(qualifiers = "w700dp-h400dp")
    fun `Given a failed first 10 in landscape at 200 percent then the card is below it`() {
        show(fontScale = 2f)

        failedFirstTen()

        val shown = bounds(message(STORAGE_FULL))
        assertBelow(shown, bounds(card).top, "the card")
        assertBelow(shown, bounds(content).top, "the content")
    }

    @Test
    fun `Given the content scrolled down then a failed first 10 brings the card below it`() {
        show()
        scrollDown()

        failedFirstTen()

        // D167 brings the content back to its top, which in card mode is below the message.
        assertCardClearOfMessage()
    }

    @Test
    fun `Given the card and the user scrolled away then an Undo message covers nothing`() {
        show()
        send(AttackDetailsEventUiModel.FirstMaximumRecorded)
        scrollDown()

        pointRemoved()

        assertBelow(bounds(message(POINT_REMOVED)), bounds(content).top, "the content")
    }

    @Test
    fun `Given Got it while the message shows then both message and content stay put`() {
        show()
        failedFirstTen()
        val before = bounds(message(STORAGE_FULL))

        tap(rule.onNodeWithText(GOT_IT))

        rule.onNodeWithText(TITLE).assertDoesNotExist()
        val after = bounds(message(STORAGE_FULL))
        assertEquals(before.top.value, after.top.value, 0.5f)
        assertEquals(before.bottom.value, after.bottom.value, 0.5f)
        assertBelow(after, bounds(content).top, "the content")
    }

    @Test
    fun `Given Got it and then the message leaves then the content comes back up`() {
        show()
        failedFirstTen()
        val shown = bounds(message(STORAGE_FULL))
        tap(rule.onNodeWithText(GOT_IT))

        tap(rule.onNodeWithContentDescription(DISMISS))

        rule.onNodeWithText(STORAGE_FULL).assertDoesNotExist()
        assertTrue(
            "content ${bounds(content).top} message was ${shown.bottom}",
            bounds(content).top < shown.bottom
        )
    }

    @Test
    fun `Given card mode then Dismiss closes the message`() {
        show()
        failedFirstTen()

        tap(rule.onNodeWithContentDescription(DISMISS))

        rule.onNodeWithText(STORAGE_FULL).assertDoesNotExist()
    }

    @Test
    fun `Given no card then Dismiss closes the message as before`() {
        show()
        storageFull()

        tap(rule.onNodeWithContentDescription(DISMISS))

        rule.onNodeWithText(STORAGE_FULL).assertDoesNotExist()
    }

    @Test
    fun `Given no card then a message lies over the first row as before`() {
        show()

        storageFull()

        assertTrue(bounds(content).top < bounds(message(STORAGE_FULL)).bottom)
    }

    // D165 (3): TalkBack hears the card first.

    @Test
    fun `Given a failed first 10 then the message appears only after the card is announced`() {
        show()

        events.show(AttackDetailsEventUiModel.FirstMaximumRecorded)
        events.show(AttackDetailsEventUiModel.Message(AttackMessageUiModel.StorageFull))
        repeat(3) { rule.mainClock.advanceTimeByFrame() }

        assertEquals(LiveRegionMode.Assertive, liveRegionOf(card))
        rule.onNodeWithText(STORAGE_FULL).assertDoesNotExist()
        // Not in the card's batch of accessibility events either.
        rule.mainClock.advanceTimeBy(120)
        rule.onNodeWithText(STORAGE_FULL).assertDoesNotExist()
        settle()
        rule.onNodeWithText(STORAGE_FULL).assertExists()
    }

    @Test
    fun `Given an error when the 10 is added again and saves then Saved waits for the card`() {
        show()
        storageFull()

        events.show(AttackDetailsEventUiModel.FirstMaximumRecorded)
        events.show(AttackDetailsEventUiModel.Saved)
        repeat(3) { rule.mainClock.advanceTimeByFrame() }

        assertEquals(LiveRegionMode.Assertive, liveRegionOf(card))
        rule.onNodeWithText(SAVED).assertDoesNotExist()
        rule.mainClock.advanceTimeBy(120)
        rule.onNodeWithText(SAVED).assertDoesNotExist()
        settle()
        rule.onNodeWithText(SAVED).assertExists()
        rule.onNodeWithText(STORAGE_FULL).assertDoesNotExist()
    }

    @Test
    fun `Given a message held behind the card when the screen is rebuilt then it still shows`() {
        var composed by mutableStateOf(true)
        rule.setContent {
            val saved = rememberSaveableStateHolder()
            MigrelyTheme {
                // Torn down and rebuilt with its saved state, as on an activity recreation.
                if (composed) {
                    saved.SaveableStateProvider("details") {
                        dependencies.AttackDetails(
                            attackId = "a",
                            announceMerge = false,
                            navController = NavController(rule.activity)
                        )
                    }
                }
            }
        }
        settle()

        events.show(AttackDetailsEventUiModel.FirstMaximumRecorded)
        events.show(AttackDetailsEventUiModel.Message(AttackMessageUiModel.StorageFull))
        repeat(2) { rule.mainClock.advanceTimeByFrame() }
        rule.onNodeWithText(STORAGE_FULL).assertDoesNotExist()
        composed = false
        Snapshot.sendApplyNotifications()
        repeat(2) { rule.mainClock.advanceTimeByFrame() }
        composed = true
        Snapshot.sendApplyNotifications()
        settle()

        rule.onNodeWithText(TITLE).assertExists()
        rule.onNodeWithText(STORAGE_FULL).assertExists()
    }

    @Test
    fun `Given the card shows then an error is polite`() {
        show()

        failedFirstTen()

        assertEquals(LiveRegionMode.Polite, liveRegionOf(message(STORAGE_FULL)))
        assertEquals(LiveRegionMode.Assertive, liveRegionOf(card))
    }

    @Test
    fun `Given no card then an error is assertive as before`() {
        show()

        storageFull()

        assertEquals(LiveRegionMode.Assertive, liveRegionOf(message(STORAGE_FULL)))
    }

    @Test
    fun `The card's title is a heading`() {
        show()

        send(AttackDetailsEventUiModel.FirstMaximumRecorded)

        rule.onNodeWithText(TITLE).assert(isHeading())
    }

    // D165 (4): an error goes when a later write saves.

    @Test
    fun `Given an error when a later write saves then Saved replaces it`() {
        show()
        storageFull()

        saved()

        rule.onNodeWithText(STORAGE_FULL).assertDoesNotExist()
        assertEquals(LiveRegionMode.Polite, liveRegionOf(message(SAVED)))
    }

    @Test
    fun `Given Saved then it leaves after its 8 seconds`() {
        show()
        storageFull()
        saved()

        settle(9_000)

        rule.onNodeWithText(SAVED).assertDoesNotExist()
    }

    @Test
    fun `Given no error when a write saves then nothing shows`() {
        show()

        saved()

        rule.onNodeWithText(SAVED).assertDoesNotExist()
    }

    @Test
    fun `Given a message with Undo when a write saves then the message stays`() {
        show()
        pointRemoved()

        saved()

        rule.onNodeWithText(POINT_REMOVED).assertExists()
        rule.onNodeWithText(SAVED).assertDoesNotExist()
    }

    @Test
    fun `Given an error when a write brings its own message then that message replaces it`() {
        show()
        storageFull()

        pointRemoved()

        rule.onNodeWithText(STORAGE_FULL).assertDoesNotExist()
        rule.onNodeWithText(POINT_REMOVED).assertExists()
    }

    @Test
    fun `Given Saved replaced an error then a second save shows nothing new`() {
        show()
        storageFull()
        saved()
        settle(4_000)

        saved()

        // Still the first one, 5 s in: a new one would have restarted its 8 s.
        settle(4_000)
        rule.onNodeWithText(SAVED).assertDoesNotExist()
    }

    // D165 (5): an error belongs to the visit.

    private fun goForwardAndBack() {
        rule.runOnUiThread { navController.navigate(OTHER_ROUTE) }
        settle()
        rule.onNodeWithText("Log medication").assertExists()
        rule.runOnUiThread { navController.popBackStack() }
        settle()
    }

    @Test
    fun `Given an error when the user goes forward and back then it is gone and the card stays`() {
        show()
        failedFirstTen()

        goForwardAndBack()

        rule.onNodeWithText(STORAGE_FULL).assertDoesNotExist()
        rule.onNodeWithText(TITLE).assertExists()
    }

    @Test
    fun `Given an error held behind the card when the user goes forward then it is gone after`() {
        show()

        events.show(AttackDetailsEventUiModel.FirstMaximumRecorded)
        events.show(AttackDetailsEventUiModel.Message(AttackMessageUiModel.StorageFull))
        repeat(2) { rule.mainClock.advanceTimeByFrame() }
        rule.onNodeWithText(STORAGE_FULL).assertDoesNotExist()
        goForwardAndBack()

        rule.onNodeWithText(STORAGE_FULL).assertDoesNotExist()
        rule.onNodeWithText(TITLE).assertExists()
    }

    @Test
    fun `Given a message with Undo when the user goes forward and back then it is still there`() {
        show()
        pointRemoved()

        goForwardAndBack()

        rule.onNodeWithText(POINT_REMOVED).assertExists()
    }

    @Test
    fun `Given an error when the app goes to the background and back then it is still there`() {
        show()
        storageFull()

        rule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        settle()
        rule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        settle()

        rule.onNodeWithText(STORAGE_FULL).assertExists()
    }

    @Test
    fun `Given the user came back then a new error shows as usual`() {
        show()
        storageFull()
        goForwardAndBack()

        storageFull()

        rule.onNodeWithText(STORAGE_FULL).assertExists()
    }

    @Test
    fun `Given an error and the card when restored then the card is still not covered`() {
        val restoration = StateRestorationTester(rule)
        restoration.setContent {
            MigrelyTheme {
                dependencies.AttackDetails(
                    attackId = "a",
                    announceMerge = false,
                    navController = NavController(rule.activity)
                )
            }
        }
        settle()
        failedFirstTen()

        restoration.emulateSavedInstanceStateRestore()
        settle()

        assertCardClearOfMessage()
    }
}
