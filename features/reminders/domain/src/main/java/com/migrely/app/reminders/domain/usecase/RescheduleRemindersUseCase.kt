package com.migrely.app.reminders.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.DoctorVisitsResultDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsResultDomainModel
import com.migrely.app.reminders.domain.model.RescheduleRemindersRequestDomainModel
import com.migrely.app.reminders.domain.repository.CancelDailyReminderRepository
import com.migrely.app.reminders.domain.repository.CancelVisitReminderRepository
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.GetDoctorVisitsRepository
import com.migrely.app.reminders.domain.repository.GetReminderSettingsRepository
import com.migrely.app.reminders.domain.repository.PlanDailyReminderIfNoneRepository
import com.migrely.app.reminders.domain.repository.ScheduleDailyReminderRepository
import com.migrely.app.reminders.domain.repository.ScheduleVisitReminderRepository

/**
 * Plans the daily reminder and the visit reminder again by the device's local time: when the app starts, which also
 * covers a restore from a backup and a lost WorkManager store, and when the time zone or the clock changes (BRD
 * EC-REM-05, EC-REM-14, EC-REM-22). The daily reminder aims at [DailyReminderTime.due], so no day whose reminder has not
 * shown is skipped (D176). An app start keeps a daily run already planned, even a late one; a clock change replaces it,
 * so it moves to the new local time (D176, EC-REM-24, EC-REM-25). The visit reminder replaces its run either way. A
 * reminder that is off, or a visit already reminded of, stays unplanned; an attack's postponement is kept, so the
 * reminder never jumps to the moment the app opens. Safe to repeat.
 */
@Suppress("LongParameterList")
class RescheduleRemindersUseCase(
    private val getReminderSettingsRepository: GetReminderSettingsRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val scheduleDailyReminderRepository: ScheduleDailyReminderRepository,
    private val planDailyReminderIfNoneRepository: PlanDailyReminderIfNoneRepository,
    private val cancelDailyReminderRepository: CancelDailyReminderRepository,
    private val getDoctorVisitsRepository: GetDoctorVisitsRepository,
    scheduleVisitReminderRepository: ScheduleVisitReminderRepository,
    cancelVisitReminderRepository: CancelVisitReminderRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<RescheduleRemindersRequestDomainModel, Unit>(
    coroutineContextProvider
) {
    private val dailyReminderTime = DailyReminderTime()
    private val visitReminderPlan =
        VisitReminderPlan(scheduleVisitReminderRepository, cancelVisitReminderRepository)

    override fun executeInBackground(request: RescheduleRemindersRequestDomainModel) {
        val result = getReminderSettingsRepository.reminderSettings()
        val settings = (result as? ReminderSettingsResultDomainModel.Loaded)?.settings ?: return
        val now = getCurrentTimeRepository.currentTime()
        if (settings.dailyEnabled) {
            val at = dailyReminderTime.due(now, settings.dailyTime, settings.dailyLastShownOn)
            val date = dailyReminderTime.dateOf(at, now.zone)
            when (request) {
                RescheduleRemindersRequestDomainModel.AppStart ->
                    planDailyReminderIfNoneRepository.planDailyReminderIfNone(at, date)
                RescheduleRemindersRequestDomainModel.ClockChange ->
                    scheduleDailyReminderRepository.scheduleDailyReminder(at, date)
            }
        } else {
            cancelDailyReminderRepository.cancelDailyReminder()
        }
        val today = now.instant.atZone(now.zone).toLocalDate()
        val visits = getDoctorVisitsRepository.doctorVisits(today)
        if (visits is DoctorVisitsResultDomainModel.Loaded) {
            visitReminderPlan.plan(
                next = visits.next?.date,
                remindedFor = settings.visitRemindedFor,
                postponedTo = settings.visitPostponedTo,
                now = now
            )
        }
    }
}
