package com.migrely.app.subscription.domain.repository

import java.time.ZoneId

/** The phone's time zone, read at every call so travel is picked up. */
interface GetTimeZoneRepository {
    fun zone(): ZoneId
}
