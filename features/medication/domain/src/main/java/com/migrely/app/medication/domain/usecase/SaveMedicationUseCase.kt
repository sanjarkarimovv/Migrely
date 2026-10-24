package com.migrely.app.medication.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.ActiveCountResultDomainModel
import com.migrely.app.medication.domain.model.MedicationKindDomainModel
import com.migrely.app.medication.domain.model.MedicationLookupResultDomainModel
import com.migrely.app.medication.domain.model.MedicationWriteResultDomainModel
import com.migrely.app.medication.domain.model.SaveMedicationRequestDomainModel
import com.migrely.app.medication.domain.model.SaveMedicationResultDomainModel
import com.migrely.app.medication.domain.repository.CountActiveMedicationsRepository
import com.migrely.app.medication.domain.repository.CreateMedicationRepository
import com.migrely.app.medication.domain.repository.FindMedicationByNameRepository
import com.migrely.app.medication.domain.repository.UpdateMedicationRepository
import java.util.Locale

const val MAXIMUM_MEDICATION_NAME_LENGTH = 60
const val MAXIMUM_DOSE_LENGTH = 20
const val MAXIMUM_ACTIVE_MEDICATIONS = 50

/**
 * Adds or edits a medication (BRD MED-F1, MED-F2, D35). Names are trimmed and unique ignoring case; adding the name of
 * an archived medication brings it back with its history (EC-MED-07). Preventive medications have no category.
 */
class SaveMedicationUseCase(
    private val findMedicationByNameRepository: FindMedicationByNameRepository,
    private val countActiveMedicationsRepository: CountActiveMedicationsRepository,
    private val createMedicationRepository: CreateMedicationRepository,
    private val updateMedicationRepository: UpdateMedicationRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<SaveMedicationRequestDomainModel, SaveMedicationResultDomainModel>(
    coroutineContextProvider
) {
    override fun executeInBackground(
        request: SaveMedicationRequestDomainModel
    ): SaveMedicationResultDomainModel {
        val name = request.name.trim()
        val dose = request.defaultDose?.trim()?.takeIf(String::isNotEmpty)
        if (name.isEmpty() || name.length > MAXIMUM_MEDICATION_NAME_LENGTH) {
            return SaveMedicationResultDomainModel.InvalidName
        }
        if (dose != null && dose.length > MAXIMUM_DOSE_LENGTH) {
            return SaveMedicationResultDomainModel.InvalidDose
        }
        val nameKey = name.lowercase(Locale.ROOT)
        val category = request.category.takeIf { request.kind == MedicationKindDomainModel.Acute }
        val existing = when (
            val lookup = findMedicationByNameRepository.medicationByNameKey(
                nameKey
            )
        ) {
            is MedicationLookupResultDomainModel.Found -> lookup.medication
            MedicationLookupResultDomainModel.NotFound -> null
            MedicationLookupResultDomainModel.Failed ->
                return SaveMedicationResultDomainModel.Failed
        }
        val editedId = request.id
        // An archived name can be taken back by a new medication, never by renaming another one.
        val clashes = existing != null && existing.id != editedId &&
            (editedId != null || !existing.archived)
        return when {
            clashes -> SaveMedicationResultDomainModel.Duplicate(checkNotNull(existing).name)

            editedId != null -> updateMedicationRepository.updateMedication(
                editedId,
                name,
                nameKey,
                request.kind,
                category,
                dose
            ).toSaveResult(restored = false)

            existing != null -> whenUnderLimit {
                updateMedicationRepository.updateMedication(
                    existing.id,
                    name,
                    nameKey,
                    request.kind,
                    category,
                    dose
                ).toSaveResult(restored = true)
            }

            else -> whenUnderLimit {
                createMedicationRepository.createMedication(
                    name,
                    nameKey,
                    request.kind,
                    category,
                    dose
                ).toSaveResult(restored = false)
            }
        }
    }

    private fun whenUnderLimit(
        save: () -> SaveMedicationResultDomainModel
    ): SaveMedicationResultDomainModel =
        when (val count = countActiveMedicationsRepository.activeMedicationCount()) {
            is ActiveCountResultDomainModel.Counted ->
                if (count.count >= MAXIMUM_ACTIVE_MEDICATIONS) {
                    SaveMedicationResultDomainModel.LimitReached
                } else {
                    save()
                }

            ActiveCountResultDomainModel.Failed -> SaveMedicationResultDomainModel.Failed
        }

    private fun MedicationWriteResultDomainModel.toSaveResult(
        restored: Boolean
    ): SaveMedicationResultDomainModel = when (this) {
        is MedicationWriteResultDomainModel.Done -> if (restored) {
            SaveMedicationResultDomainModel.Restored(medication)
        } else {
            SaveMedicationResultDomainModel.Saved(medication)
        }

        MedicationWriteResultDomainModel.StorageFull -> SaveMedicationResultDomainModel.StorageFull
        MedicationWriteResultDomainModel.Failed -> SaveMedicationResultDomainModel.Failed
    }
}
