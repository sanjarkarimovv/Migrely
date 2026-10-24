package com.migrely.app.datasource.medication.datasource

import com.migrely.app.datasource.medication.model.AcuteIntakeDataModel
import com.migrely.app.datasource.medication.model.IntakeDataModel
import com.migrely.app.datasource.medication.model.IntakeEffectDataModel
import com.migrely.app.datasource.medication.model.MedicationCategoryDataModel
import com.migrely.app.datasource.medication.model.MedicationDataModel
import com.migrely.app.datasource.medication.model.MedicationKindDataModel
import com.migrely.app.datasource.medication.model.OveruseCategoryDataModel
import com.migrely.app.datasource.medication.model.OveruseLevelDataModel
import com.migrely.app.datasource.medication.model.OveruseNoticeDataModel
import com.migrely.app.datasource.medication.model.PendingEffectQuestionDataModel
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

    /** Replaces its fields, never its archived flag; null when it does not exist. */
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

    /**
     * The oldest intake of an acute medication taken in the half-open window [[takenFrom], [takenTo]) that has no
     * answer, was not skipped and is not in an attack being deleted (BRD MED-F4, EC-MED-05, EC-MED-11).
     */
    fun pendingEffectQuestion(takenFrom: Instant, takenTo: Instant): PendingEffectQuestionDataModel?

    /** [intakeId]'s question when it is still open by the same rules, or null (BRD D67). */
    fun pendingEffectQuestionOf(
        intakeId: String,
        takenFrom: Instant,
        takenTo: Instant
    ): PendingEffectQuestionDataModel?

    /** Stores the answer, or clears it when null, and closes the question (BRD D39, EC-MED-12). */
    fun setIntakeEffect(intakeId: String, effect: IntakeEffectDataModel?)

    /** Closes the question without an answer. */
    fun closeEffectQuestion(intakeId: String)

    /** Acute intakes taken in the half-open window [[from], [to]), outside attacks being deleted (BRD D45). */
    fun acuteIntakesTakenBetween(from: Instant, to: Instant): List<AcuteIntakeDataModel>

    /** The last "Got it" of each overuse category and level (BRD EC-MED-06). */
    fun overuseNotices(): List<OveruseNoticeDataModel>

    fun acknowledgeOveruse(
        category: OveruseCategoryDataModel,
        level: OveruseLevelDataModel,
        acknowledgedAt: Instant
    )
}
