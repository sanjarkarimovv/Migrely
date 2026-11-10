package com.migrely.app.reminders.domain.repository

interface DismissVisitReminderRepository {
    /** Takes away the visit notification if it shows. */
    fun dismissVisitReminder()
}
