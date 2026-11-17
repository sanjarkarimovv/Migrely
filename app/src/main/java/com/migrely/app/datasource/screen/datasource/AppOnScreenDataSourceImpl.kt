package com.migrely.app.datasource.screen.datasource

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo

/**
 * Reads the importance the system gives this process, not the resumed activity (change keep-late-daily-reminder, design
 * D3): the system marks a process started for a top activity as foreground before any of the app's code runs, while no
 * activity has resumed yet, and lowers it when the screen goes dark or locks. Split screen and the notification shade
 * over Migrely keep it foreground.
 */
class AppOnScreenDataSourceImpl : AppOnScreenDataSource {
    override fun isOnScreen(): Boolean {
        // A new RunningAppProcessInfo says foreground; start from gone so an unfilled state posts the
        // reminder (cr-213). Only Robolectric leaves it unfilled: on a device the system always fills
        // importance, so this start value changes nothing there (cr-213b).
        val state = RunningAppProcessInfo().apply {
            importance = RunningAppProcessInfo.IMPORTANCE_GONE
        }
        ActivityManager.getMyMemoryState(state)
        return state.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
    }
}
