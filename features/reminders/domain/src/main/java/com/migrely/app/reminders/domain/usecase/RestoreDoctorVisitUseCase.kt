package com.migrely.app.reminders.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.DoctorVisitDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsResultDomainModel
import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import com.migrely.app.reminders.domain.repository.CancelVisitReminderRepository
import com.migrely.app.reminders.domain.repository.DismissVisitReminderRepository
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.GetReminderSettingsRepository
import com.migrely.app.reminders.domain.repository.RestoreNextVisitRepository
import com.migrely.app.reminders.domain.repository.ScheduleVisitReminderRepository
import com.migrely.app.reminders.domain.repository.SetVisitRemindedRepository

/**
 * Undo of "Remove" (BRD EC-REM-10): the visit comes back by the same path as a new date, so a reminder time that passed
 * meanwhile sends nothing, and only the home card shows it (design D4).
 */
@Suppress("LongParameterList")
class RestoreDoctorVisitUseCase(
    private val getReminderSettingsRepository: GetReminderSettingsRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val restoreNextVisitRepository: RestoreNextVisitRepository,
    setVisitRemindedRepository: SetVisitRemindedRepository,
    dismissVisitReminderRepository: DismissVisitReminderRepository,
    scheduleVisitReminderRepository: ScheduleVisitReminderRepository,
    cancelVisitReminderRepository: CancelVisitReminderRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<DoctorVisitDomainModel, ReminderWriteResultDomainModel>(
    coroutineContextProvider
) {
    private val nextVisitSave = NextVisitSave(
        setVisitRemindedRepository,
        dismissVisitReminderRepository,
        VisitReminderPlan(scheduleVisitReminderRepository, cancelVisitReminderRepository)
    )

    override fun executeInBackground(
        request: DoctorVisitDomainModel
    ): ReminderWriteResultDomainModel {
        val settings = when (val result = getReminderSettingsRepository.reminderSettings()) {
            is ReminderSettingsResultDomainModel.Loaded -> result.settings
            ReminderSettingsResultDomainModel.Failed -> return ReminderWriteResultDomainModel.Failed
        }
        val now = getCurrentTimeRepository.currentTime()
        // A visit day that ended while Undo was offered comes back as the last visit, and plans nothing.
        return nextVisitSave.save(request.date, settings, now) {
            restoreNextVisitRepository.restoreNextVisit(
                request,
                now.instant.atZone(now.zone).toLocalDate()
            )
        }
    }
}
