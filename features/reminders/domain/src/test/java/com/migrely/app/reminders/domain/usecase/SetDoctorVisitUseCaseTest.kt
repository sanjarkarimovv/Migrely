package com.migrely.app.reminders.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.CurrentTimeDomainModel
import com.migrely.app.reminders.domain.model.DoctorVisitDomainModel
import com.migrely.app.reminders.domain.model.DoctorVisitsResultDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsResultDomainModel
import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import com.migrely.app.reminders.domain.model.RemoveDoctorVisitResultDomainModel
import com.migrely.app.reminders.domain.model.SetDoctorVisitResultDomainModel
import com.migrely.app.reminders.domain.repository.CancelVisitReminderRepository
import com.migrely.app.reminders.domain.repository.DismissVisitReminderRepository
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.GetDoctorVisitsRepository
import com.migrely.app.reminders.domain.repository.GetNotificationsAllowedRepository
import com.migrely.app.reminders.domain.repository.GetReminderSettingsRepository
import com.migrely.app.reminders.domain.repository.RemoveNextVisitRepository
import com.migrely.app.reminders.domain.repository.RestoreNextVisitRepository
import com.migrely.app.reminders.domain.repository.ScheduleVisitReminderRepository
import com.migrely.app.reminders.domain.repository.SetNextVisitRepository
import com.migrely.app.reminders.domain.repository.SetVisitRemindedRepository
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

private val TASHKENT = ZoneId.of("Asia/Tashkent")
private val NOVEMBER_20: LocalDate = LocalDate.of(2026, 11, 20)
private val NOVEMBER_27: LocalDate = LocalDate.of(2026, 11, 27)
private val VISIT = DoctorVisitDomainModel("v", NOVEMBER_20)

private fun at(text: String) = LocalDateTime.parse(text).atZone(TASHKENT).toInstant()

/** Setting, removing and restoring the next visit (BRD REM-F4, EC-REM-07 to EC-REM-11, design D3, D4). */
class SetDoctorVisitUseCaseTest {
    private val getReminderSettingsRepository: GetReminderSettingsRepository = mock()
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val getDoctorVisitsRepository: GetDoctorVisitsRepository = mock()
    private val getNotificationsAllowedRepository: GetNotificationsAllowedRepository = mock {
        on { notificationsAllowed() } doReturn true
    }
    private val setNextVisitRepository: SetNextVisitRepository = mock {
        on { setNextVisit(any(), any()) } doReturn ReminderWriteResultDomainModel.Done
    }
    private val restoreNextVisitRepository: RestoreNextVisitRepository = mock {
        on { restoreNextVisit(any(), any()) } doReturn ReminderWriteResultDomainModel.Done
    }
    private val removeNextVisitRepository: RemoveNextVisitRepository = mock()
    private val setVisitRemindedRepository: SetVisitRemindedRepository = mock {
        on { setVisitReminded(any()) } doReturn ReminderWriteResultDomainModel.Done
    }
    private val dismissVisitReminderRepository: DismissVisitReminderRepository = mock()
    private val scheduleVisitReminderRepository: ScheduleVisitReminderRepository = mock()
    private val cancelVisitReminderRepository: CancelVisitReminderRepository = mock()
    private lateinit var classUnderTest: SetDoctorVisitUseCase
    private lateinit var restore: RestoreDoctorVisitUseCase
    private lateinit var remove: RemoveDoctorVisitUseCase

    @Before
    fun setUp() {
        classUnderTest = SetDoctorVisitUseCase(
            getReminderSettingsRepository,
            getCurrentTimeRepository,
            getDoctorVisitsRepository,
            getNotificationsAllowedRepository,
            setNextVisitRepository,
            setVisitRemindedRepository,
            dismissVisitReminderRepository,
            scheduleVisitReminderRepository,
            cancelVisitReminderRepository,
            CoroutineContextProvider.Default
        )
        restore = RestoreDoctorVisitUseCase(
            getReminderSettingsRepository,
            getCurrentTimeRepository,
            restoreNextVisitRepository,
            setVisitRemindedRepository,
            dismissVisitReminderRepository,
            scheduleVisitReminderRepository,
            cancelVisitReminderRepository,
            CoroutineContextProvider.Default
        )
        remove = RemoveDoctorVisitUseCase(
            getCurrentTimeRepository,
            removeNextVisitRepository,
            cancelVisitReminderRepository,
            dismissVisitReminderRepository,
            CoroutineContextProvider.Default
        )
        givenNow("2026-11-10T12:00")
        givenSettings(askedAt = null)
        givenNextVisit(null)
    }

    private fun givenNow(text: String) {
        given(
            getCurrentTimeRepository.currentTime()
        ).willReturn(CurrentTimeDomainModel(at(text), TASHKENT))
    }

    private fun givenSettings(askedAt: Instant?) {
        given(getReminderSettingsRepository.reminderSettings()).willReturn(
            ReminderSettingsResultDomainModel.Loaded(
                ReminderSettingsDomainModel(false, LocalTime.of(20, 0), true, askedAt, null)
            )
        )
    }

    private fun givenNextVisit(visit: DoctorVisitDomainModel?) {
        given(getDoctorVisitsRepository.doctorVisits(any()))
            .willReturn(DoctorVisitsResultDomainModel.Loaded(visit, null))
    }

    @Test
    fun `Given November 10 when November 20 is set then saved and planned for November 18`() {
        val result = classUnderTest.executeInBackground(NOVEMBER_20)

        assertEquals(SetDoctorVisitResultDomainModel.Saved(askForPermission = false), result)
        verify(setNextVisitRepository).setNextVisit(NOVEMBER_20, LocalDate.of(2026, 11, 10))
        verify(
            scheduleVisitReminderRepository
        ).scheduleVisitReminder(at("2026-11-18T10:00"), NOVEMBER_20)
        verify(setVisitRemindedRepository, never()).setVisitReminded(any())
    }

