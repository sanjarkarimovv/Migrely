package com.migrely.app.reminders.domain.repository

import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import java.time.LocalDate

interface SetVisitRemindedRepository {
    /** The visit on [visit] needs no notification any more: it showed, or its time passed before it was set. */
    fun setVisitReminded(visit: LocalDate): ReminderWriteResultDomainModel
}
