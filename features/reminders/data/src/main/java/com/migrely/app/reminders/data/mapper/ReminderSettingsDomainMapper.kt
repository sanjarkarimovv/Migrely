package com.migrely.app.reminders.data.mapper

import com.migrely.app.datasource.reminder.model.ReminderSettingsDataModel
import com.migrely.app.reminders.domain.model.ReminderSettingsDomainModel
import java.time.LocalTime

private const val MINUTES_PER_HOUR = 60

class ReminderSettingsDomainMapper {
    fun toDomain(settings: ReminderSettingsDataModel) = ReminderSettingsDomainModel(
        dailyEnabled = settings.dailyEnabled,
        dailyTime = LocalTime.of(
            settings.dailyMinuteOfDay / MINUTES_PER_HOUR,
            settings.dailyMinuteOfDay % MINUTES_PER_HOUR
        ),
        effectEnabled = settings.effectEnabled,
        permissionAskedAt = settings.permissionAskedAt,
        dailyLastShownOn = settings.dailyLastShownOn
    )
}
