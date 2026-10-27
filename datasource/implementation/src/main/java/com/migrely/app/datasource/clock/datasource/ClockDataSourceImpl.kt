package com.migrely.app.datasource.clock.datasource

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/** [currentClock] is read at every call so that a time-zone change (travel) is picked up immediately. */
class ClockDataSourceImpl(private val currentClock: () -> Clock = { Clock.systemDefaultZone() }) :
    ClockDataSource {
    override fun now(): Instant = currentClock().instant()

    override fun zone(): ZoneId = currentClock().zone
}
