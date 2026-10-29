package com.migrely.app.review.domain.repository

/** How many migraines are logged and not deleted, or null when they cannot be read (BRD D130). */
interface GetMigraineCountRepository {
    fun migraineCount(): Int?
}
