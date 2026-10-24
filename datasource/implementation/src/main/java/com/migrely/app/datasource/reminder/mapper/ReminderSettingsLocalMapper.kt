package com.migrely.app.datasource.reminder.mapper

import com.migrely.app.datasource.reminder.model.DEFAULT_DAILY_MINUTE_OF_DAY
import com.migrely.app.datasource.reminder.model.ReminderSettingsDataModel
import com.migrely.app.datasource.reminder.model.ReminderSettingsLocalModel
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeParseException

class ReminderSettingsLocalMapper {
    fun toData(local: ReminderSettingsLocalModel?): ReminderSettingsDataModel =
        ReminderSettingsDataModel(
            dailyEnabled = local?.dailyEnabled ?: false,
            dailyMinuteOfDay = local?.dailyMinuteOfDay ?: DEFAULT_DAILY_MINUTE_OF_DAY,
            effectEnabled = local?.effectEnabled ?: true,
            permissionAskedAt = local?.permissionAskedEpochMillis?.let(Instant::ofEpochMilli),
            dailyLastShownOn = local?.dailyLastShownDate?.let(::toDate)
        )

    fun toLocal(date: LocalDate): String = date.toString()

    @Suppress("SwallowedException")
    private fun toDate(local: String): LocalDate? = try {
        LocalDate.parse(local)
    } catch (exception: DateTimeParseException) {
        null
    }
}
