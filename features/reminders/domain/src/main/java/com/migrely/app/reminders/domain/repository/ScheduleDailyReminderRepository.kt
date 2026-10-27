package com.migrely.app.reminders.domain.repository

import java.time.Instant
import java.time.LocalDate

interface ScheduleDailyReminderRepository {
    /** Plans the daily reminder for [date] at about [at]; the run asks about [date] (BRD D66). */
    fun scheduleDailyReminder(at: Instant, date: LocalDate)
}
