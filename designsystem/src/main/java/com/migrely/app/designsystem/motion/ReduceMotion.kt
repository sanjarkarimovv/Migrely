package com.migrely.app.designsystem.motion

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect

/**
 * True when the system "Remove animations" setting is on (animator scale 0). Re-read on every resume, because the user
 * can change it while the app is in the background (BRD EC-UX-08).
 */
@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    var reduceMotion by remember { mutableStateOf(context.animationsRemoved()) }
    LifecycleResumeEffect(context) {
        reduceMotion = context.animationsRemoved()
        onPauseOrDispose { }
    }
    return reduceMotion
}

private fun Context.animationsRemoved(): Boolean =
    Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
