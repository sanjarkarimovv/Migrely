package com.migrely.app.reminders.domain.repository

import java.time.LocalDate

interface ShowDailyReminderRepository {
    /**
     * "How was today?" about [date]; its "Pain-free day" marks [date]. A [quiet] one has no sound and no vibration, and
     * everything else stays the same (BRD D183).
     */
    fun showDailyReminder(date: LocalDate, quiet: Boolean)
}
