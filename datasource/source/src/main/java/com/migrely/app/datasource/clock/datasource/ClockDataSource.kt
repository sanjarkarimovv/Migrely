package com.migrely.app.datasource.clock.datasource

import java.time.Instant
import java.time.ZoneId

interface ClockDataSource {
    fun now(): Instant

    /** The device's current time zone, read at every call so travel is picked up. */
    fun zone(): ZoneId
}
