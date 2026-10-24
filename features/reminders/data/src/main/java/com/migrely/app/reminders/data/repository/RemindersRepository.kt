package com.migrely.app.reminders.data.repository

import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.daymark.datasource.DayMarkDataSource
import com.migrely.app.datasource.exception.DataException
import com.migrely.app.datasource.exception.StorageFullDataException
import com.migrely.app.datasource.notification.datasource.NotificationDataSource
import com.migrely.app.datasource.reminder.datasource.ReminderSettingsDataSource
import com.migrely.app.datasource.schedule.datasource.ReminderScheduleDataSource
import com.migrely.app.reminders.data.mapper.ReminderSettingsDomainMapper
import com.migrely.app.reminders.data.mapper.ReminderZoneDomainMapper
import com.migrely.app.reminders.domain.model.CurrentTimeDomainModel
import com.migrely.app.reminders.domain.model.ReminderAttackDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsResultDomainModel
import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import com.migrely.app.reminders.domain.model.TodayEntriesResultDomainModel
import com.migrely.app.reminders.domain.repository.CancelDailyReminderRepository
import com.migrely.app.reminders.domain.repository.DismissDailyReminderRepository
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.GetNotificationsAllowedRepository
import com.migrely.app.reminders.domain.repository.GetReminderSettingsRepository
import com.migrely.app.reminders.domain.repository.GetTodayEntriesRepository
import com.migrely.app.reminders.domain.repository.ScheduleDailyReminderRepository
import com.migrely.app.reminders.domain.repository.SetDailyReminderRepository
import com.migrely.app.reminders.domain.repository.SetDailyShownRepository
import com.migrely.app.reminders.domain.repository.SetEffectReminderRepository
import com.migrely.app.reminders.domain.repository.SetPermissionAskedRepository
import com.migrely.app.reminders.domain.repository.ShowAttackLoggedTodayRepository
import com.migrely.app.reminders.domain.repository.ShowDailyReminderRepository
import java.time.Instant
import java.time.LocalDate

/**
 * What the reminders feature reads and writes (BRD D65–D68). Storage failures are values (docs/ARCHITECTURE.md
 * section 7); scheduling and notifications do not fail.
 */
@Suppress("LongParameterList")
class RemindersRepository(
    private val reminderSettingsDataSource: ReminderSettingsDataSource,
    private val reminderScheduleDataSource: ReminderScheduleDataSource,
    private val notificationDataSource: NotificationDataSource,
    private val attackDataSource: AttackDataSource,
    private val dayMarkDataSource: DayMarkDataSource,
    private val clockDataSource: ClockDataSource,
    private val reminderSettingsDomainMapper: ReminderSettingsDomainMapper,
    private val reminderZoneDomainMapper: ReminderZoneDomainMapper
) : GetCurrentTimeRepository,
    GetReminderSettingsRepository,
    GetNotificationsAllowedRepository,
    SetDailyReminderRepository,
    SetEffectReminderRepository,
    SetPermissionAskedRepository,
    SetDailyShownRepository,
    ScheduleDailyReminderRepository,
    CancelDailyReminderRepository,
    ShowDailyReminderRepository,
    ShowAttackLoggedTodayRepository,
    DismissDailyReminderRepository,
    GetTodayEntriesRepository {
    override fun currentTime() =
        CurrentTimeDomainModel(clockDataSource.now(), clockDataSource.zone())

    override fun reminderSettings(): ReminderSettingsResultDomainModel = try {
        ReminderSettingsResultDomainModel.Loaded(
            reminderSettingsDomainMapper.toDomain(reminderSettingsDataSource.settings())
        )
    } catch (_: DataException) {
        ReminderSettingsResultDomainModel.Failed
    }

    override fun notificationsAllowed(): Boolean = notificationDataSource.areEnabled()

    override fun setDailyReminder(enabled: Boolean, minuteOfDay: Int) =
        write { reminderSettingsDataSource.setDaily(enabled, minuteOfDay) }

    override fun setEffectReminder(enabled: Boolean) = write {
        reminderSettingsDataSource.setEffect(enabled)
    }

    override fun setPermissionAsked(at: Instant) = write {
        reminderSettingsDataSource.setPermissionAsked(at)
    }

    override fun setDailyShown(date: LocalDate) = write {
        reminderSettingsDataSource.setDailyShown(date)
    }

    override fun scheduleDailyReminder(at: Instant) = reminderScheduleDataSource.scheduleDaily(at)

    override fun cancelDailyReminder() = reminderScheduleDataSource.cancelDaily()

    override fun showDailyReminder() = notificationDataSource.showDailyReminder()

    override fun showAttackLoggedToday() = notificationDataSource.showAttackLoggedToday()

    override fun dismissDailyReminder() = notificationDataSource.cancelDailyReminder()

    override fun todayEntries(
        from: Instant,
        to: Instant,
        date: LocalDate
    ): TodayEntriesResultDomainModel = try {
        TodayEntriesResultDomainModel.Loaded(
            attacks = attackDataSource.overlapping(excludingId = null, start = from, end = to).map {
                ReminderAttackDomainModel(
                    it.start,
                    reminderZoneDomainMapper.toDomain(it.startZoneId),
                    it.end
                )
            },
            painFree = dayMarkDataSource.marksBetween(date, date).firstOrNull()?.painFree == true
        )
    } catch (_: DataException) {
        TodayEntriesResultDomainModel.Failed
    }

    private inline fun write(block: () -> Unit): ReminderWriteResultDomainModel = try {
        block()
        ReminderWriteResultDomainModel.Done
    } catch (_: StorageFullDataException) {
        ReminderWriteResultDomainModel.StorageFull
    } catch (_: DataException) {
        ReminderWriteResultDomainModel.Failed
    }
}
