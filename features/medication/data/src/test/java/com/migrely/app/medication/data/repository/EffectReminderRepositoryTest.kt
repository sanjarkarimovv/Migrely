package com.migrely.app.medication.data.repository

import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.notification.datasource.NotificationDataSource
import com.migrely.app.datasource.reminder.datasource.ReminderSettingsDataSource
import com.migrely.app.datasource.reminder.model.ReminderSettingsDataModel
import com.migrely.app.datasource.schedule.datasource.ReminderScheduleDataSource
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class EffectReminderRepositoryTest {
    private val reminderScheduleDataSource: ReminderScheduleDataSource = mock()
    private val notificationDataSource: NotificationDataSource = mock()
    private val reminderSettingsDataSource: ReminderSettingsDataSource = mock()
    private val classUnderTest =
        EffectReminderRepository(
            reminderScheduleDataSource,
            notificationDataSource,
            reminderSettingsDataSource
        )

    @Test
    fun `When cancelling then the planned run and a shown notification both go`() {
        classUnderTest.cancelEffectQuestion("i")

        verify(reminderScheduleDataSource).cancelEffectQuestion("i")
        verify(notificationDataSource).cancelEffectQuestion("i")
    }

    @Test
    fun `When reading the setting then it, or off when storage fails`() {
        given(reminderSettingsDataSource.settings())
            .willReturn(ReminderSettingsDataModel(false, 1200, effectEnabled = true, null, null))
        assertEquals(true, classUnderTest.effectReminderEnabled())

        given(reminderSettingsDataSource.settings()).willThrow(DatabaseDataException())
        assertEquals(false, classUnderTest.effectReminderEnabled())
    }

    @Test
    fun `When planning then the schedule datasource plans it`() {
        val at = Instant.parse("2026-10-24T11:00:00Z")

        classUnderTest.scheduleEffectQuestion("i", at)

        verify(reminderScheduleDataSource).scheduleEffectQuestion("i", at)
    }
}
