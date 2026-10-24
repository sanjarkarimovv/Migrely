package com.migrely.app.datasource.backup.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.migrely.app.datasource.attack.model.AttackLocalModel
import com.migrely.app.datasource.attack.model.AttackTagLocalModel
import com.migrely.app.datasource.attack.model.IntensityPointLocalModel
import com.migrely.app.datasource.backup.model.AddedLocalModel
import com.migrely.app.datasource.backup.model.BackupLocalModel
import com.migrely.app.datasource.daymark.model.DayMarkLocalModel
import com.migrely.app.datasource.medication.model.MedicationIntakeLocalModel
import com.migrely.app.datasource.medication.model.MedicationLocalModel
import com.migrely.app.datasource.tag.model.CustomTagLocalModel

/** Reads and writes every health table at once (database version 6, change add-data-export-import). */
@Dao
abstract class BackupDao {
    @Query("SELECT * FROM attack WHERE deleted_epoch_millis IS NULL ORDER BY start_epoch_millis")
    abstract fun attacks(): List<AttackLocalModel>

    @Query(
        "SELECT intensity_point.* FROM intensity_point " +
            "JOIN attack ON attack.id = intensity_point.attack_id " +
            "WHERE attack.deleted_epoch_millis IS NULL " +
            "ORDER BY intensity_point.at_epoch_millis, intensity_point.id"
    )
    abstract fun intensityPoints(): List<IntensityPointLocalModel>

    @Query(
        "SELECT attack_tag.* FROM attack_tag JOIN attack ON attack.id = attack_tag.attack_id " +
            "WHERE attack.deleted_epoch_millis IS NULL"
    )
    abstract fun attackTags(): List<AttackTagLocalModel>

    @Query("SELECT * FROM custom_tag ORDER BY created_epoch_millis, id")
    abstract fun customTags(): List<CustomTagLocalModel>

    @Query("SELECT * FROM medication ORDER BY created_epoch_millis, id")
    abstract fun medications(): List<MedicationLocalModel>

    /** Intakes of an attack being deleted go with it, so they are left out too. */
    @Query(
        "SELECT * FROM medication_intake WHERE attack_id IS NULL " +
            "OR attack_id IN (SELECT id FROM attack WHERE deleted_epoch_millis IS NULL) " +
            "ORDER BY taken_epoch_millis, id"
    )
    abstract fun intakes(): List<MedicationIntakeLocalModel>

    @Query("SELECT * FROM day_mark ORDER BY date")
    abstract fun dayMarks(): List<DayMarkLocalModel>

    @Transaction
    open fun records() = BackupLocalModel(
        attacks = attacks(),
        intensityPoints = intensityPoints(),
        attackTags = attackTags(),
        customTags = customTags(),
        medications = medications(),
        intakes = intakes(),
        dayMarks = dayMarks()
    )

    /** An intake always has a medication, so medications stand for intakes here. */
    @Query(
        "SELECT EXISTS(SELECT 1 FROM attack WHERE deleted_epoch_millis IS NULL) " +
            "OR EXISTS(SELECT 1 FROM medication) OR EXISTS(SELECT 1 FROM custom_tag) " +
            "OR EXISTS(SELECT 1 FROM day_mark)"
    )
    abstract fun hasRecords(): Boolean

    @Query("DELETE FROM medication_intake")
    abstract fun deleteIntakes()

    /** Takes the attacks' intensity points and tags with them. */
    @Query("DELETE FROM attack")
    abstract fun deleteAttacks()

    @Query("DELETE FROM medication")
    abstract fun deleteMedications()

    @Query("DELETE FROM custom_tag")
    abstract fun deleteCustomTags()

    @Query("DELETE FROM day_mark")
    abstract fun deleteDayMarks()

    @Insert
    abstract fun insertCustomTags(tags: List<CustomTagLocalModel>)

    @Insert
    abstract fun insertMedications(medications: List<MedicationLocalModel>)

    @Insert
    abstract fun insertAttacks(attacks: List<AttackLocalModel>)

    @Insert
    abstract fun insertIntensityPoints(points: List<IntensityPointLocalModel>)

    @Insert
    abstract fun insertAttackTags(tags: List<AttackTagLocalModel>)

    @Insert
    abstract fun insertIntakes(intakes: List<MedicationIntakeLocalModel>)

