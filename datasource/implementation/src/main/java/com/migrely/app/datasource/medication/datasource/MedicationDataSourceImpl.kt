package com.migrely.app.datasource.medication.datasource

import com.migrely.app.datasource.database.database
import com.migrely.app.datasource.medication.dao.MedicationDao
import com.migrely.app.datasource.medication.mapper.MedicationLocalMapper
import com.migrely.app.datasource.medication.mapper.OveruseNoticeLocalMapper
import com.migrely.app.datasource.medication.model.AcuteIntakeDataModel
import com.migrely.app.datasource.medication.model.IntakeDataModel
import com.migrely.app.datasource.medication.model.IntakeEffectDataModel
import com.migrely.app.datasource.medication.model.MedicationCategoryDataModel
import com.migrely.app.datasource.medication.model.MedicationDataModel
import com.migrely.app.datasource.medication.model.MedicationIntakeLocalModel
import com.migrely.app.datasource.medication.model.MedicationKindDataModel
import com.migrely.app.datasource.medication.model.MedicationLocalModel
import com.migrely.app.datasource.medication.model.OveruseCategoryDataModel
import com.migrely.app.datasource.medication.model.OveruseLevelDataModel
import com.migrely.app.datasource.medication.model.OveruseNoticeDataModel
import com.migrely.app.datasource.medication.model.PendingEffectQuestionDataModel
import java.time.Clock
import java.time.Instant
import java.util.UUID

private const val MAXIMUM_NAME_LENGTH = 60
private const val MAXIMUM_DOSE_LENGTH = 20

