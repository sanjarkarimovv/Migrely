package com.migrely.app.reminders.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.CurrentTimeDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsResultDomainModel
import com.migrely.app.reminders.domain.repository.CancelDailyReminderRepository
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.GetReminderSettingsRepository
import com.migrely.app.reminders.domain.repository.ScheduleDailyReminderRepository
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

private val BERLIN = ZoneId.of("Europe/Berlin")

class RescheduleRemindersUseCaseTest {
    private val getReminderSettingsRepository: GetReminderSettingsRepository = mock()
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val scheduleDailyReminderRepository: ScheduleDailyReminderRepository = mock()
    private val cancelDailyReminderRepository: CancelDailyReminderRepository = mock()
    private lateinit var classUnderTest: RescheduleRemindersUseCase

    @Before
    fun setUp() {
        classUnderTest = RescheduleRemindersUseCase(
            getReminderSettingsRepository,
            getCurrentTimeRepository,
            scheduleDailyReminderRepository,
            cancelDailyReminderRepository,
            CoroutineContextProvider.Default
        )
        // The phone has just arrived in Berlin.
        given(getCurrentTimeRepository.currentTime()).willReturn(
            CurrentTimeDomainModel(
                LocalDateTime.parse("2026-10-24T18:00").atZone(BERLIN).toInstant(),
                BERLIN
            )
        )
    }

    private fun givenSettings(enabled: Boolean) {
        given(getReminderSettingsRepository.reminderSettings()).willReturn(
            ReminderSettingsResultDomainModel.Loaded(
                ReminderSettingsDomainModel(enabled, LocalTime.of(20, 0), true, null, null)
            )
        )
    }

    @Test
    fun `Given the reminder is on when the zone changes then planned for 8 PM local time`() {
        givenSettings(enabled = true)

        classUnderTest.executeInBackground(Unit)

        verify(scheduleDailyReminderRepository).scheduleDailyReminder(
            LocalDateTime.parse("2026-10-24T20:00").atZone(BERLIN).toInstant(),
            java.time.LocalDate.of(2026, 10, 24)
        )
    }

    @Test
    fun `Given the reminder is off when rescheduling then nothing stays planned`() {
        givenSettings(enabled = false)

        classUnderTest.executeInBackground(Unit)

        verify(cancelDailyReminderRepository).cancelDailyReminder()
    }
}
