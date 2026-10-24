package com.migrely.app.datasource.attack.datasource

import com.migrely.app.datasource.attack.model.AttackDataModel
import com.migrely.app.datasource.attack.model.AttackStartDataModel
import com.migrely.app.datasource.attack.model.ReopenDataModel
import java.time.Instant

/**
 * Attacks stored on the device. Every function blocks and must be called off the main thread.
 * Writes throw [com.migrely.app.datasource.exception.StorageFullDataException] when the device is full and
 * [com.migrely.app.datasource.exception.DatabaseDataException] for other storage failures.
 */
interface AttackDataSource {
    fun inProgress(): AttackDataModel?

    /** Ended attacks that overlap `[from, to)`. */
    fun endedTouching(from: Instant, to: Instant): List<AttackDataModel>

    /** Atomic: starts a new attack only when none is in progress. */
    fun startIfNoneInProgress(start: Instant, zoneId: String): AttackStartDataModel

    fun addIntensityPoint(attackId: String, at: Instant, value: Int)

    fun replaceLatestIntensityPoint(attackId: String, at: Instant, value: Int)

    fun updateStart(attackId: String, start: Instant)

    /** The latest end among other attacks that ended at or before [before], if any. */
    fun previousEndBefore(attackId: String, before: Instant): Instant?

    fun end(attackId: String, end: Instant)

    /** Atomic: reopens only when no other attack is in progress. */
    fun reopen(attackId: String): ReopenDataModel

    fun delete(attackId: String)

    fun updateLongAttackCheck(attackId: String, confirmedAt: Instant?, disabled: Boolean)
}
