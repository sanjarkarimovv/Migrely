package com.migrely.app.reminders.domain.repository

import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel

interface SetEffectReminderRepository {
    fun setEffectReminder(enabled: Boolean): ReminderWriteResultDomainModel
}
