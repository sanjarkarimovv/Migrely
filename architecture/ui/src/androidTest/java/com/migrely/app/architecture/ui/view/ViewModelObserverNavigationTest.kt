package com.migrely.app.architecture.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent
import com.migrely.app.architecture.presentation.notification.PresentationNotification
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.architecture.ui.notification.mapper.NoNotificationUiMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

private const val LIST = "list"
private const val GATE = "gate"
private const val EDITOR = "editor/{id}"
private const val ROW_A = "row-a"
private const val ROW_B = "row-b"
private const val LEAVE = "leave"
private const val FADE_STARTED_MILLIS = 100L
private const val LATE_IN_THE_FADE_MILLIS = 600L
private const val AFTER_THE_FADE_MILLIS = 1_000L

/**
 * A real `NavHost` with its default 700 ms fade, in which the leaving screen stays composed and its
 * clickable rows still take touches. What decides is the back stack after the event, so the clock
 * is stopped mid-fade to hold the state the issues describe (#138, #139).
 */
class ViewModelObserverNavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var navController: NavHostController

    private var singleTop = true
    private val mapper = FakeDestinations { singleTop }

    private val listViewModel = ScreenEvents()
    private val gateViewModel = ScreenEvents()
    private val editorViewModel = ScreenEvents()
    private val list = FakeHolder(listViewModel, mapper)
    private val gate = FakeHolder(gateViewModel, mapper)

    // One holder and one ViewModel for every editor entry, as the app's Dependencies do.
    private val editor = FakeHolder(editorViewModel, mapper)

    @Before
    fun setUp() {
        composeRule.setContent {
            navController = rememberNavController()
            NavHost(navController, startDestination = LIST) {
                composable(LIST) {
                    list.ViewModelObserver(navController)
                    Column {
                        Tappable(ROW_A) { listViewModel.onEvent(FakeEvent.Open("A")) }
                        Tappable(ROW_B) { listViewModel.onEvent(FakeEvent.Open("B")) }
                    }
                }
                composable(GATE) {
                    gate.ViewModelObserver(navController)
                    LaunchedEffect(Unit) { gateViewModel.onEvent(FakeEvent.Open("after-gate")) }
                }
                composable(EDITOR) { entry ->
                    editor.ViewModelObserver(navController)
                    Column {
                        Text("Editor ${entry.arguments?.getString("id")}")
                        // Below the rows, so a second touch on a row falls through the entering screen.
                        Box(Modifier.height(300.dp))
                        Tappable(LEAVE) { editorViewModel.onEvent(FakeEvent.Leave) }
                    }
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.mainClock.autoAdvance = false
    }

    @Test
    fun aSecondRowTappedDuringTheFadeDoesNotRetargetTheOpenedScreen() {
        tap(ROW_A)
        advance(FADE_STARTED_MILLIS)
        composeRule.onNodeWithTag(ROW_B).assertExists()

        tap(ROW_B)

        assertEditorOnTheList(expectedId = "A")
    }

    @Test
    fun aRestingFingerLiftedLateInTheFadeDoesNotRetargetTheOpenedScreen() {
        composeRule.onNodeWithTag(ROW_B).performTouchInput { down(1, center) }
        tap(ROW_A)
        advance(LATE_IN_THE_FADE_MILLIS)
        composeRule.onNodeWithTag(ROW_B).assertExists()

        composeRule.onNodeWithTag(ROW_B).performTouchInput { up(1) }
        composeRule.waitForIdle()

        assertEditorOnTheList(expectedId = "A")
    }

    @Test
    fun twoEventsQueuedBeforeTheFirstIsDeliveredOpenOneScreen() {
        composeRule.runOnUiThread {
            listViewModel.onEvent(FakeEvent.Open("A"))
            listViewModel.onEvent(FakeEvent.Open("B"))
        }
        composeRule.waitForIdle()

        assertEditorOnTheList(expectedId = "A")
    }

    @Test
    fun withoutSingleTopTwoRowsDoNotStackTwoScreens() {
        singleTop = false
        tap(ROW_A)
        advance(FADE_STARTED_MILLIS)

        tap(ROW_B)

        assertEditorOnTheList(expectedId = "A")
    }

    @Test
    fun withoutSingleTopADoubleTapOnOneRowDoesNotStackTwoScreens() {
        singleTop = false
        tap(ROW_A)
        advance(FADE_STARTED_MILLIS)

        tap(ROW_A)

        assertEditorOnTheList(expectedId = "A")
    }

    @Test
    fun aFirstTapOnTheEnteringScreenCountsWhileItFadesIn() {
        tap(ROW_A)
        advance(FADE_STARTED_MILLIS)

        tap(LEAVE)

        assertTopRoute(LIST)
    }

    @Test
    fun aScreenThatNavigatesAsItEntersIsObeyedDuringItsFadeIn() {
        composeRule.runOnUiThread { navController.navigate(GATE) }
        composeRule.waitForIdle()
        advance(FADE_STARTED_MILLIS)

        assertEditorOpen(expectedId = "after-gate")
    }

    @Test
    fun anEventAfterAsyncWorkOnTheCurrentScreenIsObeyed() {
        tap(ROW_A)
        advance(AFTER_THE_FADE_MILLIS)

        composeRule.runOnUiThread { editorViewModel.onEvent(FakeEvent.Leave) }
        composeRule.waitForIdle()

        assertTopRoute(LIST)
    }

    @Test
    fun aLateEventFromAScreenAlreadyPoppedDoesNotPopAgain() {
        tap(ROW_A)
        advance(AFTER_THE_FADE_MILLIS)
        composeRule.runOnUiThread { navController.popBackStack() }
        composeRule.waitForIdle()
        advance(FADE_STARTED_MILLIS)

        composeRule.runOnUiThread { editorViewModel.onEvent(FakeEvent.Leave) }
        composeRule.waitForIdle()

        assertTopRoute(LIST)
    }

    @Test
    fun aListEventAfterAFastBackIsObeyedWhileTheEditorFadesOut() {
        tap(ROW_A)
        advance(FADE_STARTED_MILLIS)
        composeRule.runOnUiThread { navController.popBackStack() }
        composeRule.waitForIdle()
        advance(FADE_STARTED_MILLIS)

        composeRule.runOnUiThread { listViewModel.onEvent(FakeEvent.Open("B")) }
        composeRule.waitForIdle()

        assertEditorOnTheList(expectedId = "B")
    }

    @Test
    fun aSingleTopReplacementOfTheCurrentEntryKeepsItCurrent() {
        tap(ROW_A)
        advance(AFTER_THE_FADE_MILLIS)
        composeRule.runOnUiThread { editorViewModel.onEvent(FakeEvent.Open("B")) }
        composeRule.waitForIdle()
        advance(AFTER_THE_FADE_MILLIS)
        assertEditorOnTheList(expectedId = "B")

        composeRule.runOnUiThread { editorViewModel.onEvent(FakeEvent.Leave) }
        composeRule.waitForIdle()

        assertTopRoute(LIST)
    }

    @Test
    fun oneViewModelShownByTwoEntriesIsObeyedOnce() {
        singleTop = false
        tap(ROW_A)
        advance(AFTER_THE_FADE_MILLIS)
        composeRule.runOnUiThread { editorViewModel.onEvent(FakeEvent.Open("B")) }
        composeRule.waitForIdle()
        advance(FADE_STARTED_MILLIS)

        composeRule.runOnUiThread { editorViewModel.onEvent(FakeEvent.Leave) }
        composeRule.waitForIdle()

        assertEditorOnTheList(expectedId = "A")
    }

    private fun tap(tag: String) {
        composeRule.onNodeWithTag(tag).performTouchInput {
            down(center)
            up()
        }
        composeRule.waitForIdle()
    }

    private fun advance(millis: Long) {
        composeRule.mainClock.advanceTimeBy(millis)
        composeRule.waitForIdle()
    }

    private fun assertEditorOnTheList(expectedId: String) = composeRule.runOnIdle {
        assertEditor(expectedId)
        assertEquals(LIST, navController.previousBackStackEntry?.destination?.route)
    }

    private fun assertEditorOpen(expectedId: String) = composeRule.runOnIdle {
        assertEditor(expectedId)
    }

    private fun assertEditor(expectedId: String) {
        val top = navController.currentBackStackEntry
        assertEquals(EDITOR, top?.destination?.route)
        assertEquals(expectedId, top?.arguments?.getString("id"))
    }

    private fun assertTopRoute(route: String) = composeRule.runOnIdle {
        assertEquals(route, navController.currentBackStackEntry?.destination?.route)
        assertNull(navController.previousBackStackEntry)
    }
}

/** Previews and tests can show a screen with no `NavHost`: there is no back stack to ask. */
class ViewModelObserverOutsideNavHostTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun anEventIsObeyedOutsideANavHost() {
        var navigations = 0
        val viewModel = ScreenEvents()
        val holder = FakeHolder(viewModel, CountingDestinations { navigations++ })
        composeRule.setContent { holder.ViewModelObserver(rememberNavController()) }
        composeRule.waitForIdle()

        composeRule.runOnUiThread { viewModel.onEvent(FakeEvent.Leave) }
        composeRule.waitForIdle()

        composeRule.runOnIdle { assertEquals(1, navigations) }
    }
}

@Composable
private fun Tappable(tag: String, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(100.dp)
            .testTag(tag)
            .clickable(onClick = onClick)
    )
}

