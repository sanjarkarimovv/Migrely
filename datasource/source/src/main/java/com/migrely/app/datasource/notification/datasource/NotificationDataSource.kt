package com.migrely.app.datasource.notification.datasource

/** The notifications Migrely shows (BRD D66, D67). Nothing shows while notifications are off for the app. */
interface NotificationDataSource {
    /** Whether the user allows Migrely's notifications. */
    fun areEnabled(): Boolean

    /** "How was today?" with "Pain-free day" and "Log a migraine". */
    fun showDailyReminder()

    /** Replaces the daily reminder after "Pain-free day" on a day with an attack (BRD EC-REM-06). */
    fun showAttackLoggedToday()

    fun cancelDailyReminder()

    /** "Did [medicationName] help?" for intake [intakeId]; tapping it opens home. */
    fun showEffectQuestion(intakeId: String, medicationName: String)

    fun cancelEffectQuestion(intakeId: String)
}
