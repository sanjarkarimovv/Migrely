package com.migrely.app.datasource.medication.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.migrely.app.datasource.medication.model.IntakeWithMedicationLocalModel
import com.migrely.app.datasource.medication.model.MedicationIntakeLocalModel
import com.migrely.app.datasource.medication.model.MedicationLocalModel

/** Medication and intake queries (database version 3). */
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
            "medication_intake.dose FROM medication_intake " +
            "JOIN medication ON medication.id = medication_intake.medication_id " +
            "WHERE medication_intake.attack_id = :attackId " +
            "ORDER BY medication_intake.taken_epoch_millis"
    )
    fun intakesOfAttack(attackId: String): List<IntakeWithMedicationLocalModel>
}
