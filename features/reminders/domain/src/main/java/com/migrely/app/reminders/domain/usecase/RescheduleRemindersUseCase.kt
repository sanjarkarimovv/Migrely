package com.migrely.app.reminders.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.DoctorVisitsResultDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsResultDomainModel
import com.migrely.app.reminders.domain.repository.CancelDailyReminderRepository
import com.migrely.app.reminders.domain.repository.CancelVisitReminderRepository
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.GetDoctorVisitsRepository
import com.migrely.app.reminders.domain.repository.GetReminderSettingsRepository
import com.migrely.app.reminders.domain.repository.ScheduleDailyReminderRepository
import com.migrely.app.reminders.domain.repository.ScheduleVisitReminderRepository

/**
 * Plans the daily reminder and the visit reminder again by the device's local time: when the app starts, which also
 * covers a restore from a backup, after an import, and when the time zone or the clock changes (BRD EC-REM-05,
 * EC-REM-14). Planning replaces any run already planned, so it is safe to repeat. A reminder that is off, or a visit
 * already reminded of, stays unplanned; an attack's postponement is kept, so the reminder never jumps to the moment the
 * app opens.
 */
@Suppress("LongParameterList")
class RescheduleRemindersUseCase(
    private val getReminderSettingsRepository: GetReminderSettingsRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val scheduleDailyReminderRepository: ScheduleDailyReminderRepository,
    private val cancelDailyReminderRepository: CancelDailyReminderRepository,
    private val getDoctorVisitsRepository: GetDoctorVisitsRepository,
    scheduleVisitReminderRepository: ScheduleVisitReminderRepository,
    cancelVisitReminderRepository: CancelVisitReminderRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, Unit>(coroutineContextProvider) {
    private val dailyReminderTime = DailyReminderTime()
    private val visitReminderPlan =
        VisitReminderPlan(scheduleVisitReminderRepository, cancelVisitReminderRepository)

    override fun executeInBackground(request: Unit) {
        val result = getReminderSettingsRepository.reminderSettings()
        val settings = (result as? ReminderSettingsResultDomainModel.Loaded)?.settings ?: return
        val now = getCurrentTimeRepository.currentTime()
        if (settings.dailyEnabled) {
            val at = dailyReminderTime.next(now, settings.dailyTime)
            scheduleDailyReminderRepository.scheduleDailyReminder(
                at,
                dailyReminderTime.dateOf(at, now.zone)
            )
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