private sealed interface FakeEvent : PresentationNavigationEvent {
    data class Open(val id: String) : FakeEvent

    data object Leave : FakeEvent
}

private class ScreenEvents : BaseViewModel<Unit, PresentationNotification>(UseCaseExecutor()) {
    fun onEvent(event: FakeEvent) = emitNavigationEvent(event)
}

private class FakeHolder(
    viewModel: ScreenEvents,
    mapper: NavigationEventDestinationMapper<FakeEvent>
) : BaseComposeHolder<Unit, PresentationNotification>(viewModel, mapper, NoNotificationUiMapper())

private class FakeDestinations(private val singleTop: () -> Boolean) :
    NavigationEventDestinationMapper<FakeEvent>(FakeEvent::class) {
    override fun mapTypedEvent(navigationEvent: FakeEvent): UiDestination = when (navigationEvent) {
        is FakeEvent.Open -> UiDestination { open(it, navigationEvent.id) }
        FakeEvent.Leave -> UiDestination { it.popBackStack() }
    }

    private fun open(navController: NavController, id: String) {
        navController.navigate("editor/$id") { launchSingleTop = singleTop() }
    }
}

private class CountingDestinations(private val onNavigate: () -> Unit) :
    NavigationEventDestinationMapper<FakeEvent>(FakeEvent::class) {
    override fun mapTypedEvent(navigationEvent: FakeEvent) = UiDestination { onNavigate() }
}
