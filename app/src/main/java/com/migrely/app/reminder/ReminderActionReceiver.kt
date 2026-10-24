package com.migrely.app.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.migrely.app.history.domain.model.DayMarkKindDomainModel
import com.migrely.app.history.domain.model.SetDayMarkRequestDomainModel
import com.migrely.app.history.domain.model.SetDayMarkResultDomainModel
import dagger.hilt.android.EntryPointAccessors
import kotlin.concurrent.thread

/**
 * "Pain-free day" on the daily reminder (BRD D66): it marks today by History's own rule, so a day an attack touches is
 * refused and the reminder says so instead (EC-DAY-01, EC-REM-06). It works without opening the app.
 */
class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_PAIN_FREE) return
        val entryPoint =
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                ReminderEntryPoint::class.java
            )
        val pending = goAsync()
        thread {
            try {
                val clock = entryPoint.clockDataSource()
                val today = clock.now().atZone(clock.zone()).toLocalDate()
                val result = entryPoint.setDayMarkUseCase().executeInBackground(
                    SetDayMarkRequestDomainModel(
                        today,
                        DayMarkKindDomainModel.PainFree,
                        marked = true
                    )
                )
                when (result) {
                    SetDayMarkResultDomainModel.AttackLogged ->
                        entryPoint.showAttackLoggedTodayUseCase().executeInBackground(Unit)
                    SetDayMarkResultDomainModel.Done ->
                        entryPoint.dismissDailyReminderUseCase().executeInBackground(Unit)
                    // A failure leaves the reminder up, so the user can try again or open the app.
                    SetDayMarkResultDomainModel.FutureDay,
                    SetDayMarkResultDomainModel.StorageFull,
                    SetDayMarkResultDomainModel.Failed -> Unit
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_PAIN_FREE = "com.migrely.app.reminder.PAIN_FREE"
    }
}
