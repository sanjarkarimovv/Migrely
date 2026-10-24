package com.migrely.app.medication.presentation.model

sealed interface EditMedicationViewState {
    data object Loading : EditMedicationViewState

    /** [medication] is null when adding a new one. */
    data class Ready(val medication: MedicationPresentationModel?) : EditMedicationViewState

    data object NotFound : EditMedicationViewState
}
