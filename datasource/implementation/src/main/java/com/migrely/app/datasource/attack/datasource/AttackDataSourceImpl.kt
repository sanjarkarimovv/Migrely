package com.migrely.app.datasource.attack.datasource

import android.database.sqlite.SQLiteException
import com.migrely.app.datasource.attack.dao.AttackDao
import com.migrely.app.datasource.attack.mapper.AttackLocalMapper
import com.migrely.app.datasource.attack.mapper.AttackTagLocalMapper
import com.migrely.app.datasource.attack.model.AttackDataModel
import com.migrely.app.datasource.attack.model.AttackLocalModel
import com.migrely.app.datasource.attack.model.AttackStartDataModel
import com.migrely.app.datasource.attack.model.AttackTagDataModel
import com.migrely.app.datasource.attack.model.CreateEndedDataModel
import com.migrely.app.datasource.attack.model.DeletedAttackDataModel
import com.migrely.app.datasource.attack.model.EarliestAttackDataModel
import com.migrely.app.datasource.attack.model.IntensityPointDataModel
import com.migrely.app.datasource.attack.model.IntensityPointLocalModel
import com.migrely.app.datasource.attack.model.ReopenDataModel
import com.migrely.app.datasource.attack.model.RestoreDataModel
import com.migrely.app.datasource.database.database
import com.migrely.app.datasource.database.databaseFailures
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

private val VALID_INTENSITY = 1..10
private val VALID_IMPACT = 0..3
private const val MAXIMUM_INTENSITY = 10
private const val MAXIMUM_NOTE_LENGTH = 1_000

/** How long a deleted attack stays restorable: the longest Android accessibility timeout (BRD D23). */
internal val DELETED_RETENTION: Duration = Duration.ofMinutes(2)

/**
 * How far ahead of the clock a deletion may read, as after a small step back, and still count as the recent one; one
 * further ahead never hides a real deletion (#112).
 */
internal val DELETION_CLOCK_TOLERANCE: Duration = Duration.ofSeconds(2)

