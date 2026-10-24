package com.migrely.app.reminders.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.CurrentTimeDomainModel
import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import com.migrely.app.reminders.domain.model.SetDailyReminderRequestDomainModel
import com.migrely.app.reminders.domain.repository.CancelDailyReminderRepository
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.GetNotificationsAllowedRepository
import com.migrely.app.reminders.domain.repository.ScheduleDailyReminderRepository
import com.migrely.app.reminders.domain.repository.SetDailyReminderRepository
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

private val TASHKENT = ZoneId.of("Asia/Tashkent")

class SetDailyReminderUseCaseTest {
    private val setDailyReminderRepository: SetDailyReminderRepository = mock()
    private val getNotificationsAllowedRepository: GetNotificationsAllowedRepository = mock()
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val scheduleDailyReminderRepository: ScheduleDailyReminderRepository = mock()
    private val cancelDailyReminderRepository: CancelDailyReminderRepository = mock()
    private lateinit var classUnderTest: SetDailyReminderUseCase

    @Before
    fun setUp() {
        classUnderTest = SetDailyReminderUseCase(
            setDailyReminderRepository,
            getNotificationsAllowedRepository,
            getCurrentTimeRepository,
            scheduleDailyReminderRepository,
            cancelDailyReminderRepository,
            CoroutineContextProvider.Default
        )
        // 15:00 in Tashkent.
        given(getCurrentTimeRepository.currentTime())
            .willReturn(CurrentTimeDomainModel(Instant.parse("2026-10-24T10:00:00Z"), TASHKENT))
        given(setDailyReminderRepository.setDailyReminder(any(), any()))
            .willReturn(ReminderWriteResultDomainModel.Done)
    }

    private fun request(enabled: Boolean) =
        SetDailyReminderRequestDomainModel(enabled, LocalTime.of(21, 30))

    @Test
    fun `Given notifications are allowed when turning it on then saved and planned for tonight`() {
        given(getNotificationsAllowedRepository.notificationsAllowed()).willReturn(true)

        assertEquals(
            ReminderWriteResultDomainModel.Done,
            classUnderTest.executeInBackground(request(true))
        )

        verify(setDailyReminderRepository).setDailyReminder(true, 21 * 60 + 30)
        verify(
            scheduleDailyReminderRepository
        ).scheduleDailyReminder(Instant.parse("2026-10-24T16:30:00Z"))
    }

    @Test
    fun `Given notifications are off, or it is turned off, when saving then nothing is planned`() {
        given(getNotificationsAllowedRepository.notificationsAllowed()).willReturn(false)
        classUnderTest.executeInBackground(request(true))

        given(getNotificationsAllowedRepository.notificationsAllowed()).willReturn(true)
        classUnderTest.executeInBackground(request(false))

        verify(cancelDailyReminderRepository, org.mockito.kotlin.times(2)).cancelDailyReminder()
        verify(scheduleDailyReminderRepository, never()).scheduleDailyReminder(any())
    }

    @Test
    fun `Given saving fails when turning it on then nothing is planned`() {
        given(setDailyReminderRepository.setDailyReminder(any(), any()))
            .willReturn(ReminderWriteResultDomainModel.StorageFull)
        given(getNotificationsAllowedRepository.notificationsAllowed()).willReturn(true)

        assertEquals(
            ReminderWriteResultDomainModel.StorageFull,
            classUnderTest.executeInBackground(request(true))
        )
        verify(scheduleDailyReminderRepository, never()).scheduleDailyReminder(any())
    }
}
