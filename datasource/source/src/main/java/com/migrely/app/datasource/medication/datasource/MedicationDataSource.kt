package com.migrely.app.datasource.medication.datasource

import com.migrely.app.datasource.medication.model.IntakeDataModel
import com.migrely.app.datasource.medication.model.MedicationCategoryDataModel
import com.migrely.app.datasource.medication.model.MedicationDataModel
import com.migrely.app.datasource.medication.model.MedicationKindDataModel
import java.time.Instant

/**
 * The user's medications and the intakes logged for them (BRD MED-F1 to MED-F3). Medications are never deleted, only
 * archived, so every intake can name its medication (EC-MED-01). An attack's intakes go with it when it is removed for
 * good (EC-MED-10). Every function blocks and throws the storage exceptions described on
 * [com.migrely.app.datasource.attack.datasource.AttackDataSource].
 */
@Suppress("TooManyFunctions")
interface MedicationDataSource {
    /** Active medications, most recently taken first, then by name. */
    fun activeMedications(): List<MedicationDataModel>

    fun medication(medicationId: String): MedicationDataModel?

    /** The medication, archived ones included, whose trimmed lower-case name is [nameKey]. */
    fun medicationByNameKey(nameKey: String): MedicationDataModel?

    fun activeMedicationCount(): Int

    fun createMedication(
        name: String,
        nameKey: String,
        kind: MedicationKindDataModel,
        category: MedicationCategoryDataModel?,
        defaultDose: String?
    ): MedicationDataModel

    /** Replaces every field and makes the medication active; null when it does not exist. */
    @Suppress("LongParameterList")
    fun updateMedication(
        medicationId: String,
        name: String,
        nameKey: String,
        kind: MedicationKindDataModel,
        category: MedicationCategoryDataModel?,
        defaultDose: String?
    ): MedicationDataModel?

    fun setArchived(medicationId: String, archived: Boolean)

    /** Stores a new intake and returns its id. */
    fun addIntake(
        medicationId: String,
        attackId: String?,
        takenAt: Instant,
        takenZoneId: String,
        dose: String?
    ): String

    /** Stores [intake] again with its own id, for "Undo". */
    fun restoreIntake(intake: IntakeDataModel)

    fun deleteIntake(intakeId: String)

    /** The intakes of [attackId], oldest first. */
    fun intakesOfAttack(attackId: String): List<IntakeDataModel>
}
