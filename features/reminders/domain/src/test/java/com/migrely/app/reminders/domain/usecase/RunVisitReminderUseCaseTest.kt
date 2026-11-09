package com.migrely.app.reminders.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.AnythingLoggedDomainModel
import com.migrely.app.reminders.domain.model.AttackInProgressDomainModel
import com.migrely.app.reminders.domain.model.CurrentTimeDomainModel
import com.migrely.app.reminders.domain.model.DoctorVisitDomainModel
import com.migrely.app.reminders.domain.model.DoctorVisitsResultDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsResultDomainModel
import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import com.migrely.app.reminders.domain.model.VisitReminderRunDomainModel
import com.migrely.app.reminders.domain.repository.GetAnythingLoggedRepository
import com.migrely.app.reminders.domain.repository.GetAttackInProgressRepository
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.GetDoctorVisitsRepository
import com.migrely.app.reminders.domain.repository.GetReminderSettingsRepository
import com.migrely.app.reminders.domain.repository.ScheduleVisitReminderRepository
import com.migrely.app.reminders.domain.repository.SetVisitPostponedRepository
import com.migrely.app.reminders.domain.repository.SetVisitRemindedRepository
import com.migrely.app.reminders.domain.repository.ShowVisitReminderRepository
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

private val TASHKENT = ZoneId.of("Asia/Tashkent")
private val NOVEMBER_20: LocalDate = LocalDate.of(2026, 11, 20)
private val AUGUST_20: LocalDate = LocalDate.of(2026, 8, 20)

private fun at(text: String) = LocalDateTime.parse(text).atZone(TASHKENT).toInstant()

/** The visit reminder's run, design D3 steps 1 to 6 (BRD D168, EC-REM-09, EC-REM-12, EC-REM-13, EC-REM-14). */
class RunVisitReminderUseCaseTest {
    private val getReminderSettingsRepository: GetReminderSettingsRepository = mock()
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val getDoctorVisitsRepository: GetDoctorVisitsRepository = mock()
    private val getAttackInProgressRepository: GetAttackInProgressRepository = mock {
        on { attackInProgress() } doReturn AttackInProgressDomainModel.NotInProgress
    }
    private val getAnythingLoggedRepository: GetAnythingLoggedRepository = mock {
        on { anythingLogged(any(), any(), any()) } doReturn AnythingLoggedDomainModel.Logged
    }
    private val setVisitPostponedRepository: SetVisitPostponedRepository = mock {
        on { setVisitPostponed(any()) } doReturn ReminderWriteResultDomainModel.Done
    }
    private val scheduleVisitReminderRepository: ScheduleVisitReminderRepository = mock()
    private val showVisitReminderRepository: ShowVisitReminderRepository = mock()
    private val setVisitRemindedRepository: SetVisitRemindedRepository = mock()
    private lateinit var classUnderTest: RunVisitReminderUseCase

    @Before
    fun setUp() {
        classUnderTest = RunVisitReminderUseCase(
            getReminderSettingsRepository,
            getCurrentTimeRepository,
            getDoctorVisitsRepository,
            getAttackInProgressRepository,
            getAnythingLoggedRepository,
            setVisitPostponedRepository,
            scheduleVisitReminderRepository,
            showVisitReminderRepository,
            setVisitRemindedRepository,
            CoroutineContextProvider.Default
        )
        givenNow("2026-11-18T10:00")
        givenRemindedFor(null)
        givenNextVisit(NOVEMBER_20)
    }

    private fun givenNow(text: String) {
        given(
            getCurrentTimeRepository.currentTime()
        ).willReturn(CurrentTimeDomainModel(at(text), TASHKENT))
    }

    private fun givenRemindedFor(date: LocalDate?) {
        given(getReminderSettingsRepository.reminderSettings()).willReturn(
            ReminderSettingsResultDomainModel.Loaded(
                ReminderSettingsDomainModel(
                    false,
                    LocalTime.of(20, 0),
                    true,
                    null,
                    null,
                    visitRemindedFor = date
                )
            )
        )
    }

    private fun givenNextVisit(date: LocalDate?, last: LocalDate? = AUGUST_20) {
        given(getDoctorVisitsRepository.doctorVisits(any())).willReturn(
            DoctorVisitsResultDomainModel.Loaded(
                date?.let {
                    DoctorVisitDomainModel("v", it)
                },
                last
            )
        )
    }

    private fun assertNothingPosted() {
        verify(showVisitReminderRepository, never()).showVisitReminder(any(), any())
        verify(setVisitRemindedRepository, never()).setVisitReminded(any())
    }

    @Test
    fun `Given the next visit when it runs 2 days before then it posts once and remembers`() {
        val result = classUnderTest.executeInBackground(NOVEMBER_20)

        assertEquals(VisitReminderRunDomainModel.Posted, result)
        verify(showVisitReminderRepository).showVisitReminder(NOVEMBER_20, at("2026-11-21T00:00"))
        verify(setVisitRemindedRepository).setVisitReminded(NOVEMBER_20)
    }

