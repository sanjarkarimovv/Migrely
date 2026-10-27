package com.migrely.app.datasource.notification.datasource

import java.time.LocalDate

/** The notifications Migrely shows (BRD D66, D67). Nothing shows while notifications are off for the app. */
interface NotificationDataSource {
    /** Whether the user allows Migrely's notifications. */
    fun areEnabled(): Boolean

    /** "How was today?" about [date], with "Pain-free day" (which marks [date]) and "Log a migraine". */
    fun showDailyReminder(date: LocalDate)

    /** Replaces the daily reminder after "Pain-free day" on a day with an attack (BRD EC-REM-06). */
    fun showAttackLoggedToday()

    fun cancelDailyReminder()

    /** "Did [medicationName] help?" for intake [intakeId]; tapping it opens home. */
    fun showEffectQuestion(intakeId: String, medicationName: String)

    fun cancelEffectQuestion(intakeId: String)

    /** Removes every notification Migrely shows (BRD D96). */
    fun cancelAll()
}
