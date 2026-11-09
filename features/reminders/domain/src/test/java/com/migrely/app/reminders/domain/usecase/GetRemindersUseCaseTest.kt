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
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

private val NOW: Instant = Instant.parse("2026-10-24T10:00:00Z")

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
}
