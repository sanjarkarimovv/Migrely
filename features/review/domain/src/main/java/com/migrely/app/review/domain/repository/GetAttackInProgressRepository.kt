package com.migrely.app.review.domain.repository

/** Whether an attack is in progress, or null when it cannot be told (BRD D130). */
interface GetAttackInProgressRepository {
    fun attackInProgress(): Boolean?
}
