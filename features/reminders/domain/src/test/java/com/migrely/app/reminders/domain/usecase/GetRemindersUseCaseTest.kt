package com.migrely.app.reminders.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.CurrentTimeDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsResultDomainModel
import com.migrely.app.reminders.domain.model.RemindersResultDomainModel
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.GetNotificationsAllowedRepository
import com.migrely.app.reminders.domain.repository.GetReminderSettingsRepository
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

private val NOW: Instant = Instant.parse("2026-10-24T10:00:00Z")

class GetRemindersUseCaseTest {
    private val getReminderSettingsRepository: GetReminderSettingsRepository = mock()
    private val getNotificationsAllowedRepository: GetNotificationsAllowedRepository = mock()
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private lateinit var classUnderTest: GetRemindersUseCase

    @Before
    fun setUp() {
        classUnderTest = GetRemindersUseCase(
            getReminderSettingsRepository,
            getNotificationsAllowedRepository,
            getCurrentTimeRepository,
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
}
