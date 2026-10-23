package com.migrely.app.attack.data.repository

import com.migrely.app.attack.data.mapper.AttackDetailsDomainResolver
import com.migrely.app.attack.data.mapper.AttackTagDataMapper
import com.migrely.app.attack.domain.model.AttackDetailsLookupResultDomainModel
import com.migrely.app.attack.domain.model.AttackTagDomainModel
import com.migrely.app.attack.domain.model.CurrentTimeDomainModel
import com.migrely.app.attack.domain.model.FunctionalImpactDomainModel
import com.migrely.app.attack.domain.model.IntensityPointDomainModel
import com.migrely.app.attack.domain.model.MaximumRecordedResultDomainModel
import com.migrely.app.attack.domain.model.OverlapLookupResultDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.repository.AddIntensityPointRepository
import com.migrely.app.attack.domain.repository.DeleteAttackRepository
import com.migrely.app.attack.domain.repository.FindOverlappingAttackRepository
import com.migrely.app.attack.domain.repository.GetAttackDetailsRepository
import com.migrely.app.attack.domain.repository.GetCurrentTimeRepository
import com.migrely.app.attack.domain.repository.GetMaximumRecordedRepository
import com.migrely.app.attack.domain.repository.RemoveIntensityPointRepository
import com.migrely.app.attack.domain.repository.RestoreAttackTimesRepository
import com.migrely.app.attack.domain.repository.SelectAttackTagRepository
import com.migrely.app.attack.domain.repository.SetFunctionalImpactRepository
import com.migrely.app.attack.domain.repository.UpdateAttackNoteRepository
import com.migrely.app.attack.domain.repository.UpdateAttackTimesRepository
import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.attack.model.AttackTagDataModel
import com.migrely.app.datasource.attack.model.IntensityPointDataModel
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.exception.DataException
import com.migrely.app.datasource.exception.StorageFullDataException
import com.migrely.app.datasource.tag.datasource.TagDataSource
import java.time.Instant

/**
 * The attack details screen's storage. Every function returns failures as values (docs/ARCHITECTURE.md section 7):
 * datasource exceptions never reach the background use cases.
 */
@Suppress("TooManyFunctions")
class AttackDetailsRepository(
    private val attackDataSource: AttackDataSource,
    private val tagDataSource: TagDataSource,
    private val clockDataSource: ClockDataSource,
    private val attackDetailsDomainResolver: AttackDetailsDomainResolver,
    private val attackTagDataMapper: AttackTagDataMapper
) : GetAttackDetailsRepository,
    GetCurrentTimeRepository,
    FindOverlappingAttackRepository,
    UpdateAttackTimesRepository,
    RestoreAttackTimesRepository,
    AddIntensityPointRepository,
    GetMaximumRecordedRepository,
    RemoveIntensityPointRepository,
    SelectAttackTagRepository,
    SetFunctionalImpactRepository,
    UpdateAttackNoteRepository,
    DeleteAttackRepository {
    override fun attackDetails(attackId: String): AttackDetailsLookupResultDomainModel = try {
        val attack = attackDataSource.byId(attackId)
        if (attack == null) {
            AttackDetailsLookupResultDomainModel.NotFound
        } else {
            val customIds = attack.tags.mapNotNull { tag ->
                when (tag) {
                    is AttackTagDataModel.CustomSymptom -> tag.tagId
                    is AttackTagDataModel.CustomTrigger -> tag.tagId
                    is AttackTagDataModel.Location,
                    is AttackTagDataModel.BuiltInSymptom,
                    is AttackTagDataModel.BuiltInTrigger -> null
                }
            }
            AttackDetailsLookupResultDomainModel.Found(
                attackDetailsDomainResolver.toDomain(attack, tagDataSource.customTags(customIds))
            )
        }
    } catch (_: DataException) {
        AttackDetailsLookupResultDomainModel.Failed
    }

    override fun currentTime() = CurrentTimeDomainModel(
        clockDataSource.now(),
        clockDataSource.zone()
    )

    override fun overlappingAttack(
        attackId: String,
        start: Instant,
        end: Instant
    ): OverlapLookupResultDomainModel = try {
        attackDataSource.overlapping(attackId, start, end)
            ?.let { other -> OverlapLookupResultDomainModel.Found(other.start, other.end) }
            ?: OverlapLookupResultDomainModel.None
    } catch (_: DataException) {
        OverlapLookupResultDomainModel.Failed
    }

    override fun updateAttackTimes(
        attackId: String,
        start: Instant,
        end: Instant?,
        pointsToRemove: List<IntensityPointDomainModel>
    ) = write { attackDataSource.updateTimes(attackId, start, end, pointsToRemove.map(::toData)) }

    override fun restoreAttackTimes(
        attackId: String,
        start: Instant,
        end: Instant?,
        points: List<IntensityPointDomainModel>
    ) = write { attackDataSource.restoreTimes(attackId, start, end, points.map(::toData)) }

    override fun addIntensityPoint(attackId: String, point: IntensityPointDomainModel) = write {
        attackDataSource.addIntensityPoint(attackId, point.at, point.value)
    }

    override fun maximumRecorded(): MaximumRecordedResultDomainModel = try {
        if (attackDataSource.maximumRecorded()) {
            MaximumRecordedResultDomainModel.Recorded
        } else {
            MaximumRecordedResultDomainModel.NotRecorded
        }
    } catch (_: DataException) {
        MaximumRecordedResultDomainModel.Failed
    }

    override fun removeIntensityPoint(attackId: String, point: IntensityPointDomainModel) = write {
        attackDataSource.removeIntensityPoint(attackId, toData(point))
    }

    override fun selectAttackTag(attackId: String, tag: AttackTagDomainModel, selected: Boolean) =
        write {
            attackDataSource.setTag(attackId, attackTagDataMapper.toData(tag), selected)
        }

    override fun setFunctionalImpact(attackId: String, impact: FunctionalImpactDomainModel?) =
        write {
            attackDataSource.setFunctionalImpact(attackId, impact?.level)
        }

    override fun updateAttackNote(attackId: String, note: String) = write {
        attackDataSource.setNote(attackId, note)
    }

    override fun deleteAttack(attackId: String) = write { attackDataSource.softDelete(attackId) }

    private fun toData(point: IntensityPointDomainModel) = IntensityPointDataModel(
        point.at,
        point.value
    )

    private inline fun write(block: () -> Unit): WriteResultDomainModel = try {
        block()
        WriteResultDomainModel.Written
    } catch (_: StorageFullDataException) {
        WriteResultDomainModel.StorageFull
    } catch (_: DataException) {
        WriteResultDomainModel.Failed
    }
}
