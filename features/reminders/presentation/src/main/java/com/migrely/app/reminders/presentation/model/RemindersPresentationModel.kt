package com.migrely.app.reminders.presentation.model

import java.time.LocalTime

/** The Reminders screen (BRD D65–D68). */
data class RemindersPresentationModel(
    val dailyEnabled: Boolean,
    val dailyTime: LocalTime,
    val effectEnabled: Boolean,
    val notificationsAllowed: Boolean,
    val mayAskPermission: Boolean
)
