package com.migrely.app.reminders.domain.model

import java.time.LocalTime

/**
 * The Reminders screen (BRD D65–D68). [mayAskPermission] is false within 30 days of the last time permission was asked
 * for (EC-REM-02).
 */
data class RemindersDomainModel(
    val dailyEnabled: Boolean,
    val dailyTime: LocalTime,
    val effectEnabled: Boolean,
    val notificationsAllowed: Boolean,
    val mayAskPermission: Boolean
)
