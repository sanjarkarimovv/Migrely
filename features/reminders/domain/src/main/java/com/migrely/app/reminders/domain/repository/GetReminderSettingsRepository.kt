package com.migrely.app.reminders.domain.repository

import com.migrely.app.reminders.domain.model.ReminderSettingsResultDomainModel

interface GetReminderSettingsRepository {
    fun reminderSettings(): ReminderSettingsResultDomainModel
}
