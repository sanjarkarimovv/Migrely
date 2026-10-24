package com.migrely.app.datasource.reminder.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

internal const val REMINDER_SETTINGS_ID = 1
internal const val DEFAULT_DAILY_MINUTE_OF_DAY = 20 * 60

/** The one row of reminder settings (database version 6, BRD D68); [id] is always [REMINDER_SETTINGS_ID]. */
@Entity(tableName = "reminder_settings")
data class ReminderSettingsLocalModel(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "daily_enabled", defaultValue = "0") val dailyEnabled: Boolean,
    @ColumnInfo(name = "daily_minute_of_day", defaultValue = "1200") val dailyMinuteOfDay: Int,
    @ColumnInfo(name = "effect_enabled", defaultValue = "1") val effectEnabled: Boolean,
    @ColumnInfo(name = "permission_asked_epoch_millis") val permissionAskedEpochMillis: Long?,
    /** ISO `yyyy-MM-dd`. */
    @ColumnInfo(name = "daily_last_shown_date") val dailyLastShownDate: String?
)
