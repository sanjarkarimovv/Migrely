package com.migrely.app.datasource.billing.datasource

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Lets QA set the simulated store from adb, in debug builds only (BRD D113). It takes broadcasts
 * only from holders of android.permission.DUMP, which adb's shell has:
 *
 *     adb shell am broadcast -p com.migrely.app -f 0x20 \
 *         -a com.migrely.app.debug.SIMULATED_STORE --es command unavailable
 *
 * The -f 0x20 flag (include stopped packages) lets it reach the app after a force-stop too;
 * without it Android drops the broadcast silently.
 *
 * The commands are "available", the default; "no-products", a Google Play that connects but knows
 * none of Migrely's products, as before they are set up in Play Console; "unavailable", a country
 * without Google Play billing; "unreachable", a Play Store that does not answer; "complete-pending" and "decline-pending",
 * which settle a pending payment with no sheet open, as at a shop; and "reset", which empties the
 * store and gives the trial back.
 */
class SimulatedBillingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val command = intent.getStringExtra("command") ?: return
        simulatedStore(context).apply(command)
    }
}

/**
 * The one simulated store of the app: every instance reads and writes the same preferences file.
 */
fun simulatedStore(context: Context, sheet: SimulatedSheet = SimulatedSheet { _, _ -> null }) =
    SimulatedBillingDataSourceImpl(
        context = context.applicationContext,
        store = context.applicationContext.getSharedPreferences(
            "migrely_simulated_store",
            Context.MODE_PRIVATE
        ),
        sheet = sheet
    )
