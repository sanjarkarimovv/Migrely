package com.migrely.app

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.migrely.app.activity.CurrentActivityHolder
import com.migrely.app.locale.FormattingLocales
import com.migrely.app.premium.PurchaseUpdatesWatcher
import com.migrely.app.reminder.ReminderEntryPoint
import com.migrely.app.reminders.domain.model.RescheduleRemindersRequestDomainModel
import com.migrely.app.start.StartEntryPoint
import com.migrely.app.storage.CodeCacheMark
import com.migrely.app.storage.ContextCodeCacheFolder
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlin.concurrent.thread

private const val LOG_TAG = "Migrely"

@HiltAndroidApp
class MigrelyApplication : Application() {
    @Inject
    lateinit var currentActivityHolder: CurrentActivityHolder

    @Inject
    lateinit var purchaseUpdatesWatcher: PurchaseUpdatesWatcher

    private val formattingLocales = FormattingLocales()

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // Before any content provider makes a folder, so an update after Clear storage empties only `code_cache`, never
        // the diary or the reminders (BRD D170, EC-STO-06). Runs at every process start, whatever component starts it.
        CodeCacheMark(ContextCodeCacheFolder(base)) { Log.w(LOG_TAG, it) }.keep()
    }

    override fun onCreate() {
        // Dates and times in English with the phone's region (BRD D117), before anything formats one.
        formattingLocales.applyToProcess(resources.configuration)
        super.onCreate()
        // Google Play's purchase sheet and messages open over the activity on screen (design D1).
        registerActivityLifecycleCallbacks(currentActivityHolder)
        // A payment completed while the app runs unlocks Premium at once (review I1).
        purchaseUpdatesWatcher.start()
        // The reminders are planned here, at every process start, whatever starts it: a restored backup or a lost
        // WorkManager store brings the settings back but no planned run (BRD D66, D175, EC-REM-22). A daily run already
        // planned is kept, even a late one, so the start never loses today's reminder (D176, EC-REM-24).
        thread {
            EntryPointAccessors.fromApplication(this, ReminderEntryPoint::class.java)
                .rescheduleRemindersUseCase()
                .executeInBackground(RescheduleRemindersRequestDomainModel.AppStart)
            val start = EntryPointAccessors.fromApplication(this, StartEntryPoint::class.java)
            // A report cut off by the app closing leaves a partial file; it goes now (BRD EC-REP-03).
            start.cleanUpReportsUseCase().executeInBackground(Unit)
            // A deleted attack whose undo has run out goes for good now, not with the next deletion (BRD D23, D118).
            start.purgeExpiredDeletionsUseCase().executeInBackground(Unit)
        }
    }

    /** The system language can change while the app runs; the default follows it (BRD D117). */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        formattingLocales.applyToProcess(newConfig)
    }
}
