package com.migrely.app.reminders.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.CurrentTimeDomainModel
import com.migrely.app.reminders.domain.model.DoctorVisitDomainModel
import com.migrely.app.reminders.domain.model.DoctorVisitsResultDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsResultDomainModel
import com.migrely.app.reminders.domain.model.RemindersResultDomainModel
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.GetDoctorVisitsRepository
import com.migrely.app.reminders.domain.repository.GetNotificationsAllowedRepository
import com.migrely.app.reminders.domain.repository.GetReminderSettingsRepository
import com.migrely.app.reminders.domain.repository.GetVisitChannelOffRepository
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

private val NOW: Instant = Instant.parse("2026-10-24T10:00:00Z")
private val TASHKENT: ZoneId = ZoneId.of("Asia/Tashkent")
private val VISIT_DAY: LocalDate = LocalDate.of(2026, 11, 12)

class GetRemindersUseCaseTest {
    private val getReminderSettingsRepository: GetReminderSettingsRepository = mock()
    private val getNotificationsAllowedRepository: GetNotificationsAllowedRepository = mock()
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val getDoctorVisitsRepository: GetDoctorVisitsRepository = mock {
        on { doctorVisits(any()) } doReturn DoctorVisitsResultDomainModel.Loaded(null, null)
    }
    private val getVisitChannelOffRepository: GetVisitChannelOffRepository = mock()
    private lateinit var classUnderTest: GetRemindersUseCase

    @Before
    fun setUp() {
        classUnderTest = GetRemindersUseCase(
            getReminderSettingsRepository,
            getNotificationsAllowedRepository,
            getCurrentTimeRepository,
            getDoctorVisitsRepository,
            getVisitChannelOffRepository,
            CoroutineContextProvider.Default
        )
        given(getCurrentTimeRepository.currentTime())
            .willReturn(CurrentTimeDomainModel(NOW, ZoneId.of("Asia/Tashkent")))
        given(getNotificationsAllowedRepository.notificationsAllowed()).willReturn(false)
    }

    private fun mayAsk(askedAt: Instant?): Boolean {
        given(getReminderSettingsRepository.reminderSettings()).willReturn(
            ReminderSettingsResultDomainModel.Loaded(
                ReminderSettingsDomainModel(false, LocalTime.of(20, 0), true, askedAt, null)
            )
        )
        val result = classUnderTest.executeInBackground(Unit) as RemindersResultDomainModel.Loaded
        return result.reminders.mayAskPermission
    }

    @Test
    fun `Given permission was never asked or asked 30 days ago when read then it may be asked`() {
        assertEquals(true, mayAsk(null))
        assertEquals(true, mayAsk(NOW.minus(Duration.ofDays(30))))
    }

    @Test
    fun `Given permission was asked 29 days ago when read then it may not be asked`() {
        assertEquals(false, mayAsk(NOW.minus(Duration.ofDays(29))))
    }

    @Test
    fun `Given the settings cannot be read when read then failed`() {
        given(getReminderSettingsRepository.reminderSettings())
            .willReturn(ReminderSettingsResultDomainModel.Failed)

        assertEquals(RemindersResultDomainModel.Failed, classUnderTest.executeInBackground(Unit))
    }

    @Test
    fun `Given a next visit and its channel off when read then the screen has both`() {
        given(getDoctorVisitsRepository.doctorVisits(LocalDate.of(2026, 10, 24))).willReturn(
            DoctorVisitsResultDomainModel.Loaded(
                DoctorVisitDomainModel("v", LocalDate.of(2026, 11, 20)),
                LocalDate.of(2026, 8, 20)
            )
        )
        given(getVisitChannelOffRepository.visitChannelOff()).willReturn(true)
        mayAsk(null)

        val result = classUnderTest.executeInBackground(Unit) as RemindersResultDomainModel.Loaded

        assertEquals(LocalDate.of(2026, 11, 20), result.reminders.nextVisit)
        assertEquals(true, result.reminders.visitChannelOff)
    }

