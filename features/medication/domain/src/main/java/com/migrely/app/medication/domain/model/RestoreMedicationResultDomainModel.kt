package com.migrely.app.medication.domain.model

sealed interface RestoreMedicationResultDomainModel {
    data object Restored : RestoreMedicationResultDomainModel

    /** Bringing it back would make more than 50 active medications (BRD D35). */
    data object LimitReached : RestoreMedicationResultDomainModel

    data object StorageFull : RestoreMedicationResultDomainModel

    data object Failed : RestoreMedicationResultDomainModel
}
