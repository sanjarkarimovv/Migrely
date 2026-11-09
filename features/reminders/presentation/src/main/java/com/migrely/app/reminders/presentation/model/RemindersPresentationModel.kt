package com.migrely.app.reminders.presentation.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * The Reminders screen (BRD D65–D68, D168): [today] is the phone's date when read, [nextVisit] the doctor visit set, if
 * any, and [visitRemovable] whether "Remove" is offered for it.
 */
data class RemindersPresentationModel(
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
