package com.migrely.app.medication.domain.model

sealed interface SaveMedicationResultDomainModel {
    data class Saved(val medication: MedicationDomainModel) : SaveMedicationResultDomainModel

    /** An archived medication with the same name was brought back (BRD EC-MED-07). */
    data class Restored(val medication: MedicationDomainModel) : SaveMedicationResultDomainModel

    /** Another active medication already has this name, ignoring case (BRD EC-MED-07). */
    data class Duplicate(val existingName: String) : SaveMedicationResultDomainModel

    /** Renaming to the name of an archived medication, which only adding it again brings back (EC-MED-07). */
    data class ArchivedName(val archivedName: String) : SaveMedicationResultDomainModel

    /** 50 medications are active already (BRD D35). */
    data object LimitReached : SaveMedicationResultDomainModel

    data object InvalidName : SaveMedicationResultDomainModel

    data object InvalidDose : SaveMedicationResultDomainModel

    data object StorageFull : SaveMedicationResultDomainModel

    data object Failed : SaveMedicationResultDomainModel
}
