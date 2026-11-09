package com.migrely.app.reminders.domain.repository

import java.time.Instant
import java.time.LocalDate

interface ScheduleVisitReminderRepository {
    /** Plans the reminder for the visit on [visit] at about [at], replacing one already planned. */
    fun scheduleVisitReminder(at: Instant, visit: LocalDate)
}
