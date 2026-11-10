package com.migrely.app.reminders.domain.usecase

import com.migrely.app.reminders.domain.model.CurrentTimeDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsDomainModel
import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import com.migrely.app.reminders.domain.repository.DismissVisitReminderRepository
import com.migrely.app.reminders.domain.repository.SetVisitRemindedRepository
import java.time.LocalDate

/**
 * The one path that makes a date the next visit, for a new date and for Undo alike (BRD D168, design D3, D4). A visit
 * whose reminder time has passed already is marked as reminded before it is written, so it never posts and only the
 * home card shows it (EC-REM-08); a mark left by a failed write is harmless, since that date stays too close. The old
 * date's notification goes (EC-REM-09) and the reminder is planned again.
 */
internal class NextVisitSave(
    private val setVisitRemindedRepository: SetVisitRemindedRepository,
    private val dismissVisitReminderRepository: DismissVisitReminderRepository,
    private val visitReminderPlan: VisitReminderPlan
) {
    private val visitReminderTime = VisitReminderTime()

    fun save(
        date: LocalDate,
        settings: ReminderSettingsDomainModel,
        now: CurrentTimeDomainModel,
        write: () -> ReminderWriteResultDomainModel
    ): ReminderWriteResultDomainModel {
        val tooClose = visitReminderTime.tooClose(date, now.instant, now.zone)
        if (tooClose) {
            val marked = setVisitRemindedRepository.setVisitReminded(date)
            if (marked != ReminderWriteResultDomainModel.Done) return marked
        }
        val written = write()
        if (written != ReminderWriteResultDomainModel.Done) return written
        dismissVisitReminderRepository.dismissVisitReminder()
        visitReminderPlan.plan(
            next = date,
            remindedFor = if (tooClose) date else settings.visitRemindedFor,
            postponedTo = settings.visitPostponedTo,
            now = now
        )
        return written
    }
}
