package com.migrely.app.history.data.repository

import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.attack.model.RestoreDataModel
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.daymark.datasource.DayMarkDataSource
import com.migrely.app.datasource.exception.DataException
import com.migrely.app.datasource.exception.StorageFullDataException
import com.migrely.app.history.data.mapper.DayMarkDomainMapper
import com.migrely.app.history.data.mapper.HistoryAttackDomainMapper
import com.migrely.app.history.data.mapper.ZoneDomainMapper
import com.migrely.app.history.domain.model.AttacksTouchingResultDomainModel
import com.migrely.app.history.domain.model.CurrentTimeDomainModel
import com.migrely.app.history.domain.model.DayMarkKindDomainModel
import com.migrely.app.history.domain.model.DayMarkWriteResultDomainModel
import com.migrely.app.history.domain.model.DayMarksResultDomainModel
import com.migrely.app.history.domain.model.DeletedAttackDomainModel
import com.migrely.app.history.domain.model.EarliestAttackResultDomainModel
import com.migrely.app.history.domain.model.UndoResultDomainModel
import com.migrely.app.history.domain.repository.GetAttacksTouchingRepository
import com.migrely.app.history.domain.repository.GetCurrentTimeRepository
import com.migrely.app.history.domain.repository.GetDayMarksRepository
import com.migrely.app.history.domain.repository.GetEarliestAttackRepository
import com.migrely.app.history.domain.repository.GetRecentlyDeletedAttackRepository
import com.migrely.app.history.domain.repository.RestoreDeletedAttackRepository
import com.migrely.app.history.domain.repository.SetDayMarkRepository
import java.time.Instant
import java.time.LocalDate

/**
 * Every function returns failures as values (docs/ARCHITECTURE.md section 7): datasource exceptions never reach the
 * background use cases.
 */
@Suppress("TooManyFunctions")
class HistoryRepository(
    private val attackDataSource: AttackDataSource,
    private val clockDataSource: ClockDataSource,
    private val dayMarkDataSource: DayMarkDataSource,
    private val historyAttackDomainMapper: HistoryAttackDomainMapper,
    private val zoneDomainMapper: ZoneDomainMapper,
    private val dayMarkDomainMapper: DayMarkDomainMapper
) : GetCurrentTimeRepository,
    GetEarliestAttackRepository,
    GetAttacksTouchingRepository,
    GetRecentlyDeletedAttackRepository,
    RestoreDeletedAttackRepository,
    GetDayMarksRepository,
    SetDayMarkRepository {
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

    override fun dayMarks(from: LocalDate, to: LocalDate): DayMarksResultDomainModel = try {
        DayMarksResultDomainModel.Loaded(
            dayMarkDataSource.marksBetween(from, to).map(dayMarkDomainMapper::toDomain)
        )
    } catch (_: DataException) {
        DayMarksResultDomainModel.Failed
    }

    override fun setDayMark(
        date: LocalDate,
        kind: DayMarkKindDomainModel,
        marked: Boolean
    ): DayMarkWriteResultDomainModel = try {
        when (kind) {
            DayMarkKindDomainModel.Period -> dayMarkDataSource.setPeriod(date, marked)
            DayMarkKindDomainModel.PainFree -> dayMarkDataSource.setPainFree(date, marked)
        }
        DayMarkWriteResultDomainModel.Written
    } catch (_: StorageFullDataException) {
        DayMarkWriteResultDomainModel.StorageFull
    } catch (_: DataException) {
        DayMarkWriteResultDomainModel.Failed
    }
}
