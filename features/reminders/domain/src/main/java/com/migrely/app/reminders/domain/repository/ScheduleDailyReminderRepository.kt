package com.migrely.app.reminders.domain.repository

import java.time.Instant

interface ScheduleDailyReminderRepository {
    fun scheduleDailyReminder(at: Instant)
}
