package com.migrely.app.datasource.schedule.datasource

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.schedule.datasource.ReminderScheduleDataSource
import com.migrely.app.reminder.ReminderWorker
import java.time.Duration
import java.time.Instant

/**
 * Reminders as WorkManager one-time work (BRD EC-REM-03): inexact by design, so no exact-alarm permission is needed,
 * and kept across reboots (EC-REM-04). Each kind is unique work, so planning again replaces the earlier run.
 */
class ReminderScheduleDataSourceImpl(
    private val workManager: WorkManager,
    private val clockDataSource: ClockDataSource
) : ReminderScheduleDataSource {
    override fun scheduleDaily(at: Instant) =
        enqueue(DAILY_WORK, at, ReminderWorker.KIND_DAILY, intakeId = null)

    override fun cancelDaily() {
        workManager.cancelUniqueWork(DAILY_WORK)
    }

    override fun scheduleEffectQuestion(intakeId: String, at: Instant) =
        enqueue(effectWork(intakeId), at, ReminderWorker.KIND_EFFECT, intakeId)

    override fun cancelEffectQuestion(intakeId: String) {
        workManager.cancelUniqueWork(effectWork(intakeId))
    }

    private fun enqueue(name: String, at: Instant, kind: String, intakeId: String?) {
        val delay = Duration.between(clockDataSource.now(), at).coerceAtLeast(Duration.ZERO)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay)
            .setInputData(
                workDataOf(
                    ReminderWorker.KEY_KIND to kind,
                    ReminderWorker.KEY_INTAKE_ID to intakeId
                )
            )
            .build()
        workManager.enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, request)
    }

    private fun effectWork(intakeId: String) = "$EFFECT_WORK_PREFIX$intakeId"

    private companion object {
        const val DAILY_WORK = "daily"
        const val EFFECT_WORK_PREFIX = "effect-"
    }
}
