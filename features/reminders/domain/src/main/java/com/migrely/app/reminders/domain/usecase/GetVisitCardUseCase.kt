package com.migrely.app.reminders.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.AnythingLoggedDomainModel
import com.migrely.app.reminders.domain.model.DoctorVisitsResultDomainModel
import com.migrely.app.reminders.domain.model.ReminderSettingsResultDomainModel
import com.migrely.app.reminders.domain.model.VisitCardDomainModel
import com.migrely.app.reminders.domain.repository.GetAnythingLoggedRepository
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.GetDoctorVisitsRepository
import com.migrely.app.reminders.domain.repository.GetReminderSettingsRepository

/**
 * Home's visit card (BRD D168, design D8): from 10:00 two days before the next visit until its day ends, unless it was
 * tapped or closed for that date or nothing was logged in the report's default period (EC-REM-13). It does not wait
 * for an attack's postponement: home shows attack mode during one, and the card once it ends. A failed read shows
 * nothing, since the card is only a reminder.
 */
class GetVisitCardUseCase(
    private val getReminderSettingsRepository: GetReminderSettingsRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val getDoctorVisitsRepository: GetDoctorVisitsRepository,
    private val getAnythingLoggedRepository: GetAnythingLoggedRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, VisitCardDomainModel>(coroutineContextProvider) {
    private val visitReminderTime = VisitReminderTime()

    override fun executeInBackground(request: Unit): VisitCardDomainModel {
        val settings = (
            getReminderSettingsRepository.reminderSettings() as?
                ReminderSettingsResultDomainModel.Loaded
            )?.settings ?: return VisitCardDomainModel.Hidden
        val now = getCurrentTimeRepository.currentTime()
        val today = now.instant.atZone(now.zone).toLocalDate()
        val visits = getDoctorVisitsRepository.doctorVisits(today) as?
            DoctorVisitsResultDomainModel.Loaded ?: return VisitCardDomainModel.Hidden
        val next = visits.next?.date ?: return VisitCardDomainModel.Hidden
        val due = settings.visitCardClosedFor != next &&
            visitReminderTime.tooClose(next, now.instant, now.zone) &&
            getAnythingLoggedRepository.anythingLogged(
                DefaultReportPeriod.firstDay(visits.last, today),
                today,
                now.zone
            ) == AnythingLoggedDomainModel.Logged
        return if (due) VisitCardDomainModel.Shown(next) else VisitCardDomainModel.Hidden
    }
}
