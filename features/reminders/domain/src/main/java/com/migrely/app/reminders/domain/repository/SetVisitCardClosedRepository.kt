package com.migrely.app.reminders.domain.repository

import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import java.time.LocalDate

interface SetVisitCardClosedRepository {
    fun setVisitCardClosed(visit: LocalDate): ReminderWriteResultDomainModel
}
