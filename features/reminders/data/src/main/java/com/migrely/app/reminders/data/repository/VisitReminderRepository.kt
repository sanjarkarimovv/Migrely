package com.migrely.app.reminders.data.repository

import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.doctorvisit.datasource.DoctorVisitDataSource
import com.migrely.app.datasource.exception.DataException
import com.migrely.app.datasource.exception.StorageFullDataException
import com.migrely.app.datasource.notification.datasource.NotificationDataSource
import com.migrely.app.datasource.reminder.datasource.ReminderSettingsDataSource
import com.migrely.app.datasource.schedule.datasource.ReminderScheduleDataSource
import com.migrely.app.reminders.data.mapper.DoctorVisitDomainMapper
import com.migrely.app.reminders.domain.model.AnythingLoggedDomainModel
import com.migrely.app.reminders.domain.model.AttackInProgressDomainModel
import com.migrely.app.reminders.domain.model.DoctorVisitDomainModel
import com.migrely.app.reminders.domain.model.DoctorVisitsResultDomainModel
import com.migrely.app.reminders.domain.model.RemoveDoctorVisitResultDomainModel
import com.migrely.app.reminders.domain.repository.CancelVisitReminderRepository
import com.migrely.app.reminders.domain.repository.DismissVisitReminderRepository
import com.migrely.app.reminders.domain.repository.GetAnythingLoggedRepository
import com.migrely.app.reminders.domain.repository.GetAttackInProgressRepository
import com.migrely.app.reminders.domain.repository.GetDoctorVisitsRepository
import com.migrely.app.reminders.domain.repository.GetVisitChannelOffRepository
import com.migrely.app.reminders.domain.repository.RemoveNextVisitRepository
import com.migrely.app.reminders.domain.repository.RestoreNextVisitRepository
import com.migrely.app.reminders.domain.repository.ScheduleVisitReminderRepository
import com.migrely.app.reminders.domain.repository.SetNextVisitRepository
import com.migrely.app.reminders.domain.repository.SetVisitCardClosedRepository
import com.migrely.app.reminders.domain.repository.SetVisitPostponedRepository
import com.migrely.app.reminders.domain.repository.SetVisitRemindedRepository
import com.migrely.app.reminders.domain.repository.ShowVisitReminderRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * The doctor visit and its reminder (BRD D168), over the shared datasources. Storage failures are values
 * (docs/ARCHITECTURE.md section 7); scheduling and notifications do not fail.
 */
@Suppress("TooManyFunctions")
class VisitReminderRepository(
    private val doctorVisitDataSource: DoctorVisitDataSource,
    private val reminderSettingsDataSource: ReminderSettingsDataSource,
    private val reminderScheduleDataSource: ReminderScheduleDataSource,
    private val notificationDataSource: NotificationDataSource,
    private val attackDataSource: AttackDataSource,
    private val doctorVisitDomainMapper: DoctorVisitDomainMapper
) : GetDoctorVisitsRepository,
    SetNextVisitRepository,
    RemoveNextVisitRepository,
    RestoreNextVisitRepository,
    SetVisitRemindedRepository,
    SetVisitCardClosedRepository,
    SetVisitPostponedRepository,
    ScheduleVisitReminderRepository,
    CancelVisitReminderRepository,
    DismissVisitReminderRepository,
    ShowVisitReminderRepository,
    GetAttackInProgressRepository,
    GetAnythingLoggedRepository,
    GetVisitChannelOffRepository {
    override fun doctorVisits(today: LocalDate): DoctorVisitsResultDomainModel = try {
        val visits = doctorVisitDataSource.visits(today)
        DoctorVisitsResultDomainModel.Loaded(
            next = visits.next?.let(doctorVisitDomainMapper::toDomain),
            last = visits.last
        )
    } catch (_: DataException) {
        DoctorVisitsResultDomainModel.Failed
    }

    override fun setNextVisit(date: LocalDate, today: LocalDate) = reminderWrite {
        doctorVisitDataSource.setNextVisit(date, today)
    }

    override fun removeNextVisit(today: LocalDate): RemoveDoctorVisitResultDomainModel = try {
        doctorVisitDataSource.removeNextVisit(today)
            ?.let {
                RemoveDoctorVisitResultDomainModel.Removed(doctorVisitDomainMapper.toDomain(it))
            }
            ?: RemoveDoctorVisitResultDomainModel.NothingToRemove
    } catch (_: StorageFullDataException) {
        RemoveDoctorVisitResultDomainModel.StorageFull
    } catch (_: DataException) {
        RemoveDoctorVisitResultDomainModel.Failed
    }

    override fun restoreNextVisit(visit: DoctorVisitDomainModel, today: LocalDate) = reminderWrite {
        doctorVisitDataSource.restoreNextVisit(doctorVisitDomainMapper.toData(visit), today)
    }

    override fun setVisitReminded(visit: LocalDate) = reminderWrite {
        reminderSettingsDataSource.setVisitReminded(visit)
    }

    override fun setVisitCardClosed(visit: LocalDate) = reminderWrite {
        reminderSettingsDataSource.setVisitCardClosed(visit)
    }

    override fun setVisitPostponed(day: LocalDate) = reminderWrite {
        reminderSettingsDataSource.setVisitPostponed(day)
    }

    override fun scheduleVisitReminder(at: Instant, visit: LocalDate) =
        reminderScheduleDataSource.scheduleVisit(at, visit)

    override fun cancelVisitReminder() = reminderScheduleDataSource.cancelVisit()

    override fun dismissVisitReminder() = notificationDataSource.cancelVisitReminder()

    override fun showVisitReminder(visit: LocalDate, until: Instant) =
        notificationDataSource.showVisitReminder(visit, until)

    override fun attackInProgress(): AttackInProgressDomainModel = try {
        if (attackDataSource.inProgress() == null) {
            AttackInProgressDomainModel.NotInProgress
        } else {
            AttackInProgressDomainModel.InProgress
        }
    } catch (_: DataException) {
        AttackInProgressDomainModel.Failed
    }

    // The period's days in the phone's zone, from the first day's start to the start of the day after the last.
    override fun anythingLogged(
        from: LocalDate,
        to: LocalDate,
        zone: ZoneId
    ): AnythingLoggedDomainModel = try {
        val logged = doctorVisitDataSource.anythingLoggedBetween(
            from.atStartOfDay(zone).toInstant(),
            to.plusDays(1).atStartOfDay(zone).toInstant(),
            from,
            to
        )
        if (logged) AnythingLoggedDomainModel.Logged else AnythingLoggedDomainModel.NothingLogged
    } catch (_: DataException) {
        AnythingLoggedDomainModel.Failed
    }

    override fun visitChannelOff(): Boolean = notificationDataSource.isVisitChannelOff()
}
