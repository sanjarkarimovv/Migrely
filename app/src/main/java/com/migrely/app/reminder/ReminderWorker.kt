package com.migrely.app.reminder

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.migrely.app.reminders.domain.model.DailyReminderRunDomainModel
import dagger.hilt.android.EntryPointAccessors
import java.time.LocalDate

/** Runs a planned reminder on WorkManager's background thread (BRD D66, D67). */
class ReminderWorker(context: Context, parameters: WorkerParameters) : Worker(context, parameters) {
    private val entryPoint =
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            ReminderEntryPoint::class.java
        )

    override fun doWork(): Result = when (inputData.getString(KEY_KIND)) {
        KIND_DAILY -> {
            val date = inputData.getString(KEY_DATE)?.let(LocalDate::parse)
            // A failed read keeps the chain alive: WorkManager runs this again with back-off.
            if (entryPoint.runDailyReminderUseCase().executeInBackground(date) ==
                DailyReminderRunDomainModel.Failed
            ) {
                Result.retry()
            } else {
                Result.success()
            }
        }
        KIND_EFFECT -> {
            inputData.getString(
                KEY_INTAKE_ID
            )?.let(entryPoint.runEffectQuestionUseCase()::executeInBackground)
            Result.success()
        }
        else -> Result.success()
    }

    companion object {
        const val KEY_KIND = "kind"
        const val KEY_INTAKE_ID = "intakeId"
        const val KEY_DATE = "date"
        const val KIND_DAILY = "daily"
        const val KIND_EFFECT = "effect"
    }
}
