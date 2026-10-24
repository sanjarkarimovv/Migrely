package com.migrely.app.reminders.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/** What the user chose for reminders (BRD D65–D68), and when the daily reminder last showed. */
data class ReminderSettingsDomainModel(
    val dailyEnabled: Boolean,
    val dailyTime: LocalTime,
    val effectEnabled: Boolean,
    val permissionAskedAt: Instant?,
    val dailyLastShownOn: LocalDate?
)
