package com.migrely.app.reminders.presentation.model

import java.time.LocalDate
import java.time.LocalTime

/** The Reminders screen (BRD D65–D68, D168): [nextVisit] is the doctor visit set, if any. */
data class RemindersPresentationModel(
    val dailyEnabled: Boolean,
    val dailyTime: LocalTime,
    val effectEnabled: Boolean,
    val notificationsAllowed: Boolean,
    val mayAskPermission: Boolean,
    val nextVisit: LocalDate? = null,
    val visitChannelOff: Boolean = false
)
