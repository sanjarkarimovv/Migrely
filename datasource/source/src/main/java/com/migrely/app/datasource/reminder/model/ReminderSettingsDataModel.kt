package com.migrely.app.datasource.reminder.model

import java.time.Instant

/**
 * The user's reminder choices (BRD D65–D68): the daily reminder and its minute of the day, whether to ask if a
 * medication helped, and when notification permission was last asked for, if ever.
 */
data class ReminderSettingsDataModel(
    val dailyEnabled: Boolean,
    val dailyMinuteOfDay: Int,
    val effectEnabled: Boolean,
    val permissionAskedAt: Instant?
)
