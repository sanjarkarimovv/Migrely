package com.migrely.app.reminders.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.ReminderSettingsResultDomainModel
import com.migrely.app.reminders.domain.model.RemindersDomainModel
import com.migrely.app.reminders.domain.model.RemindersResultDomainModel
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.GetNotificationsAllowedRepository
import com.migrely.app.reminders.domain.repository.GetReminderSettingsRepository
import java.time.Duration

private val ASK_AGAIN_AFTER: Duration = Duration.ofDays(30)

/** The Reminders screen (BRD D65–D68): permission may be asked for again 30 days after it was last asked (EC-REM-02). */
class GetRemindersUseCase(
    private val getReminderSettingsRepository: GetReminderSettingsRepository,
    private val getNotificationsAllowedRepository: GetNotificationsAllowedRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, RemindersResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): RemindersResultDomainModel {
        val settings = when (val result = getReminderSettingsRepository.reminderSettings()) {
            is ReminderSettingsResultDomainModel.Loaded -> result.settings
            ReminderSettingsResultDomainModel.Failed -> return RemindersResultDomainModel.Failed
        }
        val now = getCurrentTimeRepository.currentTime().instant
        val askedAt = settings.permissionAskedAt
        return RemindersResultDomainModel.Loaded(
            RemindersDomainModel(
                dailyEnabled = settings.dailyEnabled,
                dailyTime = settings.dailyTime,
                effectEnabled = settings.effectEnabled,
                notificationsAllowed = getNotificationsAllowedRepository.notificationsAllowed(),
                mayAskPermission =
                askedAt == null || Duration.between(askedAt, now) >= ASK_AGAIN_AFTER
            )
        )
    }
}
