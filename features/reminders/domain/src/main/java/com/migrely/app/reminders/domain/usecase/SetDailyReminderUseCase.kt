package com.migrely.app.reminders.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import com.migrely.app.reminders.domain.model.SetDailyReminderRequestDomainModel
import com.migrely.app.reminders.domain.repository.CancelDailyReminderRepository
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.ScheduleDailyReminderRepository
import com.migrely.app.reminders.domain.repository.SetDailyReminderRepository

/**
 * Turns the daily reminder on or off, or moves it (BRD D66). Once saved, an enabled reminder is planned for its next
 * time, even while notifications are off: each run checks them, so allowing them later needs nothing more.
 */
class SetDailyReminderUseCase(
    private val setDailyReminderRepository: SetDailyReminderRepository,
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
        val result = setDailyReminderRepository.setDailyReminder(request.enabled, request.time)
        if (result != ReminderWriteResultDomainModel.Done) return result
        if (request.enabled) {
            val now = getCurrentTimeRepository.currentTime()
            val at = dailyReminderTime.next(now, request.time)
            scheduleDailyReminderRepository.scheduleDailyReminder(
                at,
                dailyReminderTime.dateOf(at, now.zone)
            )
        } else {
            cancelDailyReminderRepository.cancelDailyReminder()
        }
        return result
    }
}
