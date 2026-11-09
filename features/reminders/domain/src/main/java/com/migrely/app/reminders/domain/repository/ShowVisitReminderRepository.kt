package com.migrely.app.reminders.domain.repository

import java.time.Instant
import java.time.LocalDate

interface ShowVisitReminderRepository {
    /** "Your visit is on <date>", which goes by itself at [until], the end of the visit day. */
    fun showVisitReminder(visit: LocalDate, until: Instant)
}
