package com.migrely.app.reminders.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.AnythingLoggedDomainModel
import com.migrely.app.reminders.domain.model.AttackInProgressDomainModel
import com.migrely.app.reminders.domain.model.DoctorVisitsResultDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsResultDomainModel
import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import com.migrely.app.reminders.domain.model.VisitReminderRunDomainModel
import com.migrely.app.reminders.domain.repository.GetAnythingLoggedRepository
import com.migrely.app.reminders.domain.repository.GetAttackInProgressRepository
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.GetDoctorVisitsRepository
import com.migrely.app.reminders.domain.repository.GetReminderSettingsRepository
import com.migrely.app.reminders.domain.repository.ScheduleVisitReminderRepository
import com.migrely.app.reminders.domain.repository.SetVisitPostponedRepository
import com.migrely.app.reminders.domain.repository.SetVisitRemindedRepository
import com.migrely.app.reminders.domain.repository.ShowVisitReminderRepository
import java.time.LocalDate
import java.time.ZoneId

/**
 * One run of the visit reminder planned for the visit on its date (BRD D168, design D3), in order:
 * 1. a date that is not the next visit any more, 2. one already reminded of, and 3. a visit day that has ended post
 * nothing; 4. during an attack it waits for 10:00 the next day, up to the visit day, so it never comes at night
 * (EC-REM-12); 5. with nothing logged in the report's default period it posts nothing, and the card covers an entry
 * made later (EC-REM-13); 6. otherwise it posts once and remembers that.
 */
@Suppress("LongParameterList")
class RunVisitReminderUseCase(
    private val getReminderSettingsRepository: GetReminderSettingsRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val getDoctorVisitsRepository: GetDoctorVisitsRepository,
    private val getAttackInProgressRepository: GetAttackInProgressRepository,
    private val getAnythingLoggedRepository: GetAnythingLoggedRepository,
    private val setVisitPostponedRepository: SetVisitPostponedRepository,
    private val scheduleVisitReminderRepository: ScheduleVisitReminderRepository,
    private val showVisitReminderRepository: ShowVisitReminderRepository,
    private val setVisitRemindedRepository: SetVisitRemindedRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<LocalDate, VisitReminderRunDomainModel>(coroutineContextProvider) {
    private val visitReminderTime = VisitReminderTime()

    override fun executeInBackground(request: LocalDate): VisitReminderRunDomainModel {
        val settings = when (val result = getReminderSettingsRepository.reminderSettings()) {
            is ReminderSettingsResultDomainModel.Loaded -> result.settings
            ReminderSettingsResultDomainModel.Failed -> return VisitReminderRunDomainModel.Failed
        }
        val now = getCurrentTimeRepository.currentTime()
        val today = now.instant.atZone(now.zone).toLocalDate()
        val visits = when (val result = getDoctorVisitsRepository.doctorVisits(today)) {
            is DoctorVisitsResultDomainModel.Loaded -> result
            DoctorVisitsResultDomainModel.Failed -> return VisitReminderRunDomainModel.Failed
        }
        val dayEnd = visitReminderTime.dayEnd(request, now.zone)
        if (visits.next?.date != request ||
            settings.visitRemindedFor == request ||
            !now.instant.isBefore(dayEnd)
        ) {
            return VisitReminderRunDomainModel.Skipped
        }
        when (getAttackInProgressRepository.attackInProgress()) {
            AttackInProgressDomainModel.InProgress -> return postpone(
                request,
                today.plusDays(1),
                now.zone
            )
            AttackInProgressDomainModel.Failed -> return VisitReminderRunDomainModel.Failed
            AttackInProgressDomainModel.NotInProgress -> Unit
        }
        val logged = getAnythingLoggedRepository.anythingLogged(
            DefaultReportPeriod.firstDay(visits.last, today),
            today,
            now.zone
        )
        return when (logged) {
            AnythingLoggedDomainModel.Failed -> VisitReminderRunDomainModel.Failed
            AnythingLoggedDomainModel.NothingLogged -> VisitReminderRunDomainModel.Skipped
            AnythingLoggedDomainModel.Logged -> {
                showVisitReminderRepository.showVisitReminder(request, dayEnd)
                setVisitRemindedRepository.setVisitReminded(request)
                VisitReminderRunDomainModel.Posted
            }
        }
    }

    private fun postpone(
        visit: LocalDate,
        tomorrow: LocalDate,
        zone: ZoneId
    ): VisitReminderRunDomainModel {
        if (tomorrow.isAfter(visit)) return VisitReminderRunDomainModel.Skipped
        if (setVisitPostponedRepository.setVisitPostponed(tomorrow) !=
            ReminderWriteResultDomainModel.Done
        ) {
            return VisitReminderRunDomainModel.Failed
        }
        scheduleVisitReminderRepository.scheduleVisitReminder(
            visitReminderTime.remindAt(visit, tomorrow, zone),
            visit
        )
        return VisitReminderRunDomainModel.Postponed
    }
}