    @Test
    fun `Given the visits cannot be read when read then the screen is unavailable`() {
        given(
            getDoctorVisitsRepository.doctorVisits(any())
        ).willReturn(DoctorVisitsResultDomainModel.Failed)
        given(getReminderSettingsRepository.reminderSettings()).willReturn(
            ReminderSettingsResultDomainModel.Loaded(
                ReminderSettingsDomainModel(false, LocalTime.of(20, 0), true, null, null)
            )
        )

        assertEquals(RemindersResultDomainModel.Failed, classUnderTest.executeInBackground(Unit))
    }

    private fun givenTheVisitOnTheTwelfth() {
        given(getReminderSettingsRepository.reminderSettings()).willReturn(
            ReminderSettingsResultDomainModel.Loaded(
                ReminderSettingsDomainModel(false, LocalTime.of(20, 0), true, null, null)
            )
        )
        // The first visit on or after either day read: the visit itself (EC-REM-15).
        given(getDoctorVisitsRepository.doctorVisits(any())).willReturn(
            DoctorVisitsResultDomainModel.Loaded(DoctorVisitDomainModel("v", VISIT_DAY), null)
        )
    }

    private fun readAt(instant: Instant, zone: ZoneId): RemindersResultDomainModel {
        given(getCurrentTimeRepository.currentTime())
            .willReturn(CurrentTimeDomainModel(instant, zone))
        return classUnderTest.executeInBackground(Unit)
    }

    /** Issue #187: the screen stays open past midnight; the same visit read again must not look the same. */
    @Test
    fun `Given the visit is tomorrow when the date changes to its day then the screen reads differently`() {
        givenTheVisitOnTheTwelfth()

        val before = readAt(Instant.parse("2026-11-11T18:59:00Z"), TASHKENT)
        val after = readAt(Instant.parse("2026-11-11T19:01:00Z"), TASHKENT)

        assertNotEquals(before, after)
    }

    /** Issue #187: a flight east moves the phone's date to the visit day at the same instant. */
    @Test
    fun `Given the visit is tomorrow when the zone moves the date to its day then the screen reads differently`() {
        givenTheVisitOnTheTwelfth()
        val instant = Instant.parse("2026-11-11T20:30:00Z")

        val before = readAt(instant, ZoneId.of("Europe/London"))
        val after = readAt(instant, TASHKENT)

        assertNotEquals(before, after)
    }

    /** D168 (13): Remove only for a visit after today, by the injected clock in the phone's zone, as the DAO deletes. */
    @Test
    fun `Given the visit is after today then Remove is offered, and on its day it is not`() {
        givenTheVisitOnTheTwelfth()

        val eve = readAt(Instant.parse("2026-11-11T18:59:00Z"), TASHKENT)
            as RemindersResultDomainModel.Loaded
        val day = readAt(Instant.parse("2026-11-11T19:00:00Z"), TASHKENT)
            as RemindersResultDomainModel.Loaded

        assertEquals(LocalDate.of(2026, 11, 11), eve.reminders.today)
        assertEquals(true, eve.reminders.visitRemovable)
        assertEquals(VISIT_DAY, day.reminders.today)
        assertEquals(VISIT_DAY, day.reminders.nextVisit)
        assertEquals(false, day.reminders.visitRemovable)
    }

    @Test
    fun `Given the zone moves the date to the visit day then Remove is no longer offered`() {
        givenTheVisitOnTheTwelfth()
        val instant = Instant.parse("2026-11-11T20:30:00Z")

        val london = readAt(
            instant,
            ZoneId.of("Europe/London")
        ) as RemindersResultDomainModel.Loaded
        val tashkent = readAt(instant, TASHKENT) as RemindersResultDomainModel.Loaded

        assertEquals(true, london.reminders.visitRemovable)
        assertEquals(false, tashkent.reminders.visitRemovable)
    }

    @Test
    fun `Given no visit when read then nothing to remove`() {
        mayAsk(null)

        val result = classUnderTest.executeInBackground(Unit) as RemindersResultDomainModel.Loaded

        assertEquals(LocalDate.of(2026, 10, 24), result.reminders.today)
        assertEquals(null, result.reminders.nextVisit)
        assertEquals(false, result.reminders.visitRemovable)
    }
}
