package com.migrely.app.reminders.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.CurrentTimeDomainModel
import com.migrely.app.reminders.domain.model.DailyReminderRunDomainModel
import com.migrely.app.reminders.domain.model.ReminderAttackDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsResultDomainModel
import com.migrely.app.reminders.domain.model.TodayEntriesResultDomainModel
import com.migrely.app.reminders.domain.repository.CancelDailyReminderRepository
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.GetNotificationsAllowedRepository
import com.migrely.app.reminders.domain.repository.GetReminderSettingsRepository
import com.migrely.app.reminders.domain.repository.GetTodayEntriesRepository
import com.migrely.app.reminders.domain.repository.ScheduleDailyReminderRepository
import com.migrely.app.reminders.domain.repository.SetDailyShownRepository
import com.migrely.app.reminders.domain.repository.ShowDailyReminderRepository
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
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
private val TODAY = LocalDate.of(2026, 10, 24)

class RunDailyReminderUseCaseTest {
    private val getReminderSettingsRepository: GetReminderSettingsRepository = mock()
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val getNotificationsAllowedRepository: GetNotificationsAllowedRepository = mock()
    private val getTodayEntriesRepository: GetTodayEntriesRepository = mock()
    private val scheduleDailyReminderRepository: ScheduleDailyReminderRepository = mock()
    private val cancelDailyReminderRepository: CancelDailyReminderRepository = mock()
    private val showDailyReminderRepository: ShowDailyReminderRepository = mock()
    private val setDailyShownRepository: SetDailyShownRepository = mock()
    private lateinit var classUnderTest: RunDailyReminderUseCase

    @Before
    fun setUp() {
        classUnderTest = RunDailyReminderUseCase(
            getReminderSettingsRepository,
            getCurrentTimeRepository,
            getNotificationsAllowedRepository,
            getTodayEntriesRepository,
            scheduleDailyReminderRepository,
            cancelDailyReminderRepository,
            showDailyReminderRepository,
            setDailyShownRepository,
            CoroutineContextProvider.Default
        )
        given(getCurrentTimeRepository.currentTime())
            .willReturn(CurrentTimeDomainModel(at("2026-10-24T20:05"), TASHKENT))
        given(getNotificationsAllowedRepository.notificationsAllowed()).willReturn(true)
        givenSettings()
        givenEntries()
    }

    private fun at(local: String): Instant = LocalDateTime.parse(local).atZone(TASHKENT).toInstant()

    private fun givenSettings(enabled: Boolean = true, lastShownOn: LocalDate? = null) {
        given(getReminderSettingsRepository.reminderSettings()).willReturn(
            ReminderSettingsResultDomainModel.Loaded(
                ReminderSettingsDomainModel(enabled, LocalTime.of(20, 0), true, null, lastShownOn)
            )
        )
    }

    private fun givenEntries(
        attacks: List<ReminderAttackDomainModel> = emptyList(),
        painFree: Boolean = false
    ) {
        given(getTodayEntriesRepository.todayEntries(any(), any(), any()))
            .willReturn(TodayEntriesResultDomainModel.Loaded(attacks, painFree))
    }

    private fun run(date: LocalDate? = TODAY) = classUnderTest.executeInBackground(date)

    @Test
    fun `Given nothing logged today when run then shown, noted, and tomorrow planned`() {
        assertEquals(DailyReminderRunDomainModel.Shown, run())

        verify(showDailyReminderRepository).showDailyReminder(TODAY)
        verify(setDailyShownRepository).setDailyShown(TODAY)
        verify(
            scheduleDailyReminderRepository
        ).scheduleDailyReminder(at("2026-10-25T20:00"), TODAY.plusDays(1))
    }

    @Test
    fun `Given an attack only yesterday when run then shown`() {
        givenEntries(
            attacks = listOf(
                ReminderAttackDomainModel(at("2026-10-23T09:00"), TASHKENT, at("2026-10-23T12:00"))
            )
        )

        assertEquals(DailyReminderRunDomainModel.Shown, run())
    }

    @Test
    fun `Given an attack touching today or a pain-free mark when run then skipped`() {
        // From 23:00 yesterday to 01:00 today.
        givenEntries(
            attacks = listOf(
                ReminderAttackDomainModel(at("2026-10-23T23:00"), TASHKENT, at("2026-10-24T01:00"))
            )
        )
        assertEquals(DailyReminderRunDomainModel.Skipped, run())

        givenEntries(painFree = true)
        assertEquals(DailyReminderRunDomainModel.Skipped, run())

        verify(showDailyReminderRepository, never()).showDailyReminder(any())
    }

    @Test
    fun `Given it showed today or notifications are off then skipped, tomorrow still planned`() {
        givenSettings(lastShownOn = TODAY)
        assertEquals(DailyReminderRunDomainModel.Skipped, run())

        givenSettings()
        given(getNotificationsAllowedRepository.notificationsAllowed()).willReturn(false)
        assertEquals(DailyReminderRunDomainModel.Skipped, run())

        verify(showDailyReminderRepository, never()).showDailyReminder(any())
        verify(scheduleDailyReminderRepository, org.mockito.kotlin.times(2))
            .scheduleDailyReminder(at("2026-10-25T20:00"), TODAY.plusDays(1))
    }

    @Test
    fun `Given the reminder is off when run then cancelled and nothing planned`() {
        givenSettings(enabled = false)

        assertEquals(DailyReminderRunDomainModel.Skipped, run())

        verify(cancelDailyReminderRepository).cancelDailyReminder()
        verify(scheduleDailyReminderRepository, never()).scheduleDailyReminder(any(), any())
    }

    @Test
    fun `Given today's entries cannot be read when run then failed and nothing shown`() {
        given(getTodayEntriesRepository.todayEntries(any(), any(), any()))
            .willReturn(TodayEntriesResultDomainModel.Failed)

        assertEquals(DailyReminderRunDomainModel.Failed, run())
        verify(showDailyReminderRepository, never()).showDailyReminder(any())
    }

    @Test
    fun `Given yesterday's run arrives after midnight then nothing is asked, tonight planned`() {
        // The 23:45 run for 23 October arrives at 00:10 on 24 October.
        given(getReminderSettingsRepository.reminderSettings()).willReturn(
            ReminderSettingsResultDomainModel.Loaded(
                ReminderSettingsDomainModel(true, LocalTime.of(23, 45), true, null, null)
            )
        )
        given(getCurrentTimeRepository.currentTime())
            .willReturn(CurrentTimeDomainModel(at("2026-10-24T00:10"), TASHKENT))

        assertEquals(DailyReminderRunDomainModel.Skipped, run(TODAY.minusDays(1)))

        verify(showDailyReminderRepository, never()).showDailyReminder(any())
        verify(scheduleDailyReminderRepository).scheduleDailyReminder(at("2026-10-24T23:45"), TODAY)
    }

    @Test
    fun `Given the settings cannot be read when run then failed, so the run is tried again`() {
        given(getReminderSettingsRepository.reminderSettings())
            .willReturn(ReminderSettingsResultDomainModel.Failed)

        assertEquals(DailyReminderRunDomainModel.Failed, run())
    }
}
