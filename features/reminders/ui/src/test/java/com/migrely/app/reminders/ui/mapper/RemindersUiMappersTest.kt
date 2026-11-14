package com.migrely.app.reminders.ui.mapper

import com.migrely.app.reminders.presentation.model.DoctorVisitPresentationModel
import com.migrely.app.reminders.presentation.model.ReminderKindPresentationModel
import com.migrely.app.reminders.presentation.model.RemindersPresentationModel
import com.migrely.app.reminders.presentation.model.RemindersPresentationNotification
import com.migrely.app.reminders.presentation.model.RemindersViewState
import com.migrely.app.reminders.ui.model.DoctorVisitUiModel
import com.migrely.app.reminders.ui.model.ReminderKindUiModel
import com.migrely.app.reminders.ui.model.RemindersEventUiModel
import com.migrely.app.reminders.ui.model.RemindersMessageUiModel
import com.migrely.app.reminders.ui.model.RemindersUiModel
import com.migrely.app.reminders.ui.model.RemindersViewStateUiModel
import java.time.LocalDate
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
                RemindersUiModel(true, 21 * 60 + 30, false, true, false, 20_767L)
            ),
            RemindersViewStateUiMapper().toUi(
                RemindersViewState.Ready(
                    RemindersPresentationModel(
                        true,
                        LocalTime.of(21, 30),
                        false,
                        true,
                        false,
                        LocalDate.ofEpochDay(20_767L)
                    )
                )
            )
        )
        assertEquals(
            RemindersViewStateUiModel.Unavailable,
            RemindersViewStateUiMapper().toUi(RemindersViewState.Unavailable)
        )
    }

    /** Issue #187: the screen draws the day it was read for and whether Remove is offered, never its own clock. */
    @Test
    fun `When mapping a visit then today, the visit and whether it may be removed`() {
        val reminders = RemindersPresentationModel(
            dailyEnabled = false,
            dailyTime = LocalTime.of(20, 0),
            effectEnabled = true,
            notificationsAllowed = true,
            mayAskPermission = true,
            today = LocalDate.ofEpochDay(20_769L),
            nextVisit = LocalDate.ofEpochDay(20_769L),
            visitRemovable = false,
            visitChannelOff = true
        )

        assertEquals(
            RemindersViewStateUiModel.Ready(
                RemindersUiModel(
                    dailyEnabled = false,
                    dailyMinuteOfDay = 20 * 60,
                    effectEnabled = true,
                    notificationsAllowed = true,
                    mayAskPermission = true,
                    todayEpochDay = 20_769L,
                    nextVisitEpochDay = 20_769L,
                    visitRemovable = false,
                    visitChannelOff = true
                )
            ),
            RemindersViewStateUiMapper().toUi(RemindersViewState.Ready(reminders))
        )
        assertEquals(
            true,
            (
                RemindersViewStateUiMapper().toUi(
                    RemindersViewState.Ready(reminders.copy(visitRemovable = true))
                ) as RemindersViewStateUiModel.Ready
                ).reminders.visitRemovable
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

            val third = async(start = UNDISPATCHED) { presenter.events.first() }
            classUnderTest.toUi(RemindersPresentationNotification.Saved).present()
            assertEquals(
                RemindersEventUiModel.Message(RemindersMessageUiModel.Saved),
                third.await()
            )
        }

    @Test
    fun `When Visit removed or a visit request is presented then the screen gets them`() = runTest {
        val presenter = RemindersEventPresenter()
        val classUnderTest = RemindersNotificationUiMapper(presenter)
        val removed = async(start = UNDISPATCHED) { presenter.events.first() }

        classUnderTest.toUi(
            RemindersPresentationNotification.VisitRemoved(
                DoctorVisitPresentationModel("v", LocalDate.ofEpochDay(20_777L))
            )
        ).present()

        assertEquals(
            RemindersEventUiModel.Message(
                RemindersMessageUiModel.VisitRemoved(DoctorVisitUiModel("v", 20_777L))
            ),
            removed.await()
        )
        val ask = async(start = UNDISPATCHED) { presenter.events.first() }
        classUnderTest.toUi(
            RemindersPresentationNotification.AskForPermission(
                ReminderKindPresentationModel.Visit
            )
        ).present()
        assertEquals(
            RemindersEventUiModel.AskForPermission(ReminderKindUiModel.Visit),
            ask.await()
        )
        assertEquals(
            DoctorVisitPresentationModel("v", LocalDate.ofEpochDay(20_777L)),
            DoctorVisitUiToPresentationMapper().toPresentation(DoctorVisitUiModel("v", 20_777L))
        )
    }
}
