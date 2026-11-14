package com.migrely.app.reminders.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.CurrentTimeDomainModel
import com.migrely.app.reminders.domain.model.DoctorVisitDomainModel
import com.migrely.app.reminders.domain.model.DoctorVisitsResultDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsResultDomainModel
import com.migrely.app.reminders.domain.model.RescheduleRemindersRequestDomainModel
import com.migrely.app.reminders.domain.model.RescheduleRemindersRequestDomainModel.AppStart
import com.migrely.app.reminders.domain.model.RescheduleRemindersRequestDomainModel.ClockChange
import com.migrely.app.reminders.domain.repository.CancelDailyReminderRepository
import com.migrely.app.reminders.domain.repository.CancelVisitReminderRepository
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.GetDoctorVisitsRepository
import com.migrely.app.reminders.domain.repository.GetReminderSettingsRepository
import com.migrely.app.reminders.domain.repository.PlanDailyReminderIfNoneRepository
import com.migrely.app.reminders.domain.repository.ScheduleDailyReminderRepository
import com.migrely.app.reminders.domain.repository.ScheduleVisitReminderRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

private val BERLIN = ZoneId.of("Europe/Berlin")
private val TODAY = LocalDate.of(2026, 10, 24)

class RescheduleRemindersUseCaseTest {
    private val getReminderSettingsRepository: GetReminderSettingsRepository = mock()
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val scheduleDailyReminderRepository: ScheduleDailyReminderRepository = mock()
    private val planDailyReminderIfNoneRepository: PlanDailyReminderIfNoneRepository = mock()
    private val cancelDailyReminderRepository: CancelDailyReminderRepository = mock()
    private val getDoctorVisitsRepository: GetDoctorVisitsRepository = mock {
        on { doctorVisits(any()) } doReturn DoctorVisitsResultDomainModel.Loaded(null, null)
    }
    private val scheduleVisitReminderRepository: ScheduleVisitReminderRepository = mock()
    private val cancelVisitReminderRepository: CancelVisitReminderRepository = mock()
    private lateinit var classUnderTest: RescheduleRemindersUseCase

    @Before
    fun setUp() {
        classUnderTest = RescheduleRemindersUseCase(
            getReminderSettingsRepository,
            getCurrentTimeRepository,
            scheduleDailyReminderRepository,
            planDailyReminderIfNoneRepository,
            cancelDailyReminderRepository,
            getDoctorVisitsRepository,
            scheduleVisitReminderRepository,
            cancelVisitReminderRepository,
            CoroutineContextProvider.Default
        )
        // The phone has just arrived in Berlin.
        givenNow("2026-10-24T18:00")
    }

    private fun at(local: String) = LocalDateTime.parse(local).atZone(BERLIN).toInstant()

    private fun givenNow(local: String) {
        given(getCurrentTimeRepository.currentTime())
            .willReturn(CurrentTimeDomainModel(at(local), BERLIN))
    }

    private fun givenSettings(
        enabled: Boolean,
        lastShownOn: LocalDate? = null,
        remindedFor: LocalDate? = null,
        postponedTo: LocalDate? = null
    ) {
        given(getReminderSettingsRepository.reminderSettings()).willReturn(
            ReminderSettingsResultDomainModel.Loaded(
                ReminderSettingsDomainModel(
                    enabled,
                    LocalTime.of(20, 0),
                    true,
                    null,
                    lastShownOn,
                    visitRemindedFor = remindedFor,
                    visitPostponedTo = postponedTo
                )
            )
        )
    }

    private fun givenNextVisit(date: LocalDate?) {
        given(getDoctorVisitsRepository.doctorVisits(LocalDate.of(2026, 10, 24))).willReturn(
            DoctorVisitsResultDomainModel.Loaded(
                date?.let {
                    DoctorVisitDomainModel("v", it)
                },
                null
            )
        )
    }

    @Test
    fun `Given the reminder is on when the zone changes then planned for 8 PM local time`() {
        givenSettings(enabled = true)

        classUnderTest.executeInBackground(ClockChange)

        verify(scheduleDailyReminderRepository).scheduleDailyReminder(
            LocalDateTime.parse("2026-10-24T20:00").atZone(BERLIN).toInstant(),
            LocalDate.of(2026, 10, 24)
        )
    }

    @Test
    fun `Given the reminder is off when rescheduling then nothing stays planned`() {
        givenSettings(enabled = false)

        classUnderTest.executeInBackground(ClockChange)

        verify(cancelDailyReminderRepository).cancelDailyReminder()
    }

