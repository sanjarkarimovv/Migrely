package com.migrely.app.reminders.domain.repository

import com.migrely.app.reminders.domain.model.DoctorVisitDomainModel
import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import java.time.LocalDate

interface RestoreNextVisitRepository {
    /** Puts [visit] back as the only visit on or after [today]. */
    fun restoreNextVisit(
        visit: DoctorVisitDomainModel,
        today: LocalDate
    ): ReminderWriteResultDomainModel
}
