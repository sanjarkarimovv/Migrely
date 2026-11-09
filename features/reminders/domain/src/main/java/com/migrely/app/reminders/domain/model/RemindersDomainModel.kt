package com.migrely.app.reminders.domain.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * The Reminders screen (BRD D65–D68). [mayAskPermission] is false within 30 days of the last time permission was asked
 * for (EC-REM-02). [nextVisit] is the doctor visit set, and [visitChannelOff] says its channel is off (BRD D168).
 */
data class RemindersDomainModel(
    val dailyEnabled: Boolean,
    val dailyTime: LocalTime,
    val effectEnabled: Boolean,
    val notificationsAllowed: Boolean,
    val mayAskPermission: Boolean,
    val nextVisit: LocalDate? = null,
    val visitChannelOff: Boolean = false
)
