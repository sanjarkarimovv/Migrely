package com.migrely.app.reminders.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.ReminderSettingsResultDomainModel
import com.migrely.app.reminders.domain.repository.CancelDailyReminderRepository
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.GetReminderSettingsRepository
import com.migrely.app.reminders.domain.repository.ScheduleDailyReminderRepository

/**
 * Plans the daily reminder again by the device's local time, after a reboot or a change of time zone or clock (BRD
 * EC-REM-04, EC-REM-05). A reminder that is off stays unplanned.
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
            scheduleDailyReminderRepository.scheduleDailyReminder(
                dailyReminderTime.next(getCurrentTimeRepository.currentTime(), settings.dailyTime)
            )
        } else {
            cancelDailyReminderRepository.cancelDailyReminder()
        }
    }
}