    @Test
    fun `Given November 20 when November 27 is set then the old notification goes`() {
        givenNextVisit(VISIT)

        classUnderTest.executeInBackground(NOVEMBER_27)

        verify(dismissVisitReminderRepository).dismissVisitReminder()
        verify(
            scheduleVisitReminderRepository
        ).scheduleVisitReminder(at("2026-11-25T10:00"), NOVEMBER_27)
    }

    @Test
    fun `Given November 20 when it is set again then nothing is saved or planned`() {
        givenNextVisit(VISIT)

        assertEquals(
            SetDoctorVisitResultDomainModel.Unchanged,
            classUnderTest.executeInBackground(NOVEMBER_20)
        )
        verify(setNextVisitRepository, never()).setNextVisit(any(), any())
        verifyNoInteractions(scheduleVisitReminderRepository, cancelVisitReminderRepository)
    }

    @Test
    fun `Given 3 PM on November 18 when November 20 is set then marked reminded first`() {
        givenNow("2026-11-18T15:00")

        classUnderTest.executeInBackground(NOVEMBER_20)

        inOrder(setVisitRemindedRepository, setNextVisitRepository) {
            verify(setVisitRemindedRepository).setVisitReminded(NOVEMBER_20)
            verify(setNextVisitRepository).setNextVisit(NOVEMBER_20, LocalDate.of(2026, 11, 18))
        }
        verify(cancelVisitReminderRepository).cancelVisitReminder()
        verify(scheduleVisitReminderRepository, never()).scheduleVisitReminder(any(), any())
    }

    @Test
    fun `Given 9 AM on November 18 when November 20 is set then planned at 10 AM (control)`() {
        givenNow("2026-11-18T09:00")

        classUnderTest.executeInBackground(NOVEMBER_20)

        verify(setVisitRemindedRepository, never()).setVisitReminded(any())
        verify(
            scheduleVisitReminderRepository
        ).scheduleVisitReminder(at("2026-11-18T10:00"), NOVEMBER_20)
    }

    @Test
    fun `Given a day before today or past 365 days when set then nothing is saved`() {
        assertEquals(
            SetDoctorVisitResultDomainModel.Failed,
            classUnderTest.executeInBackground(LocalDate.of(2026, 11, 9))
        )
        assertEquals(
            SetDoctorVisitResultDomainModel.Failed,
            classUnderTest.executeInBackground(LocalDate.of(2027, 11, 11))
        )
        assertEquals(
            SetDoctorVisitResultDomainModel.Saved(false),
            classUnderTest.executeInBackground(LocalDate.of(2027, 11, 10))
        )
    }

    @Test
    fun `Given notifications off when a visit is set then saved, asking by the 30-day rule`() {
        given(getNotificationsAllowedRepository.notificationsAllowed()).willReturn(false)
        assertEquals(
            SetDoctorVisitResultDomainModel.Saved(true),
            classUnderTest.executeInBackground(NOVEMBER_20)
        )

        givenSettings(askedAt = at("2026-11-10T12:00").minus(Duration.ofDays(10)))
        givenNextVisit(null)
        assertEquals(
            SetDoctorVisitResultDomainModel.Saved(false),
            classUnderTest.executeInBackground(NOVEMBER_27)
        )
    }

    @Test
    fun `Given storage is full when a visit is set then it says so and plans nothing`() {
        given(
            setNextVisitRepository.setNextVisit(any(), any())
        ).willReturn(ReminderWriteResultDomainModel.StorageFull)

        assertEquals(
            SetDoctorVisitResultDomainModel.StorageFull,
            classUnderTest.executeInBackground(NOVEMBER_20)
        )
        verifyNoInteractions(
            scheduleVisitReminderRepository,
            cancelVisitReminderRepository,
            dismissVisitReminderRepository
        )
    }

    @Test
    fun `Given a visit when removed then its run and notification go, and Undo gets it`() {
        given(removeNextVisitRepository.removeNextVisit(LocalDate.of(2026, 11, 10)))
            .willReturn(RemoveDoctorVisitResultDomainModel.Removed(VISIT))

        assertEquals(
            RemoveDoctorVisitResultDomainModel.Removed(VISIT),
            remove.executeInBackground(Unit)
        )
        verify(cancelVisitReminderRepository).cancelVisitReminder()
        verify(dismissVisitReminderRepository).dismissVisitReminder()
    }

    @Test
    fun `Given no visit when removed then nothing is cancelled`() {
        given(removeNextVisitRepository.removeNextVisit(any()))
            .willReturn(RemoveDoctorVisitResultDomainModel.NothingToRemove)

        remove.executeInBackground(Unit)

        verifyNoInteractions(cancelVisitReminderRepository, dismissVisitReminderRepository)
    }

    @Test
    fun `Given a removed visit when Undo then it comes back and is planned again`() {
        assertEquals(ReminderWriteResultDomainModel.Done, restore.executeInBackground(VISIT))

        verify(restoreNextVisitRepository).restoreNextVisit(VISIT, LocalDate.of(2026, 11, 10))
        verify(
            scheduleVisitReminderRepository
        ).scheduleVisitReminder(at("2026-11-18T10:00"), NOVEMBER_20)
    }

    @Test
    fun `Given removed at 9-59-58 when Undo comes at 10-00-02 then nothing posts`() {
        givenNow("2026-11-18T10:00:02")

        restore.executeInBackground(VISIT)

        verify(setVisitRemindedRepository).setVisitReminded(NOVEMBER_20)
        verify(scheduleVisitReminderRepository, never()).scheduleVisitReminder(any(), any())
    }
}
