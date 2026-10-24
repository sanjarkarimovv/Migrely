package com.migrely.app.reminder

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.hilt.android.EntryPointAccessors

/** Runs a planned reminder on WorkManager's background thread (BRD D66, D67). */
class ReminderWorker(context: Context, parameters: WorkerParameters) : Worker(context, parameters) {
    private val entryPoint =
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            ReminderEntryPoint::class.java
        )

    override fun doWork(): Result {
        when (inputData.getString(KEY_KIND)) {
            KIND_DAILY -> entryPoint.runDailyReminderUseCase().executeInBackground(Unit)
            KIND_EFFECT -> entryPoint.runEffectQuestionUseCase().executeInBackground(Unit)
            else -> Unit
        }
        return Result.success()
    }

    companion object {
        const val KEY_KIND = "kind"
        const val KEY_INTAKE_ID = "intakeId"
        const val KIND_DAILY = "daily"
        const val KIND_EFFECT = "effect"
    }
}
