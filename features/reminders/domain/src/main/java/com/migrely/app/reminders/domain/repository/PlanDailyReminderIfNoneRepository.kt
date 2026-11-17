package com.migrely.app.reminders.domain.repository

import java.time.Instant
import java.time.LocalDate

interface PlanDailyReminderIfNoneRepository {
    /**
     * Plans the daily reminder for [date] at about [at] only when no daily run is planned or running; one that is stays
     * as it is (BRD D176).
     */
    fun planDailyReminderIfNone(at: Instant, date: LocalDate)
}
