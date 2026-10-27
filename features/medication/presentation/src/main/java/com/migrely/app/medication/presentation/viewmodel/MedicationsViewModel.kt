package com.migrely.app.medication.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.medication.domain.model.MedicationsResultDomainModel
import com.migrely.app.medication.domain.model.RestoreMedicationResultDomainModel
import com.migrely.app.medication.domain.model.WriteResultDomainModel
import com.migrely.app.medication.domain.usecase.ArchiveMedicationUseCase
import com.migrely.app.medication.domain.usecase.GetMedicationsUseCase
import com.migrely.app.medication.domain.usecase.RestoreMedicationUseCase
import com.migrely.app.medication.presentation.mapper.MedicationPresentationMapper
import com.migrely.app.medication.presentation.model.MedicationsPresentationNotification
import com.migrely.app.medication.presentation.model.MedicationsViewState
import com.migrely.app.medication.presentation.navigation.MedicationsPresentationNavigationEvent

/** My medications (BRD MED-F1, EC-MED-01). Stateless: the list is loaded on enter, on resume and after every change. */
class MedicationsViewModel(
    private val getMedicationsUseCase: GetMedicationsUseCase,
    private val archiveMedicationUseCase: ArchiveMedicationUseCase,
    private val restoreMedicationUseCase: RestoreMedicationUseCase,
    private val medicationPresentationMapper: MedicationPresentationMapper,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<MedicationsViewState, MedicationsPresentationNotification>(useCaseExecutor) {
    fun onEnter() = loadMedications()

    fun onResume() = loadMedications()

    fun onAddAction() = emitNavigationEvent(MedicationsPresentationNavigationEvent.OnAddMedication)

    fun onMedicationAction(medicationId: String) =
        emitNavigationEvent(MedicationsPresentationNavigationEvent.OnEditMedication(medicationId))

    fun onArchiveAction(medicationId: String) {
        archiveMedicationUseCase(
            value = medicationId,
            onResult = { result ->
                reportWrite(result) {
                    notify(MedicationsPresentationNotification.Archived(medicationId))
                }
            }
        )
    }

    fun onUndoArchiveAction(medicationId: String) {
        restoreMedicationUseCase(
            value = medicationId,
            onResult = { result ->
                when (result) {
                    RestoreMedicationResultDomainModel.Restored -> {
                        notify(MedicationsPresentationNotification.Undone)
                        loadMedications()
                    }

                    RestoreMedicationResultDomainModel.LimitReached ->
                        notify(MedicationsPresentationNotification.LimitReached)

                    RestoreMedicationResultDomainModel.StorageFull ->
                        notify(MedicationsPresentationNotification.StorageFull)

                    RestoreMedicationResultDomainModel.Failed ->
                        notify(MedicationsPresentationNotification.SomethingWentWrong)
                }
            }
        )
    }

    fun onLeaveAction() = emitNavigationEvent(MedicationsPresentationNavigationEvent.OnLeave)

    private fun reportWrite(result: WriteResultDomainModel, onDone: () -> Unit) {
        when (result) {
            WriteResultDomainModel.Done -> {
                onDone()
                loadMedications()
            }

            WriteResultDomainModel.StorageFull ->
                notify(MedicationsPresentationNotification.StorageFull)

            WriteResultDomainModel.Failed ->
                notify(MedicationsPresentationNotification.SomethingWentWrong)
        }
    }

    private fun loadMedications() {
        getMedicationsUseCase(
            onResult = { result ->
                when (result) {
                    is MedicationsResultDomainModel.Loaded -> updateViewState(
                        MedicationsViewState.Ready(
                            result.medications.map(medicationPresentationMapper::toPresentation)
                        )
                    )

                    MedicationsResultDomainModel.Failed ->
                        notify(MedicationsPresentationNotification.SomethingWentWrong)
                }
            }
        )
    }
}
