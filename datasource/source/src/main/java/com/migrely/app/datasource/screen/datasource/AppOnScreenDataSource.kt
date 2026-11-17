package com.migrely.app.datasource.screen.datasource

/** Whether Migrely is what the person sees (BRD D176, EC-REM-27). */
interface AppOnScreenDataSource {
    /**
     * Whether Migrely is on screen with the screen on, from the tap that starts it, before its first screen appears. A
     * dark or locked screen, or Migrely in the background, is not on screen.
     */
    fun isOnScreen(): Boolean
}
