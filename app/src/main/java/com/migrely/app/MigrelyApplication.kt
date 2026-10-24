package com.migrely.app

import android.app.Application
import com.migrely.app.reminder.ReminderEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import kotlin.concurrent.thread

@HiltAndroidApp
class MigrelyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Keeps the daily reminder planned whatever happened while the app was not running: a restored backup brings
        // the settings back but not WorkManager's own records (BRD D66). Planning again is safe to repeat.
        thread {
            EntryPointAccessors.fromApplication(this, ReminderEntryPoint::class.java)
                .rescheduleRemindersUseCase()
                .executeInBackground(Unit)
        }
    }
}
