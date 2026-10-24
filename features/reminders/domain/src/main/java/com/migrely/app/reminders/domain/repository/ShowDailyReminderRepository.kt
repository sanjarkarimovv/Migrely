package com.migrely.app.reminders.domain.repository

import java.time.LocalDate

interface ShowDailyReminderRepository {
    /** "How was today?" about [date]; its "Pain-free day" marks [date]. */
    fun showDailyReminder(date: LocalDate)
}
