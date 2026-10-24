package com.migrely.app.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.EntryPointAccessors
import kotlin.concurrent.thread

/**
 * Plans the daily reminder again by the new local time when the time zone or the clock changes (BRD EC-REM-05). Both
 * broadcasts may be received by a manifest receiver on every Android version.
 */
class TimeChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_TIMEZONE_CHANGED &&
            intent.action != Intent.ACTION_TIME_CHANGED
        ) {
            return
        }
        val entryPoint =
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                ReminderEntryPoint::class.java
            )
        val pending = goAsync()
        thread {
            try {
                entryPoint.rescheduleRemindersUseCase().executeInBackground(Unit)
            } finally {
                pending.finish()
            }
        }
    }
}
