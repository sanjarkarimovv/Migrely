package com.migrely.app.reminders.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.CurrentTimeDomainModel
import com.migrely.app.reminders.domain.model.DoctorVisitDomainModel
import com.migrely.app.reminders.domain.model.DoctorVisitsResultDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsResultDomainModel
import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import com.migrely.app.reminders.domain.repository.CancelVisitReminderRepository
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.GetDoctorVisitsRepository
import com.migrely.app.reminders.domain.repository.GetReminderSettingsRepository
import com.migrely.app.reminders.domain.repository.ScheduleVisitReminderRepository
import com.migrely.app.reminders.domain.repository.SetVisitRemindedRepository
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
import org.mockito.kotlin.verify

private val TASHKENT = ZoneId.of("Asia/Tashkent")
private val NOVEMBER_20: LocalDate = LocalDate.of(2026, 11, 20)

private fun at(text: String) = LocalDateTime.parse(text).atZone(TASHKENT).toInstant()

/** After an import, a next visit whose reminder time has passed is treated as a UI save would (BRD EC-REM-08). */
class PlanRemindersAfterImportUseCaseTest {
    private val getReminderSettingsRepository: GetReminderSettingsRepository = mock {
        on { reminderSettings() } doReturn ReminderSettingsResultDomainModel.Loaded(
            ReminderSettingsDomainModel(false, LocalTime.of(20, 0), true, null, null)
        )
    }
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val getDoctorVisitsRepository: GetDoctorVisitsRepository = mock {
        on { doctorVisits(any()) } doReturn DoctorVisitsResultDomainModel.Loaded(
            DoctorVisitDomainModel("v", NOVEMBER_20),
            null
        )
    }
    private val setVisitRemindedRepository: SetVisitRemindedRepository = mock {
        on { setVisitReminded(any()) } doReturn ReminderWriteResultDomainModel.Done
    }
    private val scheduleVisitReminderRepository: ScheduleVisitReminderRepository = mock()
    private val cancelVisitReminderRepository: CancelVisitReminderRepository = mock()
    private lateinit var classUnderTest: PlanRemindersAfterImportUseCase

    @Before
    fun setUp() {
        classUnderTest = PlanRemindersAfterImportUseCase(
            getReminderSettingsRepository,
            getCurrentTimeRepository,
            getDoctorVisitsRepository,
            setVisitRemindedRepository,
            scheduleVisitReminderRepository,
            cancelVisitReminderRepository,
            CoroutineContextProvider.Default
        )
    }

    private fun givenNow(text: String) {
        given(
            getCurrentTimeRepository.currentTime()
        ).willReturn(CurrentTimeDomainModel(at(text), TASHKENT))
    }

    @Test
    fun `Given an imported visit too close when planned then it is marked and nothing posts`() {
        givenNow("2026-11-18T15:00")

        classUnderTest.executeInBackground(Unit)

        verify(setVisitRemindedRepository).setVisitReminded(NOVEMBER_20)
        verify(scheduleVisitReminderRepository, never()).scheduleVisitReminder(any(), any())
    }

    @Test
    fun `Given an imported visit not yet due when planned then it is planned (control)`() {
        givenNow("2026-11-18T09:00")

        classUnderTest.executeInBackground(Unit)

        verify(setVisitRemindedRepository, never()).setVisitReminded(any())
        verify(
            scheduleVisitReminderRepository
        ).scheduleVisitReminder(at("2026-11-18T10:00"), NOVEMBER_20)
    }
}
