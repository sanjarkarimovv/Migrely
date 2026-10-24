package com.migrely.app.reminders.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import com.migrely.app.reminders.domain.model.SetDailyReminderRequestDomainModel
import com.migrely.app.reminders.domain.repository.CancelDailyReminderRepository
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.GetNotificationsAllowedRepository
import com.migrely.app.reminders.domain.repository.ScheduleDailyReminderRepository
import com.migrely.app.reminders.domain.repository.SetDailyReminderRepository

private const val MINUTES_PER_HOUR = 60

/**
 * Turns the daily reminder on or off, or moves it (BRD D66). Once saved, an enabled reminder with notifications allowed
 * is planned for its next time; otherwise nothing stays planned.
 */
class SetDailyReminderUseCase(
    private val setDailyReminderRepository: SetDailyReminderRepository,
    private val getNotificationsAllowedRepository: GetNotificationsAllowedRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val scheduleDailyReminderRepository: ScheduleDailyReminderRepository,
    private val cancelDailyReminderRepository: CancelDailyReminderRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<SetDailyReminderRequestDomainModel, ReminderWriteResultDomainModel>(
    coroutineContextProvider
) {
    private val dailyReminderTime = DailyReminderTime()

    override fun executeInBackground(
        request: SetDailyReminderRequestDomainModel
    ): ReminderWriteResultDomainModel {
        val minuteOfDay = request.time.hour * MINUTES_PER_HOUR + request.time.minute
        val result = setDailyReminderRepository.setDailyReminder(request.enabled, minuteOfDay)
        if (result != ReminderWriteResultDomainModel.Done) return result
        if (request.enabled && getNotificationsAllowedRepository.notificationsAllowed()) {
            scheduleDailyReminderRepository.scheduleDailyReminder(
                dailyReminderTime.next(getCurrentTimeRepository.currentTime(), request.time)
            )
        } else {
            cancelDailyReminderRepository.cancelDailyReminder()
        }
        return result
    }
}
