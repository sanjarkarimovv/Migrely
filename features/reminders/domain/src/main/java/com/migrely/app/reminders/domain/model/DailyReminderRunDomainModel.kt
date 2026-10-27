package com.migrely.app.reminders.domain.model

/** What one run of the daily reminder did (BRD D66). */
enum class DailyReminderRunDomainModel {
    Shown,
    Skipped,
    Failed
}
