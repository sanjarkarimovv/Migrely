package com.migrely.app.home.data.repository

import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.attack.model.AttackStartDataModel
import com.migrely.app.datasource.attack.model.ReopenDataModel
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.exception.DataException
import com.migrely.app.datasource.exception.StorageFullDataException
import com.migrely.app.home.data.mapper.AttackDomainMapper
import com.migrely.app.home.domain.model.AttackLookupResultDomainModel
import com.migrely.app.home.domain.model.CurrentTimeDomainModel
import com.migrely.app.home.domain.model.IntensityPointDomainModel
import com.migrely.app.home.domain.model.LongAttackCheckDomainModel
import com.migrely.app.home.domain.model.PreviousEndResultDomainModel
import com.migrely.app.home.domain.model.ReopenResultDomainModel
import com.migrely.app.home.domain.model.StartAttackResultDomainModel
import com.migrely.app.home.domain.model.TodaySnapshotDomainModel
import com.migrely.app.home.domain.model.TodaySnapshotResultDomainModel
import com.migrely.app.home.domain.model.WriteResultDomainModel
import com.migrely.app.home.domain.repository.AddIntensityPointRepository
import com.migrely.app.home.domain.repository.DeleteAttackRepository
import com.migrely.app.home.domain.repository.EndAttackRepository
import com.migrely.app.home.domain.repository.GetAttackInProgressRepository
import com.migrely.app.home.domain.repository.GetCurrentTimeRepository
import com.migrely.app.home.domain.repository.GetPreviousAttackEndRepository
import com.migrely.app.home.domain.repository.GetTodayRepository
import com.migrely.app.home.domain.repository.ReopenAttackRepository
import com.migrely.app.home.domain.repository.ReplaceLatestIntensityPointRepository
import com.migrely.app.home.domain.repository.StartAttackRepository
import com.migrely.app.home.domain.repository.UpdateAttackStartRepository
import com.migrely.app.home.domain.repository.UpdateLongAttackCheckRepository
import java.time.Instant
import java.time.ZoneId

/**
 * Every function returns failures as values (docs/ARCHITECTURE.md section 7): datasource exceptions never reach the
 * background use cases.
 */
@Suppress("TooManyFunctions")
class HomeRepository(
    private val attackDataSource: AttackDataSource,
    private val clockDataSource: ClockDataSource,
    private val attackDomainMapper: AttackDomainMapper
) : GetTodayRepository,
    GetCurrentTimeRepository,
    StartAttackRepository,
    GetAttackInProgressRepository,
    AddIntensityPointRepository,
    ReplaceLatestIntensityPointRepository,
    GetPreviousAttackEndRepository,
    UpdateAttackStartRepository,
    EndAttackRepository,
    DeleteAttackRepository,
    ReopenAttackRepository,
    UpdateLongAttackCheckRepository {
    override fun today(): TodaySnapshotResultDomainModel = try {
        val now = clockDataSource.now()
        val zone = clockDataSource.zone()
        val (from, to) = dayBounds(now, zone)
        TodaySnapshotResultDomainModel.Loaded(
            TodaySnapshotDomainModel(
                attackInProgress = attackDataSource.inProgress()?.let(attackDomainMapper::toDomain),
                endedAttacksTouchingToday = attackDataSource.endedTouching(
                    from,
                    to
                ).map(attackDomainMapper::toDomain),
                now = now
            )
        )
    } catch (_: DataException) {
        TodaySnapshotResultDomainModel.Failed
    }

    override fun currentTime() =
        CurrentTimeDomainModel(instant = clockDataSource.now(), zone = clockDataSource.zone())

    override fun startAttackIfNoneInProgress(
        start: Instant,
        zone: ZoneId
    ): StartAttackResultDomainModel = try {
        when (val outcome = attackDataSource.startIfNoneInProgress(start, zone.id)) {
            is AttackStartDataModel.Started ->
                StartAttackResultDomainModel.Started(attackDomainMapper.toDomain(outcome.attack))

            is AttackStartDataModel.AlreadyInProgress ->
                StartAttackResultDomainModel.AlreadyInProgress(
                    attackDomainMapper.toDomain(outcome.attack)
                )
        }
    } catch (_: StorageFullDataException) {
        StartAttackResultDomainModel.StorageFull
    } catch (_: DataException) {
        StartAttackResultDomainModel.Failed
    }

    override fun attackInProgress(): AttackLookupResultDomainModel = try {
        attackDataSource.inProgress()
            ?.let { AttackLookupResultDomainModel.InProgress(attackDomainMapper.toDomain(it)) }
            ?: AttackLookupResultDomainModel.None
    } catch (_: DataException) {
        AttackLookupResultDomainModel.Failed
    }

    override fun addIntensityPoint(attackId: String, point: IntensityPointDomainModel) = write {
        attackDataSource.addIntensityPoint(attackId, point.at, point.value)
    }

    override fun replaceLatestIntensityPoint(attackId: String, point: IntensityPointDomainModel) =
        write {
            attackDataSource.replaceLatestIntensityPoint(attackId, point.at, point.value)
        }

    override fun previousAttackEnd(
        attackId: String,
        before: Instant
    ): PreviousEndResultDomainModel = try {
        attackDataSource.previousEndBefore(attackId, before)
            ?.let(PreviousEndResultDomainModel::Found)
            ?: PreviousEndResultDomainModel.None
    } catch (_: DataException) {
        PreviousEndResultDomainModel.Failed
    }

    override fun updateAttackStart(attackId: String, start: Instant) = write {
        attackDataSource.updateStart(attackId, start)
    }

    override fun endAttack(attackId: String, end: Instant) = write {
        attackDataSource.end(attackId, end)
    }

    override fun deleteAttack(attackId: String) = write { attackDataSource.delete(attackId) }

    override fun reopenAttack(attackId: String): ReopenResultDomainModel = try {
        when (attackDataSource.reopen(attackId)) {
            ReopenDataModel.Reopened -> ReopenResultDomainModel.Reopened
            ReopenDataModel.AnotherInProgress -> ReopenResultDomainModel.AnotherInProgress
        }
    } catch (_: StorageFullDataException) {
        ReopenResultDomainModel.StorageFull
    } catch (_: DataException) {
        ReopenResultDomainModel.Failed
    }

    override fun updateLongAttackCheck(attackId: String, check: LongAttackCheckDomainModel) =
        write {
            attackDataSource.updateLongAttackCheck(attackId, check.confirmedAt, check.disabled)
        }

    // Java 8 time APIs only: this code runs on Android 8 (API 26).
    private fun dayBounds(now: Instant, zone: ZoneId): Pair<Instant, Instant> {
        val date = now.atZone(zone).toLocalDate()
        return date.atStartOfDay(zone).toInstant() to
            date.plusDays(1).atStartOfDay(zone).toInstant()
    }

    private inline fun write(block: () -> Unit): WriteResultDomainModel = try {
        block()
        WriteResultDomainModel.Written
    } catch (_: StorageFullDataException) {
        WriteResultDomainModel.StorageFull
    } catch (_: DataException) {
        WriteResultDomainModel.Failed
    }
}
