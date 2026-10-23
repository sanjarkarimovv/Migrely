package com.migrely.app.datasource.attack.datasource

import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteFullException
import com.migrely.app.datasource.attack.dao.AttackDao
import com.migrely.app.datasource.attack.mapper.AttackLocalMapper
import com.migrely.app.datasource.attack.model.AttackDataModel
import com.migrely.app.datasource.attack.model.AttackLocalModel
import com.migrely.app.datasource.attack.model.AttackStartDataModel
import com.migrely.app.datasource.attack.model.IntensityPointLocalModel
import com.migrely.app.datasource.attack.model.ReopenDataModel
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.exception.StorageFullDataException
import java.time.Clock
import java.time.Instant
import java.util.UUID

private val VALID_INTENSITY = 1..10

class AttackDataSourceImpl(
    private val attackDao: AttackDao,
    private val attackLocalMapper: AttackLocalMapper,
    private val clock: Clock = Clock.systemUTC(),
    private val generateId: () -> String = { UUID.randomUUID().toString() }
) : AttackDataSource {
    override fun inProgress(): AttackDataModel? = database {
        attackDao.inProgress()?.let(attackLocalMapper::toData)
    }

    override fun endedTouching(from: Instant, to: Instant): List<AttackDataModel> = database {
        attackDao.endedTouching(
            from.toEpochMilli(),
            to.toEpochMilli()
        ).map(attackLocalMapper::toData)
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

    override fun updateStart(attackId: String, start: Instant) = database {
        attackDao.updateStart(attackId, start.toEpochMilli(), clock.millis())
    }

    override fun previousEndBefore(attackId: String, before: Instant): Instant? = database {
        attackDao.previousEndBefore(attackId, before.toEpochMilli())?.let(Instant::ofEpochMilli)
    }

    override fun end(attackId: String, end: Instant) = database {
        attackDao.end(attackId, end.toEpochMilli(), clock.millis())
    }

    override fun reopen(attackId: String): ReopenDataModel = database {
        if (attackDao.reopenIfNoneInProgress(attackId, clock.millis())) {
            ReopenDataModel.Reopened
        } else {
            ReopenDataModel.AnotherInProgress
        }
    }

    override fun delete(attackId: String) = database { attackDao.delete(attackId) }

    override fun updateLongAttackCheck(attackId: String, confirmedAt: Instant?, disabled: Boolean) =
        database {
            attackDao.updateLongCheck(
                attackId,
                confirmedAt?.toEpochMilli(),
                disabled,
                clock.millis()
            )
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

    @Suppress("SwallowedException")
    private inline fun <T> database(block: () -> T): T = try {
        block()
    } catch (exception: SQLiteFullException) {
        throw StorageFullDataException(exception)
    } catch (exception: SQLiteException) {
        throw DatabaseDataException(exception)
    }
}
