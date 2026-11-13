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
        val state = RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(state)
        return state.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
    }
}
