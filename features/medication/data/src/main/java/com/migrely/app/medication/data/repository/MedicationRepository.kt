package com.migrely.app.medication.data.repository

import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.attack.model.AttackDataModel
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.exception.DataException
import com.migrely.app.datasource.exception.StorageFullDataException
import com.migrely.app.datasource.medication.datasource.MedicationDataSource
import com.migrely.app.datasource.medication.model.MedicationDataModel
import com.migrely.app.medication.data.mapper.MedicationCategoryDataMapper
import com.migrely.app.medication.data.mapper.MedicationDomainMapper
import com.migrely.app.medication.data.mapper.MedicationKindDataMapper
import com.migrely.app.medication.domain.model.ActiveCountResultDomainModel
import com.migrely.app.medication.domain.model.AddIntakeResultDomainModel
import com.migrely.app.medication.domain.model.AttackLookupResultDomainModel
import com.migrely.app.medication.domain.model.CurrentTimeDomainModel
import com.migrely.app.medication.domain.model.LinkedAttackDomainModel
import com.migrely.app.medication.domain.model.MedicationCategoryDomainModel
import com.migrely.app.medication.domain.model.MedicationKindDomainModel
import com.migrely.app.medication.domain.model.MedicationLookupResultDomainModel
import com.migrely.app.medication.domain.model.MedicationWriteResultDomainModel
import com.migrely.app.medication.domain.model.MedicationsResultDomainModel
import com.migrely.app.medication.domain.model.WriteResultDomainModel
import com.migrely.app.medication.domain.repository.AddIntakeRepository
import com.migrely.app.medication.domain.repository.CountActiveMedicationsRepository
import com.migrely.app.medication.domain.repository.CreateMedicationRepository
import com.migrely.app.medication.domain.repository.DeleteIntakeRepository
import com.migrely.app.medication.domain.repository.FindAttackAtRepository
import com.migrely.app.medication.domain.repository.FindMedicationByNameRepository
import com.migrely.app.medication.domain.repository.GetActiveMedicationsRepository
import com.migrely.app.medication.domain.repository.GetAttackRepository
import com.migrely.app.medication.domain.repository.GetCurrentTimeRepository
import com.migrely.app.medication.domain.repository.GetMedicationRepository
import com.migrely.app.medication.domain.repository.SetMedicationArchivedRepository
import com.migrely.app.medication.domain.repository.UpdateMedicationRepository
import java.time.Instant
import java.time.ZoneId

/**
 * Every function returns failures as values (docs/ARCHITECTURE.md section 7): datasource exceptions never reach the
 * background use cases.
 */
