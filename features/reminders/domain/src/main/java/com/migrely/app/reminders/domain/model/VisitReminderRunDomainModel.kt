package com.migrely.app.reminders.domain.model

/** What one run of the visit reminder did (BRD D168, design D3). */
enum class VisitReminderRunDomainModel {
    Posted,

    /** An attack was in progress, so it waits for 10:00 the next day (EC-REM-12). */
    Postponed,
    Skipped,

    /** A read failed; the run is tried again. */
    Failed
}
