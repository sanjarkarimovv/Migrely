package com.migrely.app.datasource.reminder.model

import java.time.Instant
import java.time.LocalDate

/**
 * The user's reminder choices (BRD D65–D68): the daily reminder and its minute of the day, whether to ask if a
 * medication helped, when notification permission was last asked for, and the last date the daily reminder showed.
 */
data class ReminderSettingsDataModel(
    val dailyEnabled: Boolean,
    val dailyMinuteOfDay: Int,
    val effectEnabled: Boolean,
    val permissionAskedAt: Instant?,
    val dailyLastShownOn: LocalDate?
)
