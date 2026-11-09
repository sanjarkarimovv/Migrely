package com.migrely.app.reminders.domain.usecase

import com.migrely.app.reminders.domain.model.CurrentTimeDomainModel
import com.migrely.app.reminders.domain.repository.CancelVisitReminderRepository
import com.migrely.app.reminders.domain.repository.ScheduleVisitReminderRepository
import java.time.LocalDate

/**
 * Plans the visit reminder, or cancels it (BRD D168, design D3). It is planned only for a next visit that has not been
 * reminded of and whose day has not ended; a time already passed runs as soon as it can, so a phone that was off still
 * reminds on the visit day or before (EC-REM-14). Every path that changes the visit or the clock comes here, so it is
 * safe to repeat.
 */
internal class VisitReminderPlan(
    private val scheduleVisitReminderRepository: ScheduleVisitReminderRepository,
    private val cancelVisitReminderRepository: CancelVisitReminderRepository
) {
    private val visitReminderTime = VisitReminderTime()

    fun plan(
        next: LocalDate?,
        remindedFor: LocalDate?,
        postponedTo: LocalDate?,
        now: CurrentTimeDomainModel
    ) {
        if (next == null || remindedFor == next ||
            !now.instant.isBefore(visitReminderTime.dayEnd(next, now.zone))
        ) {
            cancelVisitReminderRepository.cancelVisitReminder()
        } else {
            scheduleVisitReminderRepository.scheduleVisitReminder(
                visitReminderTime.remindAt(next, postponedTo, now.zone),
                next
            )
        }
    }
}