@Suppress("TooManyFunctions")
class AttackDataSourceImpl(
    private val attackDao: AttackDao,
    private val attackLocalMapper: AttackLocalMapper,
    private val attackTagLocalMapper: AttackTagLocalMapper = AttackTagLocalMapper(),
    private val clock: Clock = Clock.systemUTC(),
    private val generateId: () -> String = { UUID.randomUUID().toString() }
) : AttackDataSource {
    override fun inProgress(): AttackDataModel? = database {
        attackDao.inProgress()?.let(attackLocalMapper::toData)
    }

    override fun inProgressChanges(): Flow<Boolean> =
        attackDao.inProgressChanges().distinctUntilChanged().databaseFailures()

    override fun byId(attackId: String): AttackDataModel? = database {
        attackDao.byId(attackId)?.let(attackLocalMapper::toData)
    }

    override fun endedTouching(from: Instant, to: Instant): List<AttackDataModel> = database {
        attackDao.endedTouching(
            from.toEpochMilli(),
            to.toEpochMilli()
        ).map(attackLocalMapper::toData)
    }

    override fun earliestStart(): EarliestAttackDataModel? = database {
        attackDao.earliestStart()?.let { local ->
            EarliestAttackDataModel(Instant.ofEpochMilli(local.startEpochMillis), local.startZoneId)
        }
    }

    override fun overlapping(
        excludingId: String?,
        start: Instant,
        end: Instant
    ): List<AttackDataModel> = database {
        attackDao.overlappingAll(
            excludingId,
            start.toEpochMilli(),
            end.toEpochMilli()
        ).map(attackLocalMapper::toData)
    }

    override fun createEnded(start: Instant, zoneId: String, end: Instant): CreateEndedDataModel {
        require(end.isAfter(start)) { "An ended attack must end after it starts" }
        return database {
            val now = clock.millis()
            val attack = AttackLocalModel(
                id = generateId(),
                startEpochMillis = start.toEpochMilli(),
                startZoneId = zoneId,
                endEpochMillis = end.toEpochMilli(),
                longCheckConfirmedEpochMillis = null,
                longCheckDisabled = false,
                createdEpochMillis = now,
                updatedEpochMillis = now
            )
            if (attackDao.insertEndedIfNoOverlap(attack)) {
                CreateEndedDataModel.Created(attack.id)
            } else {
                CreateEndedDataModel.Overlapping
            }
        }
    }

    override fun merge(
        survivorId: String,
        absorbedIds: List<String>,
        start: Instant,
        startZoneId: String,
        end: Instant?,
        functionalImpact: Int?,
        note: String
    ) {
        require(survivorId !in absorbedIds) { "The surviving attack cannot also be absorbed" }
        require(functionalImpact == null || functionalImpact in VALID_IMPACT) {
            "Impact must be within $VALID_IMPACT, was $functionalImpact"
        }
        require(note.length <= MAXIMUM_NOTE_LENGTH) {
            "A note holds at most $MAXIMUM_NOTE_LENGTH characters"
        }
        database {
            attackDao.mergeInto(
                survivorId = survivorId,
                absorbedIds = absorbedIds,
                startEpochMillis = start.toEpochMilli(),
                startZoneId = startZoneId,
                endEpochMillis = end?.toEpochMilli(),
                impact = functionalImpact,
                note = note,
                nowEpochMillis = clock.millis()
            )
        }
    }

    override fun startIfNoneInProgress(start: Instant, zoneId: String): AttackStartDataModel =
        database {
            val now = clock.millis()
            val outcome = attackDao.startIfNoneInProgress(
                AttackLocalModel(
                    id = generateId(),
                    startEpochMillis = start.toEpochMilli(),
                    startZoneId = zoneId,
                    endEpochMillis = null,
                    longCheckConfirmedEpochMillis = null,
                    longCheckDisabled = false,
                    createdEpochMillis = now,
                    updatedEpochMillis = now
                )
            )
            val attack = attackLocalMapper.toData(outcome.attack)
            if (outcome.created) {
                AttackStartDataModel.Started(attack)
            } else {
                AttackStartDataModel.AlreadyInProgress(attack)
            }
        }

    override fun addIntensityPoint(attackId: String, at: Instant, value: Int) {
        requireValidIntensity(value)
        database { attackDao.insertIntensityPoint(intensityPoint(attackId, at, value)) }
    }

    override fun replaceLatestIntensityPoint(attackId: String, at: Instant, value: Int) {
        requireValidIntensity(value)
        database { attackDao.replaceLatestIntensityPoint(intensityPoint(attackId, at, value)) }
    }

    override fun removeIntensityPoint(attackId: String, point: IntensityPointDataModel) = database {
        attackDao.deleteIntensityPoint(attackId, point.at.toEpochMilli(), point.value)
    }

    override fun maximumRecorded(): Boolean = database {
        attackDao.anyIntensityPointOf(MAXIMUM_INTENSITY)
    }

    override fun updateStart(attackId: String, start: Instant) = database {
        attackDao.updateStart(attackId, start.toEpochMilli(), clock.millis())
    }

    override fun updateTimes(
        attackId: String,
        start: Instant,
        end: Instant?,
        pointsToRemove: List<IntensityPointDataModel>
    ) = database {
        attackDao.updateTimesRemovingPoints(
            attackId = attackId,
            startEpochMillis = start.toEpochMilli(),
            endEpochMillis = end?.toEpochMilli(),
            pointsToRemove = pointsToRemove.map { intensityPoint(attackId, it.at, it.value) },
            nowEpochMillis = clock.millis()
        )
    }

    override fun restoreTimes(
        attackId: String,
        start: Instant,
        end: Instant?,
        points: List<IntensityPointDataModel>
    ) {
        points.forEach { requireValidIntensity(it.value) }
        database {
            attackDao.restoreTimesWithPoints(
                attackId = attackId,
                startEpochMillis = start.toEpochMilli(),
                endEpochMillis = end?.toEpochMilli(),
                points = points.map { intensityPoint(attackId, it.at, it.value) },
                nowEpochMillis = clock.millis()
            )
        }
    }

    override fun previousEndBefore(attackId: String, before: Instant): Instant? = database {
        attackDao.previousEndBefore(attackId, before.toEpochMilli())?.let(Instant::ofEpochMilli)
    }

    override fun end(attackId: String, end: Instant) = database {
        attackDao.end(attackId, end.toEpochMilli(), clock.millis())
    }

    override fun reopen(attackId: String): ReopenDataModel = database {
        if (attackDao.reopenIfNothingOverlaps(attackId, clock.millis())) {
            ReopenDataModel.Reopened
        } else {
            ReopenDataModel.NotPossible
        }
    }

    override fun delete(attackId: String) = database { attackDao.discard(attackId) }

    override fun softDelete(attackId: String) = database {
        val now = clock.millis()
        purgeExpiredDeletionsQuietly(now)
        attackDao.softDelete(attackId, now)
    }

    override fun restore(attackId: String): RestoreDataModel = database {
        if (attackDao.restoreIfPossible(attackId, clock.millis())) {
            RestoreDataModel.Restored
        } else {
            RestoreDataModel.NotPossible
        }
    }

    override fun recentlyDeleted(since: Instant): DeletedAttackDataModel? = database {
        val now = clock.millis()
        purgeExpiredDeletionsQuietly(now)
        val cutoff = now - DELETED_RETENTION.toMillis()
        attackDao.recentlyDeleted(
            maxOf(since.toEpochMilli(), cutoff),
            now + DELETION_CLOCK_TOLERANCE.toMillis()
        )?.let { local ->
            local.deletedEpochMillis?.let { deletedAt ->
                DeletedAttackDataModel(local.id, Instant.ofEpochMilli(deletedAt))
            }
        }
    }

    override fun purgeExpiredDeletions() = database {
        purgeDeletions(clock.millis())
    }

    override fun updateLongAttackCheck(attackId: String, confirmedAt: Instant?, disabled: Boolean) =
        database {
            attackDao.updateLongCheck(
                attackId,
                confirmedAt?.toEpochMilli(),
                disabled,
                clock.millis()
            )
        }

    override fun setTag(attackId: String, tag: AttackTagDataModel, selected: Boolean) {
        val local = attackTagLocalMapper.toLocal(attackId, tag)
        database {
            if (selected) {
                attackDao.insertTag(local)
            } else {
                attackDao.deleteTag(local.attackId, local.kind, local.tagKey)
            }
        }
    }

    override fun setFunctionalImpact(attackId: String, impact: Int?) {
        require(impact == null || impact in VALID_IMPACT) {
            "Impact must be within $VALID_IMPACT, was $impact"
        }
        database { attackDao.setFunctionalImpact(attackId, impact, clock.millis()) }
    }

    override fun setNote(attackId: String, note: String) {
        require(note.length <= MAXIMUM_NOTE_LENGTH) {
            "A note holds at most $MAXIMUM_NOTE_LENGTH characters"
        }
        database { attackDao.setNote(attackId, note, clock.millis()) }
    }

    /**
     * Deletions past their undo window, and those stamped further ahead of the clock than the window, as after a clock
     * set back by days (#112). One a little ahead, as after an automatic correction of a few seconds, stays
     * restorable, so an Undo already offered still works.
     */
    private fun purgeDeletions(nowEpochMillis: Long) = attackDao.purgeDeletedOutside(
        nowEpochMillis - DELETED_RETENTION.toMillis(),
        nowEpochMillis + DELETED_RETENTION.toMillis()
    )

    /**
     * Housekeeping only: a purge that fails, for example when storage is full, must not stop the read or the deletion
     * it runs before. Rows it leaves behind are hidden anyway and are purged on a later call.
     */
    @Suppress("SwallowedException")
    private fun purgeExpiredDeletionsQuietly(nowEpochMillis: Long) {
        try {
            purgeDeletions(nowEpochMillis)
        } catch (exception: SQLiteException) {
            // Ignored on purpose; see above.
        }
    }

    private fun intensityPoint(attackId: String, at: Instant, value: Int) =
        IntensityPointLocalModel(
            attackId = attackId,
            atEpochMillis = at.toEpochMilli(),
            value = value
        )

    // Values outside 1..10 are a programming error, not a storage failure (Room cannot declare CHECK constraints).
    private fun requireValidIntensity(value: Int) = require(value in VALID_INTENSITY) {
        "Intensity must be within $VALID_INTENSITY, was $value"
    }
}
