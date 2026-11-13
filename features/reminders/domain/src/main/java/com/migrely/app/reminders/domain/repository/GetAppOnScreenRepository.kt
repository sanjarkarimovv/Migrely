package com.migrely.app.reminders.domain.repository

interface GetAppOnScreenRepository {
    /** Whether Migrely is on screen with the screen on, including while it is opening (BRD D176, EC-REM-27). */
    fun appOnScreen(): Boolean
}
