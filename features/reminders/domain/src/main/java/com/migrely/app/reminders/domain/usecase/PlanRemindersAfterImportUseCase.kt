package com.migrely.app.reminders.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.DoctorVisitsResultDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsResultDomainModel
import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import com.migrely.app.reminders.domain.repository.CancelVisitReminderRepository
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.GetDoctorVisitsRepository
import com.migrely.app.reminders.domain.repository.GetReminderSettingsRepository
import com.migrely.app.reminders.domain.repository.ScheduleVisitReminderRepository
import com.migrely.app.reminders.domain.repository.SetVisitRemindedRepository

/**
 * Plans the visit reminder after an import brought or took away a visit (design D7). An imported next visit whose
 * reminder time has passed is handled like one set on the Reminders screen: it never posts, and only the home card
 * shows it (BRD EC-REM-08). An import keeps the reminder settings, so the daily reminder needs nothing.
 */
@Suppress("LongParameterList")
class PlanRemindersAfterImportUseCase(
    private val getReminderSettingsRepository: GetReminderSettingsRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val getDoctorVisitsRepository: GetDoctorVisitsRepository,
    private val setVisitRemindedRepository: SetVisitRemindedRepository,
    scheduleVisitReminderRepository: ScheduleVisitReminderRepository,
    cancelVisitReminderRepository: CancelVisitReminderRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, Unit>(coroutineContextProvider) {
    private val visitReminderTime = VisitReminderTime()
    private val visitReminderPlan =
        VisitReminderPlan(scheduleVisitReminderRepository, cancelVisitReminderRepository)

    override fun executeInBackground(request: Unit) {
        val settings = (
            getReminderSettingsRepository.reminderSettings() as?
                ReminderSettingsResultDomainModel.Loaded
            )?.settings ?: return
        val now = getCurrentTimeRepository.currentTime()
        val visits = getDoctorVisitsRepository.doctorVisits(now.instant.atZone(now.zone).toLocalDate()) as?
            DoctorVisitsResultDomainModel.Loaded ?: return
        val next = visits.next?.date
        val marked = next != null &&
            settings.visitRemindedFor != next &&
            visitReminderTime.tooClose(next, now.instant, now.zone) &&
            setVisitRemindedRepository.setVisitReminded(next) == ReminderWriteResultDomainModel.Done
        visitReminderPlan.plan(
            next = next,
            remindedFor = if (marked) next else settings.visitRemindedFor,
            postponedTo = settings.visitPostponedTo,
            now = now
        )
    }
}
