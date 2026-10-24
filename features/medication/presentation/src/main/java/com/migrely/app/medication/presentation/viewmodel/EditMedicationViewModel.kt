package com.migrely.app.medication.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.medication.domain.model.MedicationLookupResultDomainModel
import com.migrely.app.medication.domain.model.SaveMedicationResultDomainModel
import com.migrely.app.medication.domain.usecase.GetMedicationUseCase
import com.migrely.app.medication.domain.usecase.SaveMedicationUseCase
import com.migrely.app.medication.presentation.mapper.MedicationDomainMapper
import com.migrely.app.medication.presentation.mapper.MedicationPresentationMapper
import com.migrely.app.medication.presentation.mapper.SaveProblemPresentationMapper
import com.migrely.app.medication.presentation.model.EditMedicationPresentationNotification
import com.migrely.app.medication.presentation.model.EditMedicationViewState
import com.migrely.app.medication.presentation.model.MedicationCategoryPresentationModel
import com.migrely.app.medication.presentation.model.MedicationKindPresentationModel
import com.migrely.app.medication.presentation.navigation.EditMedicationPresentationNavigationEvent

/** Adds or edits one medication (BRD MED-F1, MED-F2, D35). The form's text lives in the screen. */
class EditMedicationViewModel(
    private val getMedicationUseCase: GetMedicationUseCase,
    private val saveMedicationUseCase: SaveMedicationUseCase,
    private val medicationPresentationMapper: MedicationPresentationMapper,
    private val medicationDomainMapper: MedicationDomainMapper,
    private val saveProblemPresentationMapper: SaveProblemPresentationMapper,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<EditMedicationViewState, EditMedicationPresentationNotification>(
    useCaseExecutor
) {
    /** [medicationId] is null when adding a new medication. */
    fun onEnter(medicationId: String?) {
        if (medicationId == null) {
            updateViewState(EditMedicationViewState.Ready(medication = null))
            return
        }
        getMedicationUseCase(
            value = medicationId,
            onResult = { result ->
                when (result) {
                    is MedicationLookupResultDomainModel.Found -> updateViewState(
                        EditMedicationViewState.Ready(
                            medicationPresentationMapper.toPresentation(result.medication)
                        )
                    )

                    MedicationLookupResultDomainModel.NotFound ->
                        updateViewState(EditMedicationViewState.NotFound)

                    MedicationLookupResultDomainModel.Failed ->
                        notify(EditMedicationPresentationNotification.SomethingWentWrong)
                }
            }
        )
    }

    fun onSaveAction(
        medicationId: String?,
        name: String,
        kind: MedicationKindPresentationModel,
        category: MedicationCategoryPresentationModel?,
        defaultDose: String?
    ) {
        saveMedicationUseCase(
            value = medicationDomainMapper.toDomain(
                medicationId,
                name,
                kind,
                category,
                defaultDose
            ),
            onResult = { result ->
                val problem = saveProblemPresentationMapper.toPresentation(result)
                when {
                    problem != null ->
                        notify(EditMedicationPresentationNotification.NotSaved(problem))

                    result is SaveMedicationResultDomainModel.Saved ||
                        result is SaveMedicationResultDomainModel.Restored ->
                        emitNavigationEvent(EditMedicationPresentationNavigationEvent.OnLeave)

                    result == SaveMedicationResultDomainModel.StorageFull ->
                        notify(EditMedicationPresentationNotification.StorageFull)

                    else -> notify(EditMedicationPresentationNotification.SomethingWentWrong)
                }
            }
        )
    }

    fun onLeaveAction() = emitNavigationEvent(EditMedicationPresentationNavigationEvent.OnLeave)
}
