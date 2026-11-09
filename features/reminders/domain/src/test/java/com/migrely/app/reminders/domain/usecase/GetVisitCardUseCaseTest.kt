package com.migrely.app.reminders.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.AnythingLoggedDomainModel
import com.migrely.app.reminders.domain.model.CurrentTimeDomainModel
import com.migrely.app.reminders.domain.model.DoctorVisitDomainModel
import com.migrely.app.reminders.domain.model.DoctorVisitsResultDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsResultDomainModel
import com.migrely.app.reminders.domain.model.VisitCardDomainModel
import com.migrely.app.reminders.domain.repository.GetAnythingLoggedRepository
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.GetDoctorVisitsRepository
import com.migrely.app.reminders.domain.repository.GetReminderSettingsRepository
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

private val TASHKENT = ZoneId.of("Asia/Tashkent")
private val NOVEMBER_20: LocalDate = LocalDate.of(2026, 11, 20)

/** Home's visit card (BRD D168, EC-REM-08, EC-REM-13, design D8). */
class GetVisitCardUseCaseTest {
    private val getReminderSettingsRepository: GetReminderSettingsRepository = mock()
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val getDoctorVisitsRepository: GetDoctorVisitsRepository = mock {
        on { doctorVisits(any()) } doReturn DoctorVisitsResultDomainModel.Loaded(
            DoctorVisitDomainModel("v", NOVEMBER_20),
            null
        )
    }
    private val getAnythingLoggedRepository: GetAnythingLoggedRepository = mock {
        on { anythingLogged(any(), any(), any()) } doReturn AnythingLoggedDomainModel.Logged
    }
    private lateinit var classUnderTest: GetVisitCardUseCase

    @Before
    fun setUp() {
        classUnderTest = GetVisitCardUseCase(
            getReminderSettingsRepository,
            getCurrentTimeRepository,
            getDoctorVisitsRepository,
            getAnythingLoggedRepository,
            CoroutineContextProvider.Default
        )
        givenClosedFor(null)
    }

    private fun cardAt(text: String): VisitCardDomainModel {
        given(getCurrentTimeRepository.currentTime()).willReturn(
            CurrentTimeDomainModel(LocalDateTime.parse(text).atZone(TASHKENT).toInstant(), TASHKENT)
        )
        return classUnderTest.executeInBackground(Unit)
    }

    private fun givenClosedFor(date: LocalDate?) {
        given(getReminderSettingsRepository.reminderSettings()).willReturn(
            ReminderSettingsResultDomainModel.Loaded(
                ReminderSettingsDomainModel(
                    false,
                    LocalTime.of(20, 0),
                    true,
                    null,
                    null,
                    visitCardClosedFor = date
                )
            )
        )
    }

    @Test
    fun `Given 11 AM two days before when home opens then the card shows the visit`() {
        assertEquals(VisitCardDomainModel.Shown(NOVEMBER_20), cardAt("2026-11-18T11:00"))
    }

    @Test
    fun `Given 9-59 two days before when home opens then no card yet`() {
        assertEquals(VisitCardDomainModel.Hidden, cardAt("2026-11-18T09:59"))
    }

    @Test
    fun `Given the visit day at 11 PM when home opens then the card still shows`() {
        assertEquals(VisitCardDomainModel.Shown(NOVEMBER_20), cardAt("2026-11-20T23:00"))
    }

    @Test
    fun `Given the card was closed for that visit when home opens then no card`() {
        givenClosedFor(NOVEMBER_20)

        assertEquals(VisitCardDomainModel.Hidden, cardAt("2026-11-19T11:00"))
    }

    @Test
    fun `Given nothing logged when home opens then no card`() {
        given(getAnythingLoggedRepository.anythingLogged(any(), any(), any()))
            .willReturn(AnythingLoggedDomainModel.NothingLogged)

        assertEquals(VisitCardDomainModel.Hidden, cardAt("2026-11-19T11:00"))
    }

    @Test
    fun `Given no next visit when home opens then no card`() {
        given(getDoctorVisitsRepository.doctorVisits(any()))
            .willReturn(DoctorVisitsResultDomainModel.Loaded(null, NOVEMBER_20))

        assertEquals(VisitCardDomainModel.Hidden, cardAt("2026-11-21T11:00"))
    }
}
