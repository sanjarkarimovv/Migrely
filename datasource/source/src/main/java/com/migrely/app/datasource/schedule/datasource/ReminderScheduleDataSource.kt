package com.migrely.app.datasource.schedule.datasource

import java.time.Instant
import java.time.LocalDate

/**
 * Background runs for reminders (BRD D66, D67), at about the time asked for: the system may move them by some minutes
 * (EC-REM-03), and they survive a reboot (EC-REM-04).
 */
interface ReminderScheduleDataSource {
    /** Plans the daily reminder's run for [date] at about [at], replacing any run already planned. */
    fun scheduleDaily(at: Instant, date: LocalDate)

    fun cancelDaily()

    /** Plans the "Did it help?" run for intake [intakeId] at about [at], replacing one already planned for it. */
    fun scheduleEffectQuestion(intakeId: String, at: Instant)

    fun cancelEffectQuestion(intakeId: String)

    /** Plans the visit reminder's run for the visit on [visit] at about [at], replacing any run already planned. */
    fun scheduleVisit(at: Instant, visit: LocalDate)

    fun cancelVisit()

    /** Plans every reminder again as soon as possible, as after an import that changed what they depend on. */
    fun planAgain()

    /**
     * Cancels every run planned for reminders, of every kind, and returns once that is stored, so it holds even if the
     * app stops right after (BRD D96).
     */
    fun cancelAll()
}
