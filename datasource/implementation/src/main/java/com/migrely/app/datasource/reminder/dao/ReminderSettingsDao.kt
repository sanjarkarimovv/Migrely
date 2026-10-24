package com.migrely.app.datasource.reminder.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.migrely.app.datasource.reminder.model.DEFAULT_DAILY_MINUTE_OF_DAY
import com.migrely.app.datasource.reminder.model.REMINDER_SETTINGS_ID
import com.migrely.app.datasource.reminder.model.ReminderSettingsLocalModel

@Dao
abstract class ReminderSettingsDao {
    @Query("SELECT * FROM reminder_settings WHERE id = 1")
    abstract fun settings(): ReminderSettingsLocalModel?

    @Upsert
    abstract fun upsert(settings: ReminderSettingsLocalModel)

    /** Changes the one row in a transaction, starting from the defaults when there is none yet. */
    @Transaction
    open fun update(change: (ReminderSettingsLocalModel) -> ReminderSettingsLocalModel) {
        upsert(change(settings() ?: DEFAULTS))
    }

    private companion object {
        val DEFAULTS = ReminderSettingsLocalModel(
            id = REMINDER_SETTINGS_ID,
            dailyEnabled = false,
            dailyMinuteOfDay = DEFAULT_DAILY_MINUTE_OF_DAY,
            effectEnabled = true,
            permissionAskedEpochMillis = null
        )
    }
}
