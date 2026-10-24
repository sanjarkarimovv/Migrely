package com.migrely.app.reminders.domain.repository

import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel

interface SetDailyReminderRepository {
    fun setDailyReminder(enabled: Boolean, minuteOfDay: Int): ReminderWriteResultDomainModel
}
