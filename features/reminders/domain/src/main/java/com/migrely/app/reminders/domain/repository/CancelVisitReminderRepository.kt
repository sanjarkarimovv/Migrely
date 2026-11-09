package com.migrely.app.reminders.domain.repository

interface CancelVisitReminderRepository {
    /** Cancels the planned run of the visit reminder. */
    fun cancelVisitReminder()
}
