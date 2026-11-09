package com.migrely.app.reminders.domain.repository

import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import java.time.LocalDate

interface SetVisitPostponedRepository {
    /** The visit reminder waits for 10:00 on [day] (BRD EC-REM-12). */
    fun setVisitPostponed(day: LocalDate): ReminderWriteResultDomainModel
}
