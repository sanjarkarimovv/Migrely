package com.migrely.app.datasource.schedule.datasource

import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.reminder.ReminderWorker
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.ExecutionException

/**
 * Reminders as WorkManager one-time work (BRD EC-REM-03): inexact by design, so no exact-alarm permission is needed,
 * and kept across reboots (EC-REM-04). Each kind is unique work, so planning again replaces the earlier run.
 */
class ReminderScheduleDataSourceImpl(
    private val workManager: WorkManager,
    private val clockDataSource: ClockDataSource
) : ReminderScheduleDataSource {
    override fun scheduleDaily(at: Instant, date: LocalDate) = enqueue(
        DAILY_WORK,
        at,
        workDataOf(
            ReminderWorker.KEY_KIND to ReminderWorker.KIND_DAILY,
            ReminderWorker.KEY_DATE to date.toString()
        )
    )

    override fun cancelDaily() {
        workManager.cancelUniqueWork(DAILY_WORK)
    }

    override fun scheduleEffectQuestion(intakeId: String, at: Instant) = enqueue(
        effectWork(intakeId),
        at,
        workDataOf(
            ReminderWorker.KEY_KIND to ReminderWorker.KIND_EFFECT,
            ReminderWorker.KEY_INTAKE_ID to intakeId
        )
    )

    override fun cancelEffectQuestion(intakeId: String) {
        workManager.cancelUniqueWork(effectWork(intakeId))
    }

    // WorkManager tags every request with its worker's class name, so this also reaches runs planned by older
    // versions. Waiting for the result makes the cancellation stored before the app may stop.
    override fun cancelAll() {
        try {
            workManager.cancelAllWorkByTag(ReminderWorker::class.java.name).result.get()
        } catch (exception: ExecutionException) {
            throw DatabaseDataException(exception)
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            throw DatabaseDataException(exception)
        }
    }

    // REPLACE also covers the daily run planning its own next run: WorkManager marks the running work cancelled but
    // never interrupts a Worker's doWork, which finishes normally (androidx.work.Worker).
    private fun enqueue(name: String, at: Instant, input: Data) {
        val delay = Duration.between(clockDataSource.now(), at).coerceAtLeast(Duration.ZERO)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay)
            .setInputData(input)
            .build()
        workManager.enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, request)
    }

    private fun effectWork(intakeId: String) = "$EFFECT_WORK_PREFIX$intakeId"

    private companion object {
        const val DAILY_WORK = "daily"
        const val EFFECT_WORK_PREFIX = "effect-"
    }
}
