package com.migrely.app.medication.data.mapper

import java.time.DateTimeException
import java.time.ZoneId
import java.time.ZoneOffset

/** A zone id saved on another device may be unknown here; UTC keeps the instant correct (BRD EC-TIM-02). */
class ZoneDomainMapper {
    @Suppress("SwallowedException")
    fun toDomain(zoneId: String): ZoneId = try {
        ZoneId.of(zoneId)
    } catch (exception: DateTimeException) {
        ZoneOffset.UTC
    }
}
