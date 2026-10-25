package com.migrely.app.subscription.domain.repository

/** The attacks logged and not deleted, or null when they cannot be read. */
interface GetMigraineCountRepository {
    fun migraineCount(): Int?
}
