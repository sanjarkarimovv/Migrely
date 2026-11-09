package com.migrely.app.reminders.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.DoctorVisitsResultDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsResultDomainModel
import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import com.migrely.app.reminders.domain.model.SetDoctorVisitResultDomainModel
import com.migrely.app.reminders.domain.repository.CancelVisitReminderRepository
import com.migrely.app.reminders.domain.repository.DismissVisitReminderRepository
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.GetDoctorVisitsRepository
import com.migrely.app.reminders.domain.repository.GetNotificationsAllowedRepository
import com.migrely.app.reminders.domain.repository.GetReminderSettingsRepository
import com.migrely.app.reminders.domain.repository.ScheduleVisitReminderRepository
import com.migrely.app.reminders.domain.repository.SetNextVisitRepository
import com.migrely.app.reminders.domain.repository.SetVisitRemindedRepository
import java.time.LocalDate

private const val PICKABLE_DAYS = 365L

/**
 * Saves the next doctor visit at once (BRD REM-F4, EC-REM-07): a new date replaces the one set, and the same date
 * changes nothing (EC-REM-09). Only today to 365 days ahead can be set. The date is saved whatever notifications allow;
 * the result says whether permission may be asked for (EC-REM-11).
 */
@Suppress("LongParameterList")
class SetDoctorVisitUseCase(
    private val getReminderSettingsRepository: GetReminderSettingsRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val getDoctorVisitsRepository: GetDoctorVisitsRepository,
    private val getNotificationsAllowedRepository: GetNotificationsAllowedRepository,
    private val setNextVisitRepository: SetNextVisitRepository,
    setVisitRemindedRepository: SetVisitRemindedRepository,
    dismissVisitReminderRepository: DismissVisitReminderRepository,
    scheduleVisitReminderRepository: ScheduleVisitReminderRepository,
    cancelVisitReminderRepository: CancelVisitReminderRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<LocalDate, SetDoctorVisitResultDomainModel>(
    coroutineContextProvider
) {
    private val nextVisitSave = NextVisitSave(
        setVisitRemindedRepository,
        dismissVisitReminderRepository,
        VisitReminderPlan(scheduleVisitReminderRepository, cancelVisitReminderRepository)
    )

    override fun executeInBackground(request: LocalDate): SetDoctorVisitResultDomainModel {
        val settings = when (val result = getReminderSettingsRepository.reminderSettings()) {
            is ReminderSettingsResultDomainModel.Loaded -> result.settings
            ReminderSettingsResultDomainModel.Failed ->
                return SetDoctorVisitResultDomainModel.Failed
        }
        val now = getCurrentTimeRepository.currentTime()
        val today = now.instant.atZone(now.zone).toLocalDate()
        if (request.isBefore(today) || request.isAfter(today.plusDays(PICKABLE_DAYS))) {
            return SetDoctorVisitResultDomainModel.Failed
        }
        val visits = when (val result = getDoctorVisitsRepository.doctorVisits(today)) {
            is DoctorVisitsResultDomainModel.Loaded -> result
            DoctorVisitsResultDomainModel.Failed -> return SetDoctorVisitResultDomainModel.Failed
        }
        if (visits.next?.date == request) return SetDoctorVisitResultDomainModel.Unchanged
        val written = nextVisitSave.save(request, settings, now) {
            setNextVisitRepository.setNextVisit(request, today)
        }
        return when (written) {
            ReminderWriteResultDomainModel.Done -> SetDoctorVisitResultDomainModel.Saved(
                askForPermission = !getNotificationsAllowedRepository.notificationsAllowed() &&
                    PermissionAskRule.mayAsk(settings.permissionAskedAt, now.instant)
            )
            ReminderWriteResultDomainModel.StorageFull ->
                SetDoctorVisitResultDomainModel.StorageFull
            ReminderWriteResultDomainModel.Failed -> SetDoctorVisitResultDomainModel.Failed
        }
    }
}
