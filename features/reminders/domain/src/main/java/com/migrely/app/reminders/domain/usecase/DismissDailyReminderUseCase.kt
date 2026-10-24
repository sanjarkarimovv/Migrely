package com.migrely.app.reminders.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.repository.DismissDailyReminderRepository

/** Takes the daily reminder away once it is answered from the notification (BRD D66). */
class DismissDailyReminderUseCase(
    private val dismissDailyReminderRepository: DismissDailyReminderRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, Unit>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit) =
        dismissDailyReminderRepository.dismissDailyReminder()
}
