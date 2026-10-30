package com.migrely.app.home.data.repository

import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.attack.model.AttackStartDataModel
import com.migrely.app.datasource.attack.model.ReopenDataModel
import com.migrely.app.datasource.attack.model.RestoreDataModel
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.daymark.datasource.DayMarkDataSource
import com.migrely.app.datasource.exception.DataException
import com.migrely.app.datasource.exception.StorageFullDataException
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSource
import com.migrely.app.home.data.mapper.AttackDomainMapper
import com.migrely.app.home.domain.model.AttackLookupResultDomainModel
import com.migrely.app.home.domain.model.CurrentTimeDomainModel
import com.migrely.app.home.domain.model.DayMarksDomainModel
import com.migrely.app.home.domain.model.DayMarksResultDomainModel
import com.migrely.app.home.domain.model.DeletedAttackDomainModel
import com.migrely.app.home.domain.model.IntensityPointDomainModel
import com.migrely.app.home.domain.model.LongAttackCheckDomainModel
import com.migrely.app.home.domain.model.PreviousEndResultDomainModel
import com.migrely.app.home.domain.model.ReopenResultDomainModel
import com.migrely.app.home.domain.model.StartAttackResultDomainModel
import com.migrely.app.home.domain.model.TodaySnapshotDomainModel
import com.migrely.app.home.domain.model.TodaySnapshotResultDomainModel
import com.migrely.app.home.domain.model.UndoResultDomainModel
import com.migrely.app.home.domain.model.WriteResultDomainModel
import com.migrely.app.home.domain.repository.AddIntensityPointRepository
import com.migrely.app.home.domain.repository.DeleteAttackRepository
import com.migrely.app.home.domain.repository.EndAttackRepository
import com.migrely.app.home.domain.repository.GetAttackInProgressRepository
import com.migrely.app.home.domain.repository.GetCurrentTimeRepository
import com.migrely.app.home.domain.repository.GetDayMarksRepository
import com.migrely.app.home.domain.repository.GetPeriodTrackingRepository
import com.migrely.app.home.domain.repository.GetPreviousAttackEndRepository
import com.migrely.app.home.domain.repository.GetTodayRepository
import com.migrely.app.home.domain.repository.ReopenAttackRepository
import com.migrely.app.home.domain.repository.ReplaceLatestIntensityPointRepository
import com.migrely.app.home.domain.repository.RestoreDeletedAttackRepository
import com.migrely.app.home.domain.repository.SetPeriodRepository
import com.migrely.app.home.domain.repository.StartAttackRepository
import com.migrely.app.home.domain.repository.UpdateAttackStartRepository
import com.migrely.app.home.domain.repository.UpdateLongAttackCheckRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Every function returns failures as values (docs/ARCHITECTURE.md section 7): datasource exceptions never reach the
 * background use cases.
 */
@Suppress("TooManyFunctions")
class HomeRepository(
    private val attackDataSource: AttackDataSource,
    private val clockDataSource: ClockDataSource,
    private val dayMarkDataSource: DayMarkDataSource,
    private val preferencesDataSource: PreferencesDataSource,
    private val attackDomainMapper: AttackDomainMapper
) : GetTodayRepository,
    GetDayMarksRepository,
    SetPeriodRepository,
    GetPeriodTrackingRepository,
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
    UpdateLongAttackCheckRepository,
    RestoreDeletedAttackRepository {
    override fun today(from: Instant, to: Instant): TodaySnapshotResultDomainModel = try {
        TodaySnapshotResultDomainModel.Loaded(
            TodaySnapshotDomainModel(
                attackInProgress = attackDataSource.inProgress()?.let(attackDomainMapper::toDomain),
                endedAttacks = attackDataSource.endedTouching(from, to)
                    .map(attackDomainMapper::toDomain),
                // The datasource keeps a deletion restorable only for its retention window (BRD D23).
                recentlyDeleted = attackDataSource.recentlyDeleted(
                    since = Instant.EPOCH
                )?.let { deleted ->
                    DeletedAttackDomainModel(deleted.id, deleted.deletedAt)
                }
            )
        )
    } catch (_: DataException) {
        TodaySnapshotResultDomainModel.Failed
    }

    override fun dayMarks(date: LocalDate): DayMarksResultDomainModel = try {
        DayMarksResultDomainModel.Loaded(
            dayMarkDataSource.marksBetween(date, date).firstOrNull()
                ?.let { DayMarksDomainModel(it.period, it.painFree) }
                ?: DayMarksDomainModel(period = false, painFree = false)
        )
    } catch (_: DataException) {
        DayMarksResultDomainModel.Failed
    }

    override fun periodTracking(): Boolean = preferencesDataSource.periodTracking()

    override fun setPeriod(date: LocalDate, marked: Boolean) = write {
        dayMarkDataSource.setPeriod(date, marked)
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
            ReopenDataModel.NotPossible -> ReopenResultDomainModel.NotPossible
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

    override fun restoreDeletedAttack(attackId: String): UndoResultDomainModel = try {
        when (attackDataSource.restore(attackId)) {
            RestoreDataModel.Restored -> UndoResultDomainModel.Undone
            RestoreDataModel.NotPossible -> UndoResultDomainModel.NotPossible
        }
    } catch (_: DataException) {
        UndoResultDomainModel.Failed
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
