package com.migrely.app.medication.presentation.model

sealed interface LogMedicationViewState {
    data object Loading : LogMedicationViewState

    data class Ready(val log: MedicationLogPresentationModel) : LogMedicationViewState
}
