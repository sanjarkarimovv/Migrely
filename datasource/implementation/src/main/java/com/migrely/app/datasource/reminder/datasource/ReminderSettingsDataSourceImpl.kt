package com.migrely.app.datasource.reminder.datasource

import com.migrely.app.datasource.database.database
import com.migrely.app.datasource.reminder.dao.ReminderSettingsDao
import com.migrely.app.datasource.reminder.mapper.ReminderSettingsLocalMapper
import com.migrely.app.datasource.reminder.model.ReminderSettingsDataModel
import java.time.Instant

class ReminderSettingsDataSourceImpl(
    private val reminderSettingsDao: ReminderSettingsDao,
    private val reminderSettingsLocalMapper: ReminderSettingsLocalMapper
) : ReminderSettingsDataSource {
    override fun settings(): ReminderSettingsDataModel = database {
        reminderSettingsLocalMapper.toData(reminderSettingsDao.settings())
    }

    override fun setDaily(enabled: Boolean, minuteOfDay: Int) = database {
        reminderSettingsDao.update {
            it.copy(dailyEnabled = enabled, dailyMinuteOfDay = minuteOfDay)
        }
    }

    override fun setEffect(enabled: Boolean) = database {
        reminderSettingsDao.update { it.copy(effectEnabled = enabled) }
    }

    override fun setPermissionAsked(at: Instant) = database {
        reminderSettingsDao.update { it.copy(permissionAskedEpochMillis = at.toEpochMilli()) }
    }
}