@Suppress("TooManyFunctions")
class MedicationDataSourceImpl(
    private val medicationDao: MedicationDao,
    private val medicationLocalMapper: MedicationLocalMapper,
    private val overuseNoticeLocalMapper: OveruseNoticeLocalMapper,
    private val clock: Clock = Clock.systemUTC(),
    private val generateId: () -> String = { UUID.randomUUID().toString() }
) : MedicationDataSource {
    override fun activeMedications(): List<MedicationDataModel> = database {
        medicationDao.activeByLastUse().map(medicationLocalMapper::toData)
    }

    override fun medication(medicationId: String): MedicationDataModel? = database {
        medicationDao.byId(medicationId)?.let(medicationLocalMapper::toData)
    }

    override fun medicationByNameKey(nameKey: String): MedicationDataModel? = database {
        medicationDao.byNameKey(nameKey)?.let(medicationLocalMapper::toData)
    }

    override fun activeMedicationCount(): Int = database { medicationDao.countActive() }

    override fun createMedication(
        name: String,
        nameKey: String,
        kind: MedicationKindDataModel,
        category: MedicationCategoryDataModel?,
        defaultDose: String?
    ): MedicationDataModel {
        requireValid(name, defaultDose)
        val now = clock.millis()
        val local = MedicationLocalModel(
            id = generateId(),
            name = name,
            nameKey = nameKey,
            kind = medicationLocalMapper.toLocalKind(kind),
            category = medicationLocalMapper.toLocalCategory(category),
            defaultDose = defaultDose,
            archived = false,
            createdEpochMillis = now,
            updatedEpochMillis = now
        )
        database { medicationDao.insert(local) }
        return medicationLocalMapper.toData(local)
    }

    override fun updateMedication(
        medicationId: String,
        name: String,
        nameKey: String,
        kind: MedicationKindDataModel,
        category: MedicationCategoryDataModel?,
        defaultDose: String?
    ): MedicationDataModel? {
        requireValid(name, defaultDose)
        return database {
            val updated = medicationDao.update(
                medicationId = medicationId,
                name = name,
                nameKey = nameKey,
                kind = medicationLocalMapper.toLocalKind(kind),
                category = medicationLocalMapper.toLocalCategory(category),
                defaultDose = defaultDose,
                nowEpochMillis = clock.millis()
            )
            if (updated ==
                0
            ) {
                null
            } else {
                medicationDao.byId(medicationId)?.let(medicationLocalMapper::toData)
            }
        }
    }

    override fun setArchived(medicationId: String, archived: Boolean) = database {
        medicationDao.setArchived(medicationId, archived, clock.millis())
    }

    override fun addIntake(
        medicationId: String,
        attackId: String?,
        takenAt: Instant,
        takenZoneId: String,
        dose: String?
    ): String {
        val id = generateId()
        database {
            medicationDao.insertIntake(
                MedicationIntakeLocalModel(
                    id = id,
                    medicationId = medicationId,
                    attackId = attackId,
                    takenEpochMillis = takenAt.toEpochMilli(),
                    takenZoneId = takenZoneId,
                    dose = dose,
                    createdEpochMillis = clock.millis()
                )
            )
        }
        return id
    }

    override fun restoreIntake(intake: IntakeDataModel) = database {
        medicationDao.insertIntake(
            MedicationIntakeLocalModel(
                id = intake.id,
                medicationId = intake.medicationId,
                attackId = intake.attackId,
                takenEpochMillis = intake.takenAt.toEpochMilli(),
                takenZoneId = intake.takenZoneId,
                dose = intake.dose,
                createdEpochMillis = clock.millis(),
                effect = medicationLocalMapper.toLocalEffect(intake.effect),
                effectClosed = intake.effectClosed
            )
        )
    }

    override fun deleteIntake(intakeId: String) = database { medicationDao.deleteIntake(intakeId) }

    override fun intakesOfAttack(attackId: String): List<IntakeDataModel> = database {
        medicationDao.intakesOfAttack(attackId).map(medicationLocalMapper::toData)
    }

    override fun pendingEffectQuestion(
        takenFrom: Instant,
        takenTo: Instant
    ): PendingEffectQuestionDataModel? = database {
        medicationDao.pendingEffectQuestion(takenFrom.toEpochMilli(), takenTo.toEpochMilli())
            ?.let(medicationLocalMapper::toData)
    }

    override fun pendingEffectQuestionOf(
        intakeId: String,
        takenFrom: Instant,
        takenTo: Instant
    ): PendingEffectQuestionDataModel? = database {
        medicationDao.pendingEffectQuestionOf(
            intakeId,
            takenFrom.toEpochMilli(),
            takenTo.toEpochMilli()
        )
            ?.let(medicationLocalMapper::toData)
    }

    override fun setIntakeEffect(intakeId: String, effect: IntakeEffectDataModel?) = database {
        medicationDao.setEffect(intakeId, medicationLocalMapper.toLocalEffect(effect))
    }

    override fun closeEffectQuestion(intakeId: String) = database {
        medicationDao.closeEffectQuestion(intakeId)
    }

    override fun acuteIntakesTakenBetween(from: Instant, to: Instant): List<AcuteIntakeDataModel> =
        database {
            medicationDao.acuteIntakesBetween(from.toEpochMilli(), to.toEpochMilli())
                .map(medicationLocalMapper::toData)
        }

    override fun overuseNotices(): List<OveruseNoticeDataModel> = database {
        medicationDao.overuseNotices().mapNotNull(overuseNoticeLocalMapper::toData)
    }

    override fun acknowledgeOveruse(
        category: OveruseCategoryDataModel,
        level: OveruseLevelDataModel,
        acknowledgedAt: Instant
    ) = database {
        medicationDao.upsertOveruseNotice(
            overuseNoticeLocalMapper.toLocal(
                OveruseNoticeDataModel(category, level, acknowledgedAt)
            )
        )
    }

    // Out-of-range text is a programming error (BRD D35), not a storage failure.
    private fun requireValid(name: String, defaultDose: String?) {
        require(name.isNotBlank() && name.length <= MAXIMUM_NAME_LENGTH) {
            "Invalid medication name length ${name.length}"
        }
        require(defaultDose == null || defaultDose.length <= MAXIMUM_DOSE_LENGTH) {
            "Invalid dose length ${defaultDose?.length}"
        }
    }
}
