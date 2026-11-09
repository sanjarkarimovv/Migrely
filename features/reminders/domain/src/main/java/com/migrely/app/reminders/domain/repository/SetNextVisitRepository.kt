package com.migrely.app.reminders.domain.repository

import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import java.time.LocalDate

interface SetNextVisitRepository {
    /** Makes [date] the only visit on or after [today]. */
    fun setNextVisit(date: LocalDate, today: LocalDate): ReminderWriteResultDomainModel
}
