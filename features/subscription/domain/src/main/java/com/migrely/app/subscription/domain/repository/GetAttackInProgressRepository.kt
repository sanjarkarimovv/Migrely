package com.migrely.app.subscription.domain.repository

/** Null when it cannot be read. */
interface GetAttackInProgressRepository {
    fun attackInProgress(): Boolean?
}
