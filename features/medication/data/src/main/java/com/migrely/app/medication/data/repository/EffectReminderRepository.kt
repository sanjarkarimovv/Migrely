package com.migrely.app.medication.data.repository

import com.migrely.app.datasource.exception.DataException
import com.migrely.app.datasource.notification.datasource.NotificationDataSource
import com.migrely.app.datasource.reminder.datasource.ReminderSettingsDataSource
import com.migrely.app.datasource.schedule.datasource.ReminderScheduleDataSource
import com.migrely.app.medication.domain.repository.CancelEffectQuestionRepository
import com.migrely.app.medication.domain.repository.GetEffectReminderEnabledRepository
import com.migrely.app.medication.domain.repository.GetNotificationsAllowedRepository
import com.migrely.app.medication.domain.repository.ScheduleEffectQuestionRepository
import com.migrely.app.medication.domain.repository.ShowEffectQuestionRepository
import java.time.Instant

/** The "Did it help?" notification's planning and showing (BRD D67), over the shared reminder datasources. */
class EffectReminderRepository(
    private val reminderScheduleDataSource: ReminderScheduleDataSource,
    private val notificationDataSource: NotificationDataSource,
    private val reminderSettingsDataSource: ReminderSettingsDataSource
) : ScheduleEffectQuestionRepository,
    CancelEffectQuestionRepository,
    GetEffectReminderEnabledRepository,
    GetNotificationsAllowedRepository,
    ShowEffectQuestionRepository {
    override fun scheduleEffectQuestion(intakeId: String, at: Instant) =
        reminderScheduleDataSource.scheduleEffectQuestion(intakeId, at)

    override fun cancelEffectQuestion(intakeId: String) {
        reminderScheduleDataSource.cancelEffectQuestion(intakeId)
        notificationDataSource.cancelEffectQuestion(intakeId)
    }

    override fun effectReminderEnabled(): Boolean = try {
        reminderSettingsDataSource.settings().effectEnabled
    } catch (_: DataException) {
        false
    }

    override fun notificationsAllowed(): Boolean = notificationDataSource.areEnabled()

    override fun showEffectQuestion(intakeId: String, medicationName: String) =
        notificationDataSource.showEffectQuestion(intakeId, medicationName)
}
