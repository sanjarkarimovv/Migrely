package com.migrely.app.reminders.domain.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * The Reminders screen (BRD D65–D68). [mayAskPermission] is false within 30 days of the last time permission was asked
 * for (EC-REM-02). [today] is the phone's date when read, so the screen never reads a clock of its own (issue #187).
 * [nextVisit] is the doctor visit set, [visitRemovable] says it is after [today], so "Remove" is offered, and
 * [visitChannelOff] says its channel is off (BRD D168).
 */
data class RemindersDomainModel(
    val dailyEnabled: Boolean,
    val dailyTime: LocalTime,
    val effectEnabled: Boolean,
    val notificationsAllowed: Boolean,
    val mayAskPermission: Boolean,
    val today: LocalDate,
    val nextVisit: LocalDate? = null,
    val visitRemovable: Boolean = false,
    val visitChannelOff: Boolean = false
)
