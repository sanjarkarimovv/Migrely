package com.migrely.app.datasource.medication.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.migrely.app.datasource.medication.model.AcuteIntakeLocalModel
import com.migrely.app.datasource.medication.model.IntakeWithMedicationLocalModel
import com.migrely.app.datasource.medication.model.MedicationIntakeLocalModel
import com.migrely.app.datasource.medication.model.MedicationLocalModel
import com.migrely.app.datasource.medication.model.OveruseNoticeLocalModel
import com.migrely.app.datasource.medication.model.PendingEffectQuestionLocalModel

/** Medication, intake and overuse notice queries (database version 4). */
@Dao
interface MedicationDao {
    /** Active medications, the most recently taken first, then never-taken ones by name. */
    @Query(
        "SELECT medication.* FROM medication " +
            "LEFT JOIN (SELECT medication_id, MAX(taken_epoch_millis) AS last_taken " +
            "FROM medication_intake GROUP BY medication_id) AS last_use " +
            "ON last_use.medication_id = medication.id " +
            "WHERE medication.archived = 0 " +
            "ORDER BY last_use.last_taken IS NULL, last_use.last_taken DESC, " +
            "medication.name COLLATE NOCASE"
    )
    fun activeByLastUse(): List<MedicationLocalModel>

    @Query("SELECT * FROM medication WHERE id = :medicationId")
    fun byId(medicationId: String): MedicationLocalModel?

    @Query("SELECT * FROM medication WHERE name_key = :nameKey LIMIT 1")
    fun byNameKey(nameKey: String): MedicationLocalModel?

    @Query("SELECT COUNT(*) FROM medication WHERE archived = 0")
    fun countActive(): Int

    @Insert
    fun insert(medication: MedicationLocalModel)

    @Suppress("LongParameterList")
    @Query(
        "UPDATE medication SET name = :name, name_key = :nameKey, kind = :kind, " +
            "category = :category, default_dose = :defaultDose, " +
            "updated_epoch_millis = :nowEpochMillis " +
            "WHERE id = :medicationId"
    )
    fun update(
        medicationId: String,
        name: String,
        nameKey: String,
        kind: String,
        category: String?,
        defaultDose: String?,
        nowEpochMillis: Long
    ): Int

    @Query(
        "UPDATE medication SET archived = :archived, updated_epoch_millis = :nowEpochMillis " +
            "WHERE id = :medicationId"
    )
    fun setArchived(medicationId: String, archived: Boolean, nowEpochMillis: Long)

    @Insert
    fun insertIntake(intake: MedicationIntakeLocalModel)

    @Query("DELETE FROM medication_intake WHERE id = :intakeId")
    fun deleteIntake(intakeId: String)

    @Query(
        "SELECT medication_intake.id, medication_intake.medication_id, " +
            "medication.name AS medication_name, medication_intake.attack_id, " +
            "medication_intake.taken_epoch_millis, medication_intake.taken_zone_id, " +
            "medication_intake.dose, medication_intake.effect, medication_intake.effect_closed " +
            "FROM medication_intake " +
            "JOIN medication ON medication.id = medication_intake.medication_id " +
            "WHERE medication_intake.attack_id = :attackId " +
            "ORDER BY medication_intake.taken_epoch_millis"
    )
    fun intakesOfAttack(attackId: String): List<IntakeWithMedicationLocalModel>

    /** Oldest first; an intake outside any attack has no attack row, so its deletion time reads as null. */
    @Query(
        "SELECT medication_intake.id, medication.name AS medication_name, " +
            "medication_intake.dose, medication_intake.taken_epoch_millis, " +
            "medication_intake.taken_zone_id " +
            "FROM medication_intake " +
            "JOIN medication ON medication.id = medication_intake.medication_id " +
            "LEFT JOIN attack ON attack.id = medication_intake.attack_id " +
            "WHERE medication.kind != 'PREVENTIVE' AND medication_intake.effect IS NULL " +
            "AND medication_intake.effect_closed = 0 " +
            "AND medication_intake.taken_epoch_millis >= :fromEpochMillis " +
            "AND medication_intake.taken_epoch_millis < :toEpochMillis " +
            "AND attack.deleted_epoch_millis IS NULL " +
            "ORDER BY medication_intake.taken_epoch_millis LIMIT 1"
    )
    fun pendingEffectQuestion(
        fromEpochMillis: Long,
        toEpochMillis: Long
    ): PendingEffectQuestionLocalModel?

    @Query(
        "UPDATE medication_intake SET effect = :effect, effect_closed = 1 WHERE id = :intakeId"
    )
    fun setEffect(intakeId: String, effect: String?)

    @Query("UPDATE medication_intake SET effect_closed = 1 WHERE id = :intakeId")
    fun closeEffectQuestion(intakeId: String)

    @Query(
        "SELECT medication_intake.taken_epoch_millis, medication_intake.taken_zone_id, " +
            "medication.category FROM medication_intake " +
            "JOIN medication ON medication.id = medication_intake.medication_id " +
            "LEFT JOIN attack ON attack.id = medication_intake.attack_id " +
            "WHERE medication.kind != 'PREVENTIVE' " +
            "AND medication_intake.taken_epoch_millis >= :fromEpochMillis " +
            "AND medication_intake.taken_epoch_millis < :toEpochMillis " +
            "AND attack.deleted_epoch_millis IS NULL"
    )
    fun acuteIntakesBetween(fromEpochMillis: Long, toEpochMillis: Long): List<AcuteIntakeLocalModel>

    @Query("SELECT * FROM overuse_notice")
    fun overuseNotices(): List<OveruseNoticeLocalModel>

    @Upsert
    fun upsertOveruseNotice(notice: OveruseNoticeLocalModel)
}
