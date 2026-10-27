package com.migrely.app.reminders.data.repository

import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.attack.model.AttackDataModel
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.daymark.datasource.DayMarkDataSource
import com.migrely.app.datasource.daymark.model.DayMarkDataModel
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.exception.StorageFullDataException
import com.migrely.app.datasource.notification.datasource.NotificationDataSource
import com.migrely.app.datasource.reminder.datasource.ReminderSettingsDataSource
import com.migrely.app.datasource.reminder.model.ReminderSettingsDataModel
import com.migrely.app.datasource.schedule.datasource.ReminderScheduleDataSource
import com.migrely.app.reminders.data.mapper.ReminderSettingsDomainMapper
import com.migrely.app.reminders.data.mapper.ReminderZoneDomainMapper
import com.migrely.app.reminders.domain.model.ReminderAttackDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsResultDomainModel
import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import com.migrely.app.reminders.domain.model.TodayEntriesResultDomainModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

private val DATE = LocalDate.of(2026, 10, 24)
private val FROM = Instant.parse("2026-10-21T19:00:00Z")
private val TO = Instant.parse("2026-10-26T19:00:00Z")

class RemindersRepositoryTest {
    private val reminderSettingsDataSource: ReminderSettingsDataSource = mock()
    private val reminderScheduleDataSource: ReminderScheduleDataSource = mock()
    private val notificationDataSource: NotificationDataSource = mock()
    private val attackDataSource: AttackDataSource = mock()
    private val dayMarkDataSource: DayMarkDataSource = mock()
    private val clockDataSource: ClockDataSource = mock()
    private val classUnderTest = RemindersRepository(
        reminderSettingsDataSource,
        reminderScheduleDataSource,
        notificationDataSource,
        attackDataSource,
        dayMarkDataSource,
        clockDataSource,
        ReminderSettingsDomainMapper(),
        ReminderZoneDomainMapper()
    )

    @Test
    fun `When reading the settings then the minute of the day as a time, or failed`() {
        given(reminderSettingsDataSource.settings())
            .willReturn(ReminderSettingsDataModel(true, 21 * 60 + 30, false, null, DATE))
        assertEquals(
            ReminderSettingsResultDomainModel.Loaded(
                ReminderSettingsDomainModel(true, LocalTime.of(21, 30), false, null, DATE)
            ),
            classUnderTest.reminderSettings()
        )

        given(reminderSettingsDataSource.settings()).willThrow(DatabaseDataException())
        assertEquals(ReminderSettingsResultDomainModel.Failed, classUnderTest.reminderSettings())
    }

    @Test
    fun `When writing then done, storage full or failed`() {
        assertEquals(
            ReminderWriteResultDomainModel.Done,
            classUnderTest.setDailyReminder(true, LocalTime.of(20, 0))
        )
        verify(reminderSettingsDataSource).setDaily(true, 1200)

        whenever(reminderSettingsDataSource.setEffect(false)).doThrow(StorageFullDataException())
        assertEquals(
            ReminderWriteResultDomainModel.StorageFull,
            classUnderTest.setEffectReminder(false)
        )

        whenever(reminderSettingsDataSource.setDailyShown(DATE)).doThrow(DatabaseDataException())
        assertEquals(ReminderWriteResultDomainModel.Failed, classUnderTest.setDailyShown(DATE))
    }

    @Test
    fun `When reading today's entries then the attacks with their zones and the pain-free mark`() {
        val start = Instant.parse("2026-10-24T04:00:00Z")
        given(attackDataSource.overlapping(excludingId = null, start = FROM, end = TO)).willReturn(
            listOf(AttackDataModel("a", start, "Asia/Tashkent", null, emptyList(), null, false))
        )
        given(dayMarkDataSource.marksBetween(DATE, DATE))
            .willReturn(listOf(DayMarkDataModel(DATE, period = true, painFree = false)))

        assertEquals(
            TodayEntriesResultDomainModel.Loaded(
                listOf(ReminderAttackDomainModel(start, ZoneId.of("Asia/Tashkent"), null)),
                painFree = false
            ),
            classUnderTest.todayEntries(FROM, TO, DATE)
        )
    }

    @Test
    fun `Given the attacks cannot be read when reading today's entries then failed`() {
        given(attackDataSource.overlapping(excludingId = null, start = FROM, end = TO))
            .willThrow(DatabaseDataException())

        assertEquals(
            TodayEntriesResultDomainModel.Failed,
            classUnderTest.todayEntries(FROM, TO, DATE)
        )
    }

    @Test
    fun `When scheduling and notifying then the datasources do it`() {
        given(notificationDataSource.areEnabled()).willReturn(true)

        assertEquals(true, classUnderTest.notificationsAllowed())
        classUnderTest.scheduleDailyReminder(TO, DATE)
        classUnderTest.dismissDailyReminder()

        verify(reminderScheduleDataSource).scheduleDaily(TO, DATE)
        verify(notificationDataSource).cancelDailyReminder()
    }
}
