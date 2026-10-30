package com.migrely.app.review.domain.repository

import java.time.Instant

/** The current time. */
interface GetCurrentTimeRepository {
    fun now(): Instant
}
