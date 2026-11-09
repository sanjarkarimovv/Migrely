package com.migrely.app.reminders.data.repository

import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.attack.model.AttackDataModel
import com.migrely.app.datasource.doctorvisit.datasource.DoctorVisitDataSource
import com.migrely.app.datasource.doctorvisit.model.DoctorVisitDataModel
import com.migrely.app.datasource.doctorvisit.model.DoctorVisitsDataModel
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.exception.StorageFullDataException
import com.migrely.app.datasource.notification.datasource.NotificationDataSource
import com.migrely.app.datasource.reminder.datasource.ReminderSettingsDataSource
import com.migrely.app.datasource.schedule.datasource.ReminderScheduleDataSource
import com.migrely.app.reminders.data.mapper.DoctorVisitDomainMapper
import com.migrely.app.reminders.domain.model.AnythingLoggedDomainModel
import com.migrely.app.reminders.domain.model.AttackInProgressDomainModel
import com.migrely.app.reminders.domain.model.DoctorVisitDomainModel
import com.migrely.app.reminders.domain.model.DoctorVisitsResultDomainModel
import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import com.migrely.app.reminders.domain.model.RemoveDoctorVisitResultDomainModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

private val TODAY: LocalDate = LocalDate.of(2026, 11, 18)
private val NOVEMBER_20: LocalDate = LocalDate.of(2026, 11, 20)
private val TASHKENT = ZoneId.of("Asia/Tashkent")

class VisitReminderRepositoryTest {
    private val doctorVisitDataSource: DoctorVisitDataSource = mock()
    private val reminderSettingsDataSource: ReminderSettingsDataSource = mock()
    private val reminderScheduleDataSource: ReminderScheduleDataSource = mock()
    private val notificationDataSource: NotificationDataSource = mock()
    private val attackDataSource: AttackDataSource = mock()
    private val classUnderTest = VisitReminderRepository(
        doctorVisitDataSource,
        reminderSettingsDataSource,
        reminderScheduleDataSource,
        notificationDataSource,
        attackDataSource,
        DoctorVisitDomainMapper()
    )

    @Test
    fun `When the visits are read then the next and the last, or a failure`() {
        given(doctorVisitDataSource.visits(TODAY)).willReturn(
            DoctorVisitsDataModel(DoctorVisitDataModel("v", NOVEMBER_20), LocalDate.of(2026, 8, 20))
        )
        assertEquals(
            DoctorVisitsResultDomainModel.Loaded(
                DoctorVisitDomainModel("v", NOVEMBER_20),
                LocalDate.of(2026, 8, 20)
            ),
            classUnderTest.doctorVisits(TODAY)
        )

        given(
            doctorVisitDataSource.visits(TODAY)
        ).willThrow(DatabaseDataException(RuntimeException()))
        assertEquals(DoctorVisitsResultDomainModel.Failed, classUnderTest.doctorVisits(TODAY))
    }

    @Test
    fun `When the next visit is removed then the removed one, nothing, or a storage failure`() {
        given(
            doctorVisitDataSource.removeNextVisit(TODAY)
        ).willReturn(DoctorVisitDataModel("v", NOVEMBER_20))
        assertEquals(
            RemoveDoctorVisitResultDomainModel.Removed(DoctorVisitDomainModel("v", NOVEMBER_20)),
            classUnderTest.removeNextVisit(TODAY)
        )

        given(doctorVisitDataSource.removeNextVisit(TODAY)).willReturn(null)
        assertEquals(
            RemoveDoctorVisitResultDomainModel.NothingToRemove,
            classUnderTest.removeNextVisit(TODAY)
        )

        given(
            doctorVisitDataSource.removeNextVisit(TODAY)
        ).willThrow(StorageFullDataException(RuntimeException()))
        assertEquals(
            RemoveDoctorVisitResultDomainModel.StorageFull,
            classUnderTest.removeNextVisit(TODAY)
        )
    }

    @Test
    fun `Given a full phone when a visit is set then storage full`() {
        given(
            doctorVisitDataSource.setNextVisit(any(), any())
        ).willThrow(StorageFullDataException(RuntimeException()))

        assertEquals(
            ReminderWriteResultDomainModel.StorageFull,
            classUnderTest.setNextVisit(NOVEMBER_20, TODAY)
        )
    }

    @Test
    fun `When asked whether anything was logged then the period's days in the phone's zone`() {
        val from = LocalDate.of(2026, 8, 20)
        given(
            doctorVisitDataSource.anythingLoggedBetween(
                LocalDateTime.parse("2026-08-20T00:00").atZone(TASHKENT).toInstant(),
                LocalDateTime.parse("2026-11-19T00:00").atZone(TASHKENT).toInstant(),
                from,
                TODAY
            )
        ).willReturn(true)

        assertEquals(
            AnythingLoggedDomainModel.Logged,
            classUnderTest.anythingLogged(from, TODAY, TASHKENT)
        )
        assertEquals(
            AnythingLoggedDomainModel.NothingLogged,
            classUnderTest.anythingLogged(from.plusDays(1), TODAY, TASHKENT)
        )
    }

    @Test
    fun `When asked about an attack in progress then in progress, none, or a failure`() {
        given(attackDataSource.inProgress()).willReturn(mock<AttackDataModel>())
        assertEquals(AttackInProgressDomainModel.InProgress, classUnderTest.attackInProgress())

        given(attackDataSource.inProgress()).willReturn(null)
        assertEquals(AttackInProgressDomainModel.NotInProgress, classUnderTest.attackInProgress())

        given(attackDataSource.inProgress()).willThrow(DatabaseDataException(RuntimeException()))
        assertEquals(AttackInProgressDomainModel.Failed, classUnderTest.attackInProgress())
    }

    @Test
    fun `When the reminder is planned, shown or taken away then the shared datasources do it`() {
        val until = LocalDateTime.parse("2026-11-21T00:00").atZone(TASHKENT).toInstant()

        classUnderTest.scheduleVisitReminder(until, NOVEMBER_20)
        classUnderTest.showVisitReminder(NOVEMBER_20, until)
        classUnderTest.cancelVisitReminder()
        classUnderTest.dismissVisitReminder()

        verify(reminderScheduleDataSource).scheduleVisit(until, NOVEMBER_20)
        verify(notificationDataSource).showVisitReminder(NOVEMBER_20, until)
        verify(reminderScheduleDataSource).cancelVisit()
        verify(notificationDataSource).cancelVisitReminder()
    }
}
