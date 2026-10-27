package com.migrely.app.reminders.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import com.migrely.app.reminders.domain.repository.SetEffectReminderRepository

/** Turns "Ask if a medication helped" on or off (BRD D67); each run checks it, so nothing needs cancelling here. */
class SetEffectReminderUseCase(
    private val setEffectReminderRepository: SetEffectReminderRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Boolean, ReminderWriteResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Boolean): ReminderWriteResultDomainModel =
        setEffectReminderRepository.setEffectReminder(request)
}
