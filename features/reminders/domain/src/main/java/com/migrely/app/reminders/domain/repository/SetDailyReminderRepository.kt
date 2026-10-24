package com.migrely.app.reminders.domain.repository

import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import java.time.LocalTime

interface SetDailyReminderRepository {
    fun setDailyReminder(enabled: Boolean, time: LocalTime): ReminderWriteResultDomainModel
}
