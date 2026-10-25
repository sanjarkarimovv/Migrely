package com.migrely.app.subscription.domain.repository

import java.time.Instant

interface GetCurrentTimeRepository {
    fun now(): Instant
}
