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
 * (EC-REM-02), and the next doctor visit with whether its own channel is off. Only a visit after today may be removed:
 * on its day it stays as the last visit a report starts from, as the database keeps it (BRD EC-REP-04). "Today" is the
 * injected clock's, read again at every read, so a screen kept open past midnight or across a zone change follows it.
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
                today = today,
                nextVisit = visits.next?.date,
                visitRemovable = visits.next?.date?.isAfter(today) == true,
                visitChannelOff = getVisitChannelOffRepository.visitChannelOff()
            )
        )
    }
}
