package com.migrely.app.reminders.domain.model

import java.time.Instant
import java.time.ZoneId

/** An attack, as the daily reminder needs it: [end] is null while it is in progress. */
data class ReminderAttackDomainModel(val start: Instant, val startZone: ZoneId, val end: Instant?)
