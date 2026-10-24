package com.migrely.app.reminders.data.mapper

import java.time.DateTimeException
import java.time.ZoneId
import java.time.ZoneOffset

/** A zone id saved on another device may be unknown to this device's time-zone data; UTC keeps the instant correct. */
class ReminderZoneDomainMapper {
    @Suppress("SwallowedException")
    fun toDomain(zoneId: String): ZoneId = try {
        ZoneId.of(zoneId)
    } catch (exception: DateTimeException) {
        ZoneOffset.UTC
    }
}
