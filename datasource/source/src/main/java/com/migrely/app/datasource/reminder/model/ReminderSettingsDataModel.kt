package com.migrely.app.datasource.reminder.model

import java.time.Instant
import java.time.LocalDate

/**
 * The user's reminder choices (BRD D65–D68): the daily reminder and its minute of the day, whether to ask if a
 * medication helped, when notification permission was last asked for, and the last date the daily reminder showed.
 * The visit fields are device state keyed by a visit's date (BRD D168, design D2): its notification needs no showing,
 * its home card was closed, and the day whose 10:00 its reminder waits for after an attack.
 */
data class ReminderSettingsDataModel(
    val dailyEnabled: Boolean,
    val dailyMinuteOfDay: Int,
    val effectEnabled: Boolean,
    val permissionAskedAt: Instant?,
    val dailyLastShownOn: LocalDate?,
    val visitRemindedFor: LocalDate? = null,
    val visitCardClosedFor: LocalDate? = null,
    val visitPostponedTo: LocalDate? = null
)