@Suppress("TooManyFunctions")
class MedicationRepository(
    private val medicationDataSource: MedicationDataSource,
    private val attackDataSource: AttackDataSource,
    private val clockDataSource: ClockDataSource,
    private val medicationDomainMapper: MedicationDomainMapper,
    private val medicationKindDataMapper: MedicationKindDataMapper,
    private val medicationCategoryDataMapper: MedicationCategoryDataMapper
) : GetCurrentTimeRepository,
    GetActiveMedicationsRepository,
    GetMedicationRepository,
    FindMedicationByNameRepository,
    CountActiveMedicationsRepository,
    CreateMedicationRepository,
    UpdateMedicationRepository,
    SetMedicationArchivedRepository,
    GetAttackRepository,
    FindAttackAtRepository,
    AddIntakeRepository,
    DeleteIntakeRepository {
    override fun currentTime() =
        CurrentTimeDomainModel(instant = clockDataSource.now(), zone = clockDataSource.zone())

    override fun activeMedications(): MedicationsResultDomainModel = try {
        MedicationsResultDomainModel.Loaded(
            medicationDataSource.activeMedications().map(medicationDomainMapper::toDomain)
        )
    } catch (_: DataException) {
        MedicationsResultDomainModel.Failed
    }

    override fun medication(medicationId: String) =
        lookUp { medicationDataSource.medication(medicationId) }

    override fun medicationByNameKey(nameKey: String) =
        lookUp { medicationDataSource.medicationByNameKey(nameKey) }

    override fun activeMedicationCount(): ActiveCountResultDomainModel = try {
        ActiveCountResultDomainModel.Counted(medicationDataSource.activeMedicationCount())
    } catch (_: DataException) {
        ActiveCountResultDomainModel.Failed
    }

    override fun createMedication(
        name: String,
        nameKey: String,
        kind: MedicationKindDomainModel,
        category: MedicationCategoryDomainModel?,
        defaultDose: String?
    ) = writeMedication {
        medicationDataSource.createMedication(
            name,
            nameKey,
            medicationKindDataMapper.toData(kind),
            medicationCategoryDataMapper.toData(category),
            defaultDose
        )
    }

    override fun updateMedication(
        medicationId: String,
        name: String,
        nameKey: String,
        kind: MedicationKindDomainModel,
        category: MedicationCategoryDomainModel?,
        defaultDose: String?
    ) = writeMedication {
        medicationDataSource.updateMedication(
            medicationId,
            name,
            nameKey,
            medicationKindDataMapper.toData(kind),
            medicationCategoryDataMapper.toData(category),
            defaultDose
        )
    }

    override fun setMedicationArchived(medicationId: String, archived: Boolean) =
        write { medicationDataSource.setArchived(medicationId, archived) }

    override fun attack(attackId: String): AttackLookupResultDomainModel = lookUpAttack {
        attackDataSource.byId(attackId)
    }

    // An instant inside an attack overlaps the millisecond that starts there; an attack in progress is open-ended.
    override fun attackAt(instant: Instant): AttackLookupResultDomainModel = lookUpAttack {
        attackDataSource.overlapping(null, instant, instant.plusMillis(1)).firstOrNull()
    }

    override fun addIntake(
        medicationId: String,
        attackId: String?,
        takenAt: Instant,
        zone: ZoneId,
        dose: String?
    ): AddIntakeResultDomainModel = try {
        AddIntakeResultDomainModel.Added(
            medicationDataSource.addIntake(medicationId, attackId, takenAt, zone.id, dose)
        )
    } catch (_: StorageFullDataException) {
        AddIntakeResultDomainModel.StorageFull
    } catch (_: DataException) {
        AddIntakeResultDomainModel.Failed
    }

    override fun deleteIntake(intakeId: String) = write {
        medicationDataSource.deleteIntake(intakeId)
    }

    private inline fun lookUp(read: () -> MedicationDataModel?): MedicationLookupResultDomainModel =
        try {
            read()?.let {
                MedicationLookupResultDomainModel.Found(medicationDomainMapper.toDomain(it))
            }
                ?: MedicationLookupResultDomainModel.NotFound
        } catch (_: DataException) {
            MedicationLookupResultDomainModel.Failed
        }

    private inline fun lookUpAttack(read: () -> AttackDataModel?): AttackLookupResultDomainModel =
        try {
            read()?.let { attack ->
                AttackLookupResultDomainModel.Found(
                    LinkedAttackDomainModel(attack.id, attack.start, attack.end)
                )
            } ?: AttackLookupResultDomainModel.None
        } catch (_: DataException) {
            AttackLookupResultDomainModel.Failed
        }

    private inline fun writeMedication(
        write: () -> MedicationDataModel?
    ): MedicationWriteResultDomainModel = try {
        write()?.let { MedicationWriteResultDomainModel.Done(medicationDomainMapper.toDomain(it)) }
            ?: MedicationWriteResultDomainModel.Failed
    } catch (_: StorageFullDataException) {
        MedicationWriteResultDomainModel.StorageFull
    } catch (_: DataException) {
        MedicationWriteResultDomainModel.Failed
    }

    private inline fun write(block: () -> Unit): WriteResultDomainModel = try {
        block()
        WriteResultDomainModel.Done
    } catch (_: StorageFullDataException) {
        WriteResultDomainModel.StorageFull
    } catch (_: DataException) {
        WriteResultDomainModel.Failed
    }
}
