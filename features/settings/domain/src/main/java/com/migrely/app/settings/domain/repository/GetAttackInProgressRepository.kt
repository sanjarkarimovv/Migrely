package com.migrely.app.settings.domain.repository

import kotlinx.coroutines.flow.Flow

interface GetAttackInProgressRepository {
    /** Whether an attack is in progress, then again each time that changes. Never fails: unknown reads as not. */
    fun attackInProgress(): Flow<Boolean>
}
