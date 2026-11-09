package com.migrely.app.reminders.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.DoctorVisitsResultDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsResultDomainModel
import com.migrely.app.reminders.domain.model.RemindersDomainModel
import com.migrely.app.reminders.domain.model.RemindersResultDomainModel
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.GetDoctorVisitsRepository
import com.migrely.app.reminders.domain.repository.GetNotificationsAllowedRepository
import com.migrely.app.reminders.domain.repository.GetReminderSettingsRepository
import com.migrely.app.reminders.domain.repository.GetVisitChannelOffRepository

/**
 * The Reminders screen (BRD D65–D68, D168): permission may be asked for again 30 days after it was last asked
 * (EC-REM-02), and the next doctor visit with whether its own channel is off.
 */
class GetRemindersUseCase(
    private val getReminderSettingsRepository: GetReminderSettingsRepository,
    private val getNotificationsAllowedRepository: GetNotificationsAllowedRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val getDoctorVisitsRepository: GetDoctorVisitsRepository,
    private val getVisitChannelOffRepository: GetVisitChannelOffRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, RemindersResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): RemindersResultDomainModel {
        val settings = when (val result = getReminderSettingsRepository.reminderSettings()) {
            is ReminderSettingsResultDomainModel.Loaded -> result.settings
            ReminderSettingsResultDomainModel.Failed -> return RemindersResultDomainModel.Failed
        }
        val now = getCurrentTimeRepository.currentTime()
        val today = now.instant.atZone(now.zone).toLocalDate()
        val visits = when (val result = getDoctorVisitsRepository.doctorVisits(today)) {
            is DoctorVisitsResultDomainModel.Loaded -> result
            DoctorVisitsResultDomainModel.Failed -> return RemindersResultDomainModel.Failed
        }
        return RemindersResultDomainModel.Loaded(
            RemindersDomainModel(
                dailyEnabled = settings.dailyEnabled,
                dailyTime = settings.dailyTime,
                effectEnabled = settings.effectEnabled,
                notificationsAllowed = getNotificationsAllowedRepository.notificationsAllowed(),
                mayAskPermission = PermissionAskRule.mayAsk(
                    settings.permissionAskedAt,
                    now.instant
                ),
                nextVisit = visits.next?.date,
                visitChannelOff = getVisitChannelOffRepository.visitChannelOff()
            )
        )
    }
}
