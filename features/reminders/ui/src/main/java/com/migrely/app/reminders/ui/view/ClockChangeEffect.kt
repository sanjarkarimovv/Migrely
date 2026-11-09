package com.migrely.app.reminders.ui.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleStartEffect

/**
 * Calls [onChange] while the screen is started each time the phone's date may have moved: at midnight, when the time is
 * set or when the zone changes (issue #187). It reads no clock: the view model reads the injected one again. A stopped
 * screen hears nothing, since it reads again when it resumes.
 */
@Composable
internal fun ClockChangeEffect(onChange: () -> Unit) {
    val context = LocalContext.current
    val currentOnChange by rememberUpdatedState(onChange)
    LifecycleStartEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) = currentOnChange()
        }
        val changes = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        // System broadcasts still arrive at a receiver that is not exported.
        ContextCompat.registerReceiver(
            context,
            receiver,
            changes,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onStopOrDispose { context.unregisterReceiver(receiver) }
    }
}
