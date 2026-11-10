package com.migrely.app.reminders.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * What the user chose for reminders (BRD D65–D68), and when the daily reminder last showed. The visit fields hold a
 * visit's date (BRD D168, design D2): [visitRemindedFor] needs no notification any more, [visitCardClosedFor] had its
 * home card tapped or closed, and [visitPostponedTo] is the day whose 10:00 the visit reminder waits for after an
 * attack. Keyed by date, so a new visit date starts clean.
 */
data class ReminderSettingsDomainModel(
    val dailyEnabled: Boolean,
    val dailyTime: LocalTime,
    val effectEnabled: Boolean,
    val permissionAskedAt: Instant?,
    val dailyLastShownOn: LocalDate?,
    val visitRemindedFor: LocalDate? = null,
    val visitCardClosedFor: LocalDate? = null,
    val visitPostponedTo: LocalDate? = null
)
