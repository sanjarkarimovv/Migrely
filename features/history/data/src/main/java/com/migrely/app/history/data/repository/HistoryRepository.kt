package com.migrely.app.history.data.repository

import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.attack.model.RestoreDataModel
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.exception.DataException
import com.migrely.app.history.data.mapper.HistoryAttackDomainMapper
import com.migrely.app.history.data.mapper.ZoneDomainMapper
import com.migrely.app.history.domain.model.AttacksTouchingResultDomainModel
import com.migrely.app.history.domain.model.CurrentTimeDomainModel
import com.migrely.app.history.domain.model.DeletedAttackDomainModel
import com.migrely.app.history.domain.model.EarliestAttackResultDomainModel
import com.migrely.app.history.domain.model.UndoResultDomainModel
import com.migrely.app.history.domain.repository.GetAttacksTouchingRepository
import com.migrely.app.history.domain.repository.GetCurrentTimeRepository
import com.migrely.app.history.domain.repository.GetEarliestAttackRepository
import com.migrely.app.history.domain.repository.GetRecentlyDeletedAttackRepository
import com.migrely.app.history.domain.repository.RestoreDeletedAttackRepository
import java.time.Instant

/**
 * Every function returns failures as values (docs/ARCHITECTURE.md section 7): datasource exceptions never reach the
 * background use cases.
 */
class HistoryRepository(
    private val attackDataSource: AttackDataSource,
    private val clockDataSource: ClockDataSource,
    private val historyAttackDomainMapper: HistoryAttackDomainMapper,
    private val zoneDomainMapper: ZoneDomainMapper
) : GetCurrentTimeRepository,
    GetEarliestAttackRepository,
    GetAttacksTouchingRepository,
    GetRecentlyDeletedAttackRepository,
    RestoreDeletedAttackRepository {
    override fun currentTime() =
        CurrentTimeDomainModel(instant = clockDataSource.now(), zone = clockDataSource.zone())

    override fun earliestAttack(): EarliestAttackResultDomainModel = try {
        attackDataSource.earliestStart()?.let { earliest ->
            EarliestAttackResultDomainModel.Found(
                start = earliest.start,
                zone = zoneDomainMapper.toDomain(earliest.startZoneId)
            )
        } ?: EarliestAttackResultDomainModel.None
    } catch (_: DataException) {
        EarliestAttackResultDomainModel.Failed
    }

    // An attack in progress is open-ended in the overlap read, so it is included.
    override fun attacksTouching(from: Instant, to: Instant): AttacksTouchingResultDomainModel =
        try {
            AttacksTouchingResultDomainModel.Loaded(
                attackDataSource.overlapping(excludingId = null, start = from, end = to)
                    .map(historyAttackDomainMapper::toDomain)
            )
        } catch (_: DataException) {
            AttacksTouchingResultDomainModel.Failed
        }

    // The datasource keeps a deletion restorable only for its retention window (BRD D23).
    override fun recentlyDeletedAttack(): DeletedAttackDomainModel? = try {
        attackDataSource.recentlyDeleted(since = Instant.EPOCH)?.let { deleted ->
            DeletedAttackDomainModel(deleted.id, deleted.deletedAt)
        }
    } catch (_: DataException) {
        null
    }

    override fun restoreDeletedAttack(attackId: String): UndoResultDomainModel = try {
        when (attackDataSource.restore(attackId)) {
            RestoreDataModel.Restored -> UndoResultDomainModel.Undone
            RestoreDataModel.NotPossible -> UndoResultDomainModel.NotPossible
        }
    } catch (_: DataException) {
        UndoResultDomainModel.Failed
    }
}
