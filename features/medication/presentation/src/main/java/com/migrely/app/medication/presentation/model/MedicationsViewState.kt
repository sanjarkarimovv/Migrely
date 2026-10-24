package com.migrely.app.medication.presentation.model

sealed interface MedicationsViewState {
    data object Loading : MedicationsViewState

    /** [medications] may be empty: nothing added yet. */
    data class Ready(val medications: List<MedicationPresentationModel>) : MedicationsViewState
}
