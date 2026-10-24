package com.migrely.app.reminders.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.ReminderSettingsResultDomainModel
import com.migrely.app.reminders.domain.repository.CancelDailyReminderRepository
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.GetReminderSettingsRepository
import com.migrely.app.reminders.domain.repository.ScheduleDailyReminderRepository

/**
 * Plans the daily reminder again by the device's local time: when the app starts, which also covers a restore from a
 * backup, and when the time zone or the clock changes (BRD EC-REM-05). Planning replaces any run already planned, so
 * it is safe to repeat. A reminder that is off stays unplanned.
 */
class RescheduleRemindersUseCase(
    private val getReminderSettingsRepository: GetReminderSettingsRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val scheduleDailyReminderRepository: ScheduleDailyReminderRepository,
    private val cancelDailyReminderRepository: CancelDailyReminderRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, Unit>(coroutineContextProvider) {
    private val dailyReminderTime = DailyReminderTime()

    override fun executeInBackground(request: Unit) {
        val result = getReminderSettingsRepository.reminderSettings()
        val settings = (result as? ReminderSettingsResultDomainModel.Loaded)?.settings ?: return
        if (settings.dailyEnabled) {
            val now = getCurrentTimeRepository.currentTime()
            val at = dailyReminderTime.next(now, settings.dailyTime)
            scheduleDailyReminderRepository.scheduleDailyReminder(
                at,
                dailyReminderTime.dateOf(at, now.zone)
            )
        } else {
            cancelDailyReminderRepository.cancelDailyReminder()
        }
    }
}
