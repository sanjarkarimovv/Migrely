package com.migrely.app.reminders.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.CurrentTimeDomainModel
import com.migrely.app.reminders.domain.model.DailyReminderRunDomainModel
import com.migrely.app.reminders.domain.model.ReminderAttackDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsResultDomainModel
import com.migrely.app.reminders.domain.model.TodayEntriesResultDomainModel
import com.migrely.app.reminders.domain.repository.CancelDailyReminderRepository
import com.migrely.app.reminders.domain.repository.GetAppOnScreenRepository
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
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

private val TASHKENT = ZoneId.of("Asia/Tashkent")
private val BERLIN = ZoneId.of("Europe/Berlin")
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
    private val getAppOnScreenRepository: GetAppOnScreenRepository = mock()
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
            getAppOnScreenRepository,
            CoroutineContextProvider.Default
        )
        given(getCurrentTimeRepository.currentTime())
            .willReturn(CurrentTimeDomainModel(at("2026-10-24T20:05"), TASHKENT))
        given(getNotificationsAllowedRepository.notificationsAllowed()).willReturn(true)
        givenSettings()
        givenEntries()
    }

    private fun givenNow(local: String, zone: ZoneId = TASHKENT) {
        given(getCurrentTimeRepository.currentTime())
            .willReturn(CurrentTimeDomainModel(at(local, zone), zone))
    }

    private fun at(local: String, zone: ZoneId = TASHKENT): Instant =
        LocalDateTime.parse(local).atZone(zone).toInstant()

    private fun givenSettings(
        enabled: Boolean = true,
        lastShownOn: LocalDate? = null,
        time: LocalTime = LocalTime.of(20, 0)
    ) {
        given(getReminderSettingsRepository.reminderSettings()).willReturn(
            ReminderSettingsResultDomainModel.Loaded(
                ReminderSettingsDomainModel(enabled, time, true, null, lastShownOn)
            )
        )
    }

    private fun assertShownAt(local: String, quiet: Boolean, date: LocalDate? = TODAY) {
        givenNow(local)

        assertEquals(DailyReminderRunDomainModel.Shown, run(date))

        verify(showDailyReminderRepository).showDailyReminder(TODAY, quiet)
        verify(setDailyShownRepository).setDailyShown(TODAY)
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

        verify(showDailyReminderRepository).showDailyReminder(TODAY, quiet = false)
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

        verify(showDailyReminderRepository, never()).showDailyReminder(any(), any())
    }

    @Test
    fun `Given it showed today or notifications are off then skipped, tomorrow still planned`() {
        givenSettings(lastShownOn = TODAY)
        assertEquals(DailyReminderRunDomainModel.Skipped, run())

        givenSettings()
        given(getNotificationsAllowedRepository.notificationsAllowed()).willReturn(false)
        assertEquals(DailyReminderRunDomainModel.Skipped, run())

        verify(showDailyReminderRepository, never()).showDailyReminder(any(), any())
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
        verify(showDailyReminderRepository, never()).showDailyReminder(any(), any())
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

        verify(showDailyReminderRepository, never()).showDailyReminder(any(), any())
        verify(scheduleDailyReminderRepository).scheduleDailyReminder(at("2026-10-24T23:45"), TODAY)
    }

    @Test
    fun `Given the settings cannot be read when run then failed, so the run is tried again`() {
        given(getReminderSettingsRepository.reminderSettings())
            .willReturn(ReminderSettingsResultDomainModel.Failed)

        assertEquals(DailyReminderRunDomainModel.Failed, run())
    }

    @Test
    fun `Given Migrely on screen when run then not shown, not marked, the date in 30 minutes`() {
        given(getAppOnScreenRepository.appOnScreen()).willReturn(true)

        assertEquals(DailyReminderRunDomainModel.Skipped, run())

        verify(showDailyReminderRepository, never()).showDailyReminder(any(), any())
        verify(setDailyShownRepository, never()).setDailyShown(any())
        inOrder(scheduleDailyReminderRepository) {
            verify(scheduleDailyReminderRepository)
                .scheduleDailyReminder(at("2026-10-25T20:00"), TODAY.plusDays(1))
            verify(
                scheduleDailyReminderRepository
            ).scheduleDailyReminder(at("2026-10-24T20:35"), TODAY)
        }
    }

    @Test
    fun `Given a postponed run off screen, today not answered when run then shown and marked`() {
        givenNow("2026-10-24T20:35")

        assertEquals(DailyReminderRunDomainModel.Shown, run())

        verify(showDailyReminderRepository).showDailyReminder(TODAY, quiet = false)
        verify(setDailyShownRepository).setDailyShown(TODAY)
        verify(scheduleDailyReminderRepository)
            .scheduleDailyReminder(at("2026-10-25T20:00"), TODAY.plusDays(1))
    }

    @Test
    fun `Given a postponed run and today answered when run then not shown`() {
        givenNow("2026-10-24T20:35")
        givenEntries(painFree = true)

        assertEquals(DailyReminderRunDomainModel.Skipped, run())

        verify(showDailyReminderRepository, never()).showDailyReminder(any(), any())
        verify(scheduleDailyReminderRepository, never()).scheduleDailyReminder(any(), eq(TODAY))
    }

    @Test
    fun `Given a postponed run and Migrely still on screen when run then postponed again`() {
        givenNow("2026-10-24T20:35")
        given(getAppOnScreenRepository.appOnScreen()).willReturn(true)

        assertEquals(DailyReminderRunDomainModel.Skipped, run())

        verify(showDailyReminderRepository, never()).showDailyReminder(any(), any())
        verify(setDailyShownRepository, never()).setDailyShown(any())
        verify(scheduleDailyReminderRepository).scheduleDailyReminder(at("2026-10-24T21:05"), TODAY)
    }

    @Test
    fun `Given a postponed run after midnight when run then not shown, next day planned`() {
        // Postponed at 23:50 on 23 October, it comes at 00:20 on 24 October.
        givenNow("2026-10-24T00:20")
        given(getAppOnScreenRepository.appOnScreen()).willReturn(true)

        assertEquals(DailyReminderRunDomainModel.Skipped, run(TODAY.minusDays(1)))

        verify(showDailyReminderRepository, never()).showDailyReminder(any(), any())
        verify(scheduleDailyReminderRepository).scheduleDailyReminder(at("2026-10-24T20:00"), TODAY)
        verify(scheduleDailyReminderRepository, never())
            .scheduleDailyReminder(any(), eq(TODAY.minusDays(1)))
    }

    @Test
    fun `Given a postponed run after today showed when run then not shown, not postponed`() {
        givenNow("2026-10-24T20:35")
        givenSettings(lastShownOn = TODAY)
        given(getAppOnScreenRepository.appOnScreen()).willReturn(true)

        assertEquals(DailyReminderRunDomainModel.Skipped, run())

        verify(showDailyReminderRepository, never()).showDailyReminder(any(), any())
        verify(scheduleDailyReminderRepository, never()).scheduleDailyReminder(any(), eq(TODAY))
    }

    @Test
    fun `Given the run comes 7 minutes after the time when run then shown with its sound`() {
        assertShownAt("2026-10-24T20:07", quiet = false)
    }

    @Test
    fun `Given the run comes exactly 60 minutes late when run then shown with its sound`() {
        assertShownAt("2026-10-24T21:00", quiet = false)
    }

    @Test
    fun `Given the run comes 61 minutes after the time when run then shown quietly`() {
        assertShownAt("2026-10-24T21:01", quiet = true)
    }

    @Test
    fun `Given Doze held the run until 11-10 PM when run then shown quietly and today marked`() {
        assertShownAt("2026-10-24T23:10", quiet = true)
    }

    @Test
    fun `Given the first postponed run at 8-31 PM when run then shown with its sound`() {
        assertShownAt("2026-10-24T20:31", quiet = false)
    }

    @Test
    fun `Given a postponed run at 9-40 PM when run then shown quietly`() {
        assertShownAt("2026-10-24T21:40", quiet = true)
    }

    @Test
    fun `Given a catch-up after a reboot or clock change at 11 PM when run then shown quietly`() {
        assertShownAt("2026-10-24T23:00", quiet = true)
    }

    @Test
    fun `Given an old run without a date at 11 PM when run then shown quietly`() {
        assertShownAt("2026-10-24T23:00", quiet = true, date = null)
    }

    @Test
    fun `Given a run that came early when run then shown with its sound`() {
        // The clock was set back, or JobScheduler ran it a little early.
        assertShownAt("2026-10-24T19:50", quiet = false)
    }

    @Test
    fun `Given the phone moved to Berlin, 3 hours after 8 PM there, when run then shown quietly`() {
        // 23:00 in Berlin is 02:00 on 25 October in Tashkent, where 8 PM was 6 hours ago.
        givenNow("2026-10-24T23:00", BERLIN)

        assertEquals(DailyReminderRunDomainModel.Shown, run())

        verify(showDailyReminderRepository).showDailyReminder(TODAY, quiet = true)
    }

    @Test
    fun `Given the phone moved to Berlin, 30 minutes after 8 PM there, when run then with sound`() {
        // 20:30 in Berlin is 23:30 in Tashkent, where 8 PM was 3.5 hours ago.
        givenNow("2026-10-24T20:30", BERLIN)

        assertEquals(DailyReminderRunDomainModel.Shown, run())

        verify(showDailyReminderRepository).showDailyReminder(TODAY, quiet = false)
    }

    @Test
    fun `Given the time was changed to 10 PM when run at 10-05 PM then shown with its sound`() {
        givenSettings(time = LocalTime.of(22, 0))

        assertShownAt("2026-10-24T22:05", quiet = false)
    }

    @Test
    fun `Given a time of 11 PM when run at 11-59 PM then shown with its sound`() {
        givenSettings(time = LocalTime.of(23, 0))

        assertShownAt("2026-10-24T23:59", quiet = false)
    }

    @Test
    fun `Given a time of 11-15 PM held past midnight when run then nothing shown, quiet or not`() {
        givenSettings(time = LocalTime.of(23, 15))
        givenNow("2026-10-25T00:20")

        assertEquals(DailyReminderRunDomainModel.Skipped, run())

        verify(showDailyReminderRepository, never()).showDailyReminder(any(), any())
        verify(setDailyShownRepository, never()).setDailyShown(any())
    }

    @Test
    fun `Given Migrely on screen at 11-10 PM when run then postponed, not shown`() {
        givenNow("2026-10-24T23:10")
        given(getAppOnScreenRepository.appOnScreen()).willReturn(true)

        assertEquals(DailyReminderRunDomainModel.Skipped, run())

        verify(showDailyReminderRepository, never()).showDailyReminder(any(), any())
        verify(setDailyShownRepository, never()).setDailyShown(any())
        verify(scheduleDailyReminderRepository).scheduleDailyReminder(at("2026-10-24T23:40"), TODAY)
    }

    @Test
    fun `Given a late run and today answered, shown or notifications off then nothing shown`() {
        givenNow("2026-10-24T23:10")
        givenEntries(painFree = true)
        assertEquals(DailyReminderRunDomainModel.Skipped, run())

        givenEntries()
        givenSettings(lastShownOn = TODAY)
        assertEquals(DailyReminderRunDomainModel.Skipped, run())

        givenSettings()
        given(getNotificationsAllowedRepository.notificationsAllowed()).willReturn(false)
        assertEquals(DailyReminderRunDomainModel.Skipped, run())

        verify(showDailyReminderRepository, never()).showDailyReminder(any(), any())
        verify(setDailyShownRepository, never()).setDailyShown(any())
    }
}