    @Insert
    abstract fun insertDayMarks(marks: List<DayMarkLocalModel>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertDayMarkIfAbsent(mark: DayMarkLocalModel)

    @Query("SELECT EXISTS(SELECT 1 FROM custom_tag WHERE id = :tagId)")
    abstract fun hasCustomTag(tagId: String): Boolean

    @Query("SELECT id FROM custom_tag WHERE kind = :kind AND name_key = :nameKey LIMIT 1")
    abstract fun customTagIdByName(kind: String, nameKey: String): String?

    @Query("SELECT EXISTS(SELECT 1 FROM medication WHERE id = :medicationId)")
    abstract fun hasMedication(medicationId: String): Boolean

    @Query("SELECT id FROM medication WHERE name_key = :nameKey LIMIT 1")
    abstract fun medicationIdByName(nameKey: String): String?

    /** Any attack with this ID, even one being deleted. */
    @Query("SELECT EXISTS(SELECT 1 FROM attack WHERE id = :attackId)")
    abstract fun hasAttack(attackId: String): Boolean

    /** The same rule as logging an attack: an attack in progress reaches into the future. */
    @Query(
        "SELECT EXISTS(SELECT 1 FROM attack WHERE deleted_epoch_millis IS NULL " +
            "AND start_epoch_millis < :endEpochMillis " +
            "AND (end_epoch_millis IS NULL OR end_epoch_millis > :startEpochMillis))"
    )
    abstract fun anyOverlapping(startEpochMillis: Long, endEpochMillis: Long): Boolean

    @Query(
        "SELECT EXISTS(SELECT 1 FROM medication_intake WHERE id = :intakeId " +
            "OR (medication_id = :medicationId AND taken_epoch_millis = :takenEpochMillis))"
    )
    abstract fun hasIntake(intakeId: String, medicationId: String, takenEpochMillis: Long): Boolean

    /** Deletes every health row and writes [records] in their place (BRD D73 "Replace"). */
    @Transaction
    open fun replaceAll(records: BackupLocalModel) {
        deleteIntakes()
        deleteAttacks()
        deleteMedications()
        deleteCustomTags()
        deleteDayMarks()
        insertCustomTags(records.customTags)
        insertMedications(records.medications)
        insertAttacks(records.attacks)
        insertIntensityPoints(records.intensityPoints)
        insertAttackTags(records.attackTags)
        insertIntakes(records.intakes)
        insertDayMarks(records.dayMarks)
    }

    /** Writes what the phone does not have yet (BRD D73 "Add"); the rules are on `BackupDataSource.add`. */
    @Transaction
    open fun add(records: BackupLocalModel): AddedLocalModel {
        val tagIds = records.customTags.associate { it.id to customTagIdOnPhone(it) }
        val medicationIds = records.medications.associate { it.id to medicationIdOnPhone(it) }
        val pointsByAttack = records.intensityPoints.groupBy(IntensityPointLocalModel::attackId)
        val tagsByAttack = records.attackTags.groupBy(AttackTagLocalModel::attackId)
        val onPhone = mutableSetOf<String>()
        var added = 0
        records.attacks.sortedBy(AttackLocalModel::startEpochMillis).forEach { attack ->
            when {
                hasAttack(attack.id) -> onPhone += attack.id
                anyOverlapping(
                    attack.startEpochMillis,
                    attack.endEpochMillis ?: Long.MAX_VALUE
                ) -> Unit
                else -> {
                    insertAttacks(listOf(attack))
                    insertIntensityPoints(pointsByAttack[attack.id].orEmpty())
                    insertAttackTags(
                        tagsByAttack[attack.id].orEmpty().map {
                            it.withTagIds(tagIds)
                        }
                    )
                    onPhone += attack.id
                    added++
                }
            }
        }
        records.intakes.forEach { intake ->
            val medicationId = medicationIds.getValue(intake.medicationId)
            if (!hasIntake(intake.id, medicationId, intake.takenEpochMillis)) {
                insertIntakes(
                    listOf(
                        intake.copy(
                            medicationId = medicationId,
                            attackId = intake.attackId?.takeIf(onPhone::contains)
                        )
                    )
                )
            }
        }
        records.dayMarks.forEach(::insertDayMarkIfAbsent)
        return AddedLocalModel(addedAttacks = added, skippedAttacks = records.attacks.size - added)
    }

    private fun customTagIdOnPhone(tag: CustomTagLocalModel): String = when {
        hasCustomTag(tag.id) -> tag.id
        else -> customTagIdByName(tag.kind, tag.nameKey)
            ?: tag.id.also { insertCustomTags(listOf(tag)) }
    }

    private fun medicationIdOnPhone(medication: MedicationLocalModel): String = when {
        hasMedication(medication.id) -> medication.id
        else -> medicationIdByName(medication.nameKey)
            ?: medication.id.also { insertMedications(listOf(medication)) }
    }

    private fun AttackTagLocalModel.withTagIds(tagIds: Map<String, String>): AttackTagLocalModel {
        val prefix = AttackTagLocalModel.CUSTOM_KEY_PREFIX
        val tagId = tagKey.removePrefix(prefix).takeIf { tagKey.startsWith(prefix) }
            ?: return this
        return copy(tagKey = prefix + (tagIds[tagId] ?: tagId))
    }
}
