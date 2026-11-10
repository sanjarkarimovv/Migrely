package com.migrely.app.reminders.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.RemoveDoctorVisitResultDomainModel
import com.migrely.app.reminders.domain.repository.CancelVisitReminderRepository
import com.migrely.app.reminders.domain.repository.DismissVisitReminderRepository
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.RemoveNextVisitRepository

/**
 * "Remove" takes the next visit away at once, with its planned reminder and its notification (BRD EC-REM-10). Its card
 * goes with it, since the card shows only a next visit. The removed visit comes back with Undo.
 */
class RemoveDoctorVisitUseCase(
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val removeNextVisitRepository: RemoveNextVisitRepository,
    private val cancelVisitReminderRepository: CancelVisitReminderRepository,
    private val dismissVisitReminderRepository: DismissVisitReminderRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, RemoveDoctorVisitResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): RemoveDoctorVisitResultDomainModel {
        val now = getCurrentTimeRepository.currentTime()
        val result = removeNextVisitRepository.removeNextVisit(
            now.instant.atZone(now.zone).toLocalDate()
        )
        if (result is RemoveDoctorVisitResultDomainModel.Removed) {
            cancelVisitReminderRepository.cancelVisitReminder()
            dismissVisitReminderRepository.dismissVisitReminder()
        }
        return result
    }
}
