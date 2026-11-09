package com.migrely.app.reminders.ui.view

import android.app.Application
import android.content.Intent
import android.os.Looper
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.rememberNavController
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.reminders.presentation.model.RemindersPresentationNotification
import com.migrely.app.reminders.presentation.navigation.RemindersPresentationNavigationEvent
import com.migrely.app.reminders.presentation.viewmodel.RemindersViewModel
import com.migrely.app.reminders.ui.di.RemindersDependencies
import com.migrely.app.reminders.ui.mapper.DoctorVisitUiToPresentationMapper
import com.migrely.app.reminders.ui.mapper.ReminderKindUiToPresentationMapper
import com.migrely.app.reminders.ui.mapper.RemindersEventPresenter
import com.migrely.app.reminders.ui.mapper.RemindersMessageBehaviourUiMapper
import com.migrely.app.reminders.ui.mapper.RemindersViewStateUiMapper
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Issue #187: a Reminders screen kept open reads again when the phone's date may have moved, and stops listening once
 * it is stopped or gone. SDK 35: Robolectric's Android 16 sandbox needs a JDK internals export.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS")
class ClockChangeTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun broadcast(action: String) {
        RuntimeEnvironment.getApplication().sendBroadcast(Intent(action))
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun givenTheRemindersScreenShows_whenTheDateChanges_thenItsViewModelIsTold() {
        val viewModel: RemindersViewModel = mock {
            on { viewState } doReturn emptyFlow()
            on { notification } doReturn emptyFlow()
            on { navigationEvent } doReturn emptyFlow()
        }
        val dependencies = RemindersDependencies(
            remindersViewModel = viewModel,
            remindersViewStateUiMapper = RemindersViewStateUiMapper(),
            remindersEventPresenter = RemindersEventPresenter(),
            remindersMessageBehaviourUiMapper = RemindersMessageBehaviourUiMapper(),
            reminderKindUiToPresentationMapper = ReminderKindUiToPresentationMapper(),
            doctorVisitUiToPresentationMapper = DoctorVisitUiToPresentationMapper(),
            visitNotificationChannelId = "visit",
            navigationMapper =
            mock<NavigationEventDestinationMapper<RemindersPresentationNavigationEvent>>(),
            notificationMapper = mock<NotificationUiMapper<RemindersPresentationNotification>>()
        )
        composeTestRule.setContent { dependencies.Reminders(rememberNavController()) }
        composeTestRule.waitForIdle()
        verify(viewModel, never()).onClockChanged()

        broadcast(Intent.ACTION_DATE_CHANGED)

        composeTestRule.runOnIdle { verify(viewModel).onClockChanged() }
    }

    @Test
    fun givenTheScreenStops_thenNoChangeReachesIt_untilItStartsAgain() {
        var changes = 0
        val owner = object : LifecycleOwner {
            val registry = LifecycleRegistry(this)
            override val lifecycle: Lifecycle get() = registry
        }
        composeTestRule.runOnUiThread { owner.registry.currentState = Lifecycle.State.RESUMED }
        composeTestRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                ClockChangeEffect { changes++ }
            }
        }

        composeTestRule.runOnIdle { owner.registry.currentState = Lifecycle.State.CREATED }
        broadcast(Intent.ACTION_TIME_CHANGED)
        composeTestRule.runOnIdle { assertEquals("stopped", 0, changes) }

        composeTestRule.runOnIdle { owner.registry.currentState = Lifecycle.State.STARTED }
        broadcast(Intent.ACTION_TIME_CHANGED)
        composeTestRule.runOnIdle { assertEquals("started again", 1, changes) }
    }

    @Test
    fun givenTheEffectLeavesTheScreen_thenNoChangeReachesIt() {
        var changes = 0
        val shown = mutableStateOf(true)
        composeTestRule.setContent {
            if (shown.value) ClockChangeEffect { changes++ }
        }

        composeTestRule.runOnIdle { shown.value = false }
        composeTestRule.waitForIdle()
        broadcast(Intent.ACTION_DATE_CHANGED)

        composeTestRule.runOnIdle { assertEquals(0, changes) }
    }
}
