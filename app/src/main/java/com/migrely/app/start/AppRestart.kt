package com.migrely.app.start

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.migrely.app.MainActivity
import kotlin.system.exitProcess

/**
 * Starts the app again in a new process, after all data is deleted (BRD D96). App-wide objects, such as the view
 * models kept for the whole app, hold nothing from before; the app then finds no records and no finished onboarding,
 * and shows onboarding.
 */
fun restartApp(context: Context) {
    context.startActivity(
        Intent.makeRestartActivityTask(ComponentName(context, MainActivity::class.java))
    )
    exitProcess(0)
}
