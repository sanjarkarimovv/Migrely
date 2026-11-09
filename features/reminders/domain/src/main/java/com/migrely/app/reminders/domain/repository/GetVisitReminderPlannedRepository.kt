package com.migrely.app.reminders.domain.repository

import java.time.LocalDate

interface GetVisitReminderPlannedRepository {
    /** Whether a run of the reminder for the visit on [visit] is still planned. */
    fun visitReminderPlanned(visit: LocalDate): Boolean
}
