package com.migrely.app.reminders.ui.mapper

import com.migrely.app.reminders.presentation.model.ReminderKindPresentationModel
import com.migrely.app.reminders.presentation.model.RemindersPresentationModel
import com.migrely.app.reminders.presentation.model.RemindersPresentationNotification
import com.migrely.app.reminders.presentation.model.RemindersViewState
import com.migrely.app.reminders.ui.model.ReminderKindUiModel
import com.migrely.app.reminders.ui.model.RemindersEventUiModel
import com.migrely.app.reminders.ui.model.RemindersMessageUiModel
import com.migrely.app.reminders.ui.model.RemindersUiModel
import com.migrely.app.reminders.ui.model.RemindersViewStateUiModel
import java.time.LocalTime
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RemindersUiMappersTest {
    @Test
    fun `When mapping a ready state then the time as minutes after midnight`() {
        assertEquals(
            RemindersViewStateUiModel.Ready(
                RemindersUiModel(true, 21 * 60 + 30, false, true, false)
            ),
            RemindersViewStateUiMapper().toUi(
                RemindersViewState.Ready(
                    RemindersPresentationModel(true, LocalTime.of(21, 30), false, true, false)
                )
            )
        )
        assertEquals(
            RemindersViewStateUiModel.Unavailable,
            RemindersViewStateUiMapper().toUi(RemindersViewState.Unavailable)
        )
    }

    @Test
    fun `When a permission request or a message is presented then the presenter shows it`() =
        runTest {
            val presenter = RemindersEventPresenter()
            val classUnderTest = RemindersNotificationUiMapper(presenter)
            val first = async(start = UNDISPATCHED) { presenter.events.first() }

            classUnderTest.toUi(
                RemindersPresentationNotification.AskForPermission(
                    ReminderKindPresentationModel.Effect
                )
            ).present()

            assertEquals(
                RemindersEventUiModel.AskForPermission(ReminderKindUiModel.Effect),
                first.await()
            )

            val second = async(start = UNDISPATCHED) { presenter.events.first() }
            classUnderTest.toUi(RemindersPresentationNotification.StorageFull).present()
            assertEquals(
                RemindersEventUiModel.Message(RemindersMessageUiModel.StorageFull),
                second.await()
            )
        }
}
