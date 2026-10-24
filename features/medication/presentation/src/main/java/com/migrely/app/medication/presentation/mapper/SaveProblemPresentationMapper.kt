package com.migrely.app.medication.presentation.mapper

import com.migrely.app.medication.domain.model.SaveMedicationResultDomainModel
import com.migrely.app.medication.presentation.model.SaveProblemPresentationModel

/** The problem behind a refused save, or null when the result is not a refusal. */
class SaveProblemPresentationMapper {
    fun toPresentation(result: SaveMedicationResultDomainModel): SaveProblemPresentationModel? =
        when (result) {
            is SaveMedicationResultDomainModel.Duplicate ->
                SaveProblemPresentationModel.Duplicate(result.existingName)
            is SaveMedicationResultDomainModel.ArchivedName ->
                SaveProblemPresentationModel.ArchivedName(result.archivedName)
            SaveMedicationResultDomainModel.LimitReached ->
                SaveProblemPresentationModel.LimitReached
            SaveMedicationResultDomainModel.InvalidName -> SaveProblemPresentationModel.InvalidName
            SaveMedicationResultDomainModel.InvalidDose -> SaveProblemPresentationModel.InvalidDose
            is SaveMedicationResultDomainModel.Saved,
            is SaveMedicationResultDomainModel.Restored,
            SaveMedicationResultDomainModel.StorageFull,
            SaveMedicationResultDomainModel.Failed -> null
        }
}