    @Test
    fun `Given a next visit when the zone changes then its reminder is at 10 AM local time`() {
        givenSettings(enabled = false)
        givenNextVisit(LocalDate.of(2026, 11, 20))

        classUnderTest.executeInBackground(ClockChange)

        verify(scheduleVisitReminderRepository).scheduleVisitReminder(
            LocalDateTime.parse("2026-11-18T10:00").atZone(BERLIN).toInstant(),
            LocalDate.of(2026, 11, 20)
        )
    }

    @Test
    fun `Given an attack postponed it when the app starts then it keeps waiting for that day`() {
        givenSettings(enabled = false, postponedTo = LocalDate.of(2026, 11, 19))
        givenNextVisit(LocalDate.of(2026, 11, 20))

        classUnderTest.executeInBackground(AppStart)

        verify(scheduleVisitReminderRepository).scheduleVisitReminder(
            LocalDateTime.parse("2026-11-19T10:00").atZone(BERLIN).toInstant(),
            LocalDate.of(2026, 11, 20)
        )
    }

    @Test
    fun `Given no next visit or one reminded of when rescheduling then its run is cancelled`() {
        givenSettings(enabled = false)
        givenNextVisit(null)
        classUnderTest.executeInBackground(ClockChange)

        givenSettings(enabled = false, remindedFor = LocalDate.of(2026, 11, 20))
        givenNextVisit(LocalDate.of(2026, 11, 20))
        classUnderTest.executeInBackground(ClockChange)

        verify(cancelVisitReminderRepository, times(2)).cancelVisitReminder()
        verify(scheduleVisitReminderRepository, never()).scheduleVisitReminder(any(), any())
    }

    @Test
    fun `Given the time passed, today not shown when the app starts then now, kept if planned`() {
        givenSettings(enabled = true, lastShownOn = TODAY.minusDays(1))
        givenNow("2026-10-24T20:01")

        classUnderTest.executeInBackground(AppStart)

        verify(planDailyReminderIfNoneRepository)
            .planDailyReminderIfNone(at("2026-10-24T20:01"), TODAY)
        verify(scheduleDailyReminderRepository, never()).scheduleDailyReminder(any(), any())
    }

    @Test
    fun `Given the time is ahead when the app starts then today's time, kept if planned`() {
        givenSettings(enabled = true)

        classUnderTest.executeInBackground(AppStart)

        verify(planDailyReminderIfNoneRepository)
            .planDailyReminderIfNone(at("2026-10-24T20:00"), TODAY)
        verify(scheduleDailyReminderRepository, never()).scheduleDailyReminder(any(), any())
    }

    @Test
    fun `Given the time passed, today not shown when the clock changes then now, replacing`() {
        givenSettings(enabled = true)
        givenNow("2026-10-24T20:30")

        classUnderTest.executeInBackground(ClockChange)

        verify(scheduleDailyReminderRepository).scheduleDailyReminder(at("2026-10-24T20:30"), TODAY)
        verify(planDailyReminderIfNoneRepository, never()).planDailyReminderIfNone(any(), any())
    }

    @Test
    fun `Given the time passed and today shown when either plans then tomorrow`() {
        givenSettings(enabled = true, lastShownOn = TODAY)
        givenNow("2026-10-24T20:30")

        classUnderTest.executeInBackground(AppStart)
        classUnderTest.executeInBackground(ClockChange)

        verify(planDailyReminderIfNoneRepository)
            .planDailyReminderIfNone(at("2026-10-25T20:00"), TODAY.plusDays(1))
        verify(scheduleDailyReminderRepository)
            .scheduleDailyReminder(at("2026-10-25T20:00"), TODAY.plusDays(1))
    }

    @Test
    fun `Given the reminder is off when either plans then cancelled, nothing planned`() {
        givenSettings(enabled = false)

        classUnderTest.executeInBackground(AppStart)
        classUnderTest.executeInBackground(ClockChange)

        verify(cancelDailyReminderRepository, times(2)).cancelDailyReminder()
        verify(planDailyReminderIfNoneRepository, never()).planDailyReminderIfNone(any(), any())
        verify(scheduleDailyReminderRepository, never()).scheduleDailyReminder(any(), any())
    }

    @Test
    fun `Given a next visit when either plans then its reminder is planned the same way`() {
        givenSettings(enabled = true)
        givenNextVisit(LocalDate.of(2026, 11, 20))

        RescheduleRemindersRequestDomainModel.entries.forEach(classUnderTest::executeInBackground)

        verify(scheduleVisitReminderRepository, times(2)).scheduleVisitReminder(
            LocalDateTime.parse("2026-11-18T10:00").atZone(BERLIN).toInstant(),
            LocalDate.of(2026, 11, 20)
        )
    }
}