    @Test
    fun `Given a last visit when it checks for entries then it asks about the period since`() {
        classUnderTest.executeInBackground(NOVEMBER_20)

        verify(
            getAnythingLoggedRepository
        ).anythingLogged(AUGUST_20, LocalDate.of(2026, 11, 18), TASHKENT)
    }

    @Test
    fun `Given work for a date no longer the next visit when it runs then nothing posts (1)`() {
        givenNextVisit(LocalDate.of(2026, 11, 27))

        assertEquals(
            VisitReminderRunDomainModel.Skipped,
            classUnderTest.executeInBackground(NOVEMBER_20)
        )
        assertNothingPosted()
    }

    @Test
    fun `Given that date was reminded of already when it runs again then nothing posts (2)`() {
        givenRemindedFor(NOVEMBER_20)

        assertEquals(
            VisitReminderRunDomainModel.Skipped,
            classUnderTest.executeInBackground(NOVEMBER_20)
        )
        assertNothingPosted()
    }

    @Test
    fun `Given the phone was off until 8 AM on the visit day when it runs then it still posts`() {
        givenNow("2026-11-20T08:00")

        assertEquals(
            VisitReminderRunDomainModel.Posted,
            classUnderTest.executeInBackground(NOVEMBER_20)
        )
    }

    @Test
    fun `Given the visit day has ended when it runs then nothing posts (3)`() {
        givenNow("2026-11-21T00:00")
        // The day after, the visit is the last one; the clock went forward with the work still planned.
        givenNextVisit(null, last = NOVEMBER_20)

        assertEquals(
            VisitReminderRunDomainModel.Skipped,
            classUnderTest.executeInBackground(NOVEMBER_20)
        )
        assertNothingPosted()
    }

    @Test
    fun `Given an attack at 10 AM when it runs then it waits for 10 AM the next day (4)`() {
        given(
            getAttackInProgressRepository.attackInProgress()
        ).willReturn(AttackInProgressDomainModel.InProgress)

        assertEquals(
            VisitReminderRunDomainModel.Postponed,
            classUnderTest.executeInBackground(NOVEMBER_20)
        )
        verify(setVisitPostponedRepository).setVisitPostponed(LocalDate.of(2026, 11, 19))
        verify(
            scheduleVisitReminderRepository
        ).scheduleVisitReminder(at("2026-11-19T10:00"), NOVEMBER_20)
        assertNothingPosted()
    }

    @Test
    fun `Given an attack on the visit day when it runs then nothing posts or is planned (4)`() {
        givenNow("2026-11-20T10:00")
        given(
            getAttackInProgressRepository.attackInProgress()
        ).willReturn(AttackInProgressDomainModel.InProgress)

        assertEquals(
            VisitReminderRunDomainModel.Skipped,
            classUnderTest.executeInBackground(NOVEMBER_20)
        )
        verify(scheduleVisitReminderRepository, never()).scheduleVisitReminder(any(), any())
        assertNothingPosted()
    }

    @Test
    fun `Given nothing logged in the period when it runs then nothing posts, and it is done (5)`() {
        given(getAnythingLoggedRepository.anythingLogged(any(), any(), any()))
            .willReturn(AnythingLoggedDomainModel.NothingLogged)

        assertEquals(
            VisitReminderRunDomainModel.Skipped,
            classUnderTest.executeInBackground(NOVEMBER_20)
        )
        verify(showVisitReminderRepository, never()).showVisitReminder(any(), any())
        // Handled for that date, so a later start or clock change plans nothing that could post at night; the
        // card covers an entry made later.
        verify(setVisitRemindedRepository).setVisitReminded(NOVEMBER_20)
    }

    @Test
    fun `Given a read fails when it runs then it is tried again`() {
        given(getAnythingLoggedRepository.anythingLogged(any(), any(), any()))
            .willReturn(AnythingLoggedDomainModel.Failed)
        assertEquals(
            VisitReminderRunDomainModel.Failed,
            classUnderTest.executeInBackground(NOVEMBER_20)
        )

        given(
            getAttackInProgressRepository.attackInProgress()
        ).willReturn(AttackInProgressDomainModel.Failed)
        assertEquals(
            VisitReminderRunDomainModel.Failed,
            classUnderTest.executeInBackground(NOVEMBER_20)
        )

        given(
            getDoctorVisitsRepository.doctorVisits(any())
        ).willReturn(DoctorVisitsResultDomainModel.Failed)
        assertEquals(
            VisitReminderRunDomainModel.Failed,
            classUnderTest.executeInBackground(NOVEMBER_20)
        )

        given(
            getReminderSettingsRepository.reminderSettings()
        ).willReturn(ReminderSettingsResultDomainModel.Failed)
        assertEquals(
            VisitReminderRunDomainModel.Failed,
            classUnderTest.executeInBackground(NOVEMBER_20)
        )
        assertNothingPosted()
    }
}
