package com.migrely.app.attack.data.repository

import com.migrely.app.attack.data.mapper.AttackDetailsDomainResolver
import com.migrely.app.attack.data.mapper.AttackIntakeDomainMapper
import com.migrely.app.attack.data.mapper.AttackTagDataMapper
import com.migrely.app.attack.domain.model.AttackDetailsDomainModel
import com.migrely.app.attack.domain.model.AttackDetailsLookupResultDomainModel
import com.migrely.app.attack.domain.model.AttackIntakeDomainModel
import com.migrely.app.attack.domain.model.AttackMergeDomainModel
import com.migrely.app.attack.domain.model.AttackTagDomainModel
import com.migrely.app.attack.domain.model.CurrentTimeDomainModel
import com.migrely.app.attack.domain.model.FunctionalImpactDomainModel
import com.migrely.app.attack.domain.model.IntensityPointDomainModel
import com.migrely.app.attack.domain.model.MaximumRecordedResultDomainModel
import com.migrely.app.attack.domain.model.OverlapLookupResultDomainModel
import com.migrely.app.attack.domain.model.StorePastAttackResultDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.repository.AddIntensityPointRepository
import com.migrely.app.attack.domain.repository.DeleteAttackRepository
import com.migrely.app.attack.domain.repository.FindOverlappingAttacksRepository
import com.migrely.app.attack.domain.repository.GetAttackDetailsRepository
import com.migrely.app.attack.domain.repository.GetCurrentTimeRepository
import com.migrely.app.attack.domain.repository.GetMaximumRecordedRepository
import com.migrely.app.attack.domain.repository.MergeAttacksRepository
import com.migrely.app.attack.domain.repository.RemoveIntakeRepository
import com.migrely.app.attack.domain.repository.RemoveIntensityPointRepository
import com.migrely.app.attack.domain.repository.RestoreAttackTimesRepository
import com.migrely.app.attack.domain.repository.RestoreIntakeRepository
import com.migrely.app.attack.domain.repository.SelectAttackTagRepository
import com.migrely.app.attack.domain.repository.SetFunctionalImpactRepository
import com.migrely.app.attack.domain.repository.StorePastAttackRepository
import com.migrely.app.attack.domain.repository.UpdateAttackNoteRepository
import com.migrely.app.attack.domain.repository.UpdateAttackTimesRepository
import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.attack.model.AttackDataModel
import com.migrely.app.datasource.attack.model.AttackTagDataModel
import com.migrely.app.datasource.attack.model.CreateEndedDataModel
import com.migrely.app.datasource.attack.model.IntensityPointDataModel
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.exception.DataException
import com.migrely.app.datasource.exception.StorageFullDataException
import com.migrely.app.datasource.medication.datasource.MedicationDataSource
import com.migrely.app.datasource.medication.model.IntakeDataModel
import com.migrely.app.datasource.tag.datasource.TagDataSource
import java.time.Instant
import java.time.ZoneId

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
    private val attackTagDataMapper: AttackTagDataMapper,
    private val medicationDataSource: MedicationDataSource,
    private val attackIntakeDomainMapper: AttackIntakeDomainMapper
) : GetAttackDetailsRepository,
    GetCurrentTimeRepository,
    FindOverlappingAttacksRepository,
    StorePastAttackRepository,
    MergeAttacksRepository,
    UpdateAttackTimesRepository,
    RestoreAttackTimesRepository,
    AddIntensityPointRepository,
    GetMaximumRecordedRepository,
    RemoveIntensityPointRepository,
    SelectAttackTagRepository,
    SetFunctionalImpactRepository,
    UpdateAttackNoteRepository,
    DeleteAttackRepository,
    RemoveIntakeRepository,
    RestoreIntakeRepository {
    override fun attackDetails(attackId: String): AttackDetailsLookupResultDomainModel = try {
        val attack = attackDataSource.byId(attackId)
        if (attack == null) {
            AttackDetailsLookupResultDomainModel.NotFound
        } else {
            AttackDetailsLookupResultDomainModel.Found(
                withCustomTags(listOf(attack)).single().copy(
                    intakes = medicationDataSource.intakesOfAttack(attackId)
                        .map(attackIntakeDomainMapper::toDomain)
                )
            )
        }
    } catch (_: DataException) {
        AttackDetailsLookupResultDomainModel.Failed
    }

    override fun currentTime() = CurrentTimeDomainModel(
        clockDataSource.now(),
        clockDataSource.zone()
    )

    override fun overlappingAttacks(
        excludingId: String?,
        start: Instant,
        end: Instant
    ): OverlapLookupResultDomainModel = try {
        val attacks = attackDataSource.overlapping(excludingId, start, end)
        if (attacks.isEmpty()) {
            OverlapLookupResultDomainModel.None
        } else {
            OverlapLookupResultDomainModel.Found(withCustomTags(attacks))
        }
    } catch (_: DataException) {
        OverlapLookupResultDomainModel.Failed
    }

    override fun storePastAttack(
        start: Instant,
        zone: ZoneId,
        end: Instant
    ): StorePastAttackResultDomainModel = try {
        when (val created = attackDataSource.createEnded(start, zone.id, end)) {
            is CreateEndedDataModel.Created ->
                StorePastAttackResultDomainModel.Stored(created.attackId)

            CreateEndedDataModel.Overlapping -> StorePastAttackResultDomainModel.Overlapping
        }
    } catch (_: StorageFullDataException) {
        StorePastAttackResultDomainModel.StorageFull
    } catch (_: DataException) {
        StorePastAttackResultDomainModel.Failed
    }

    override fun mergeAttacks(merge: AttackMergeDomainModel) = write {
        attackDataSource.merge(
            survivorId = merge.survivorId,
            absorbedIds = merge.absorbedIds,
            start = merge.start,
            startZoneId = merge.startZone.id,
            end = merge.end,
            functionalImpact = merge.functionalImpact?.level,
            note = merge.note
        )
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

    /** Resolves the custom tags of [attacks] with one lookup. */
    private fun withCustomTags(attacks: List<AttackDataModel>): List<AttackDetailsDomainModel> {
        val customIds = attacks.flatMap { attack ->
            attack.tags.mapNotNull { tag ->
                when (tag) {
                    is AttackTagDataModel.CustomSymptom -> tag.tagId
                    is AttackTagDataModel.CustomTrigger -> tag.tagId
                    is AttackTagDataModel.Location,
                    is AttackTagDataModel.BuiltInSymptom,
                    is AttackTagDataModel.BuiltInTrigger -> null
                }
            }
        }.distinct()
        val customTags = tagDataSource.customTags(customIds)
        return attacks.map { attack -> attackDetailsDomainResolver.toDomain(attack, customTags) }
    }

    override fun removeIntake(intakeId: String) = write {
        medicationDataSource.deleteIntake(intakeId)
    }

    override fun restoreIntake(attackId: String, intake: AttackIntakeDomainModel) = write {
        medicationDataSource.restoreIntake(
            IntakeDataModel(
                id = intake.id,
                medicationId = intake.medicationId,
                medicationName = intake.medicationName,
                attackId = attackId,
                takenAt = intake.takenAt,
                takenZoneId = intake.zone.id,
                dose = intake.dose
            )
        )
    }

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
