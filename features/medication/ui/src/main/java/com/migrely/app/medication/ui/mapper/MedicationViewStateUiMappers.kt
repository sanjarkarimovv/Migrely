package com.migrely.app.medication.ui.mapper

import com.migrely.app.medication.presentation.model.EditMedicationViewState
import com.migrely.app.medication.presentation.model.LogMedicationViewState
import com.migrely.app.medication.presentation.model.MedicationsViewState
import com.migrely.app.medication.ui.model.EditMedicationViewStateUiModel
import com.migrely.app.medication.ui.model.LogMedicationViewStateUiModel
import com.migrely.app.medication.ui.model.MedicationLogUiModel
import com.migrely.app.medication.ui.model.MedicationsViewStateUiModel

class MedicationsViewStateUiMapper(private val medicationUiMapper: MedicationUiMapper) {
    fun toUi(viewState: MedicationsViewState): MedicationsViewStateUiModel = when (viewState) {
        MedicationsViewState.Loading -> MedicationsViewStateUiModel.Loading
        is MedicationsViewState.Ready -> MedicationsViewStateUiModel.Ready(
            viewState.medications.map(medicationUiMapper::toUi)
        )
    }
}

class EditMedicationViewStateUiMapper(private val medicationUiMapper: MedicationUiMapper) {
    fun toUi(viewState: EditMedicationViewState): EditMedicationViewStateUiModel =
        when (viewState) {
            EditMedicationViewState.Loading -> EditMedicationViewStateUiModel.Loading
            is EditMedicationViewState.Ready -> EditMedicationViewStateUiModel.Ready(
                viewState.medication?.let(medicationUiMapper::toUi)
            )
            EditMedicationViewState.NotFound -> EditMedicationViewStateUiModel.NotFound
        }
}

class LogMedicationViewStateUiMapper(private val medicationUiMapper: MedicationUiMapper) {
    fun toUi(viewState: LogMedicationViewState): LogMedicationViewStateUiModel = when (viewState) {
        LogMedicationViewState.Loading -> LogMedicationViewStateUiModel.Loading
        is LogMedicationViewState.Ready -> LogMedicationViewStateUiModel.Ready(
            MedicationLogUiModel(
                defaultTimeEpochMillis = viewState.log.defaultTime.toEpochMilli(),
                zoneId = viewState.log.zone.id,
                attackId = viewState.log.attackId,
                medications = viewState.log.medications.map(medicationUiMapper::toUi)
            )
        )
    }
}
