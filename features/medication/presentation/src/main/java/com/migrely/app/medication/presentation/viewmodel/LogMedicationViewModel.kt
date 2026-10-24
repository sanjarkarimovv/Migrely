package com.migrely.app.medication.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.medication.domain.model.LogIntakeRequestDomainModel
import com.migrely.app.medication.domain.model.LogIntakeResultDomainModel
import com.migrely.app.medication.domain.model.MedicationDomainModel
import com.migrely.app.medication.domain.model.MedicationLogResultDomainModel
import com.migrely.app.medication.domain.model.SaveMedicationResultDomainModel
import com.migrely.app.medication.domain.model.WriteResultDomainModel
import com.migrely.app.medication.domain.usecase.GetMedicationLogUseCase
import com.migrely.app.medication.domain.usecase.LogIntakeUseCase
import com.migrely.app.medication.domain.usecase.SaveMedicationUseCase
import com.migrely.app.medication.domain.usecase.UndoIntakeUseCase
import com.migrely.app.medication.presentation.mapper.IntakePresentationMapper
import com.migrely.app.medication.presentation.mapper.MedicationDomainMapper
import com.migrely.app.medication.presentation.mapper.MedicationPresentationMapper
import com.migrely.app.medication.presentation.mapper.SaveProblemPresentationMapper
import com.migrely.app.medication.presentation.model.LogMedicationPresentationNotification
import com.migrely.app.medication.presentation.model.LogMedicationPresentationNotification.SomethingWentWrong
import com.migrely.app.medication.presentation.model.LogMedicationViewState
import com.migrely.app.medication.presentation.model.MedicationCategoryPresentationModel
import com.migrely.app.medication.presentation.model.MedicationKindPresentationModel
import com.migrely.app.medication.presentation.model.MedicationLogPresentationModel
import com.migrely.app.medication.presentation.model.MedicationPresentationModel
import com.migrely.app.medication.presentation.navigation.LogMedicationPresentationNavigationEvent
import java.time.Instant

/**
 * "Medication taken" (BRD MED-F3, D34, EC-MED-08). Stateless: the screen passes the attack, the picked time and the
 * intake to undo with every action.
 */
@Suppress("LongParameterList")
class LogMedicationViewModel(
    private val getMedicationLogUseCase: GetMedicationLogUseCase,
    private val logIntakeUseCase: LogIntakeUseCase,
    private val undoIntakeUseCase: UndoIntakeUseCase,
    private val saveMedicationUseCase: SaveMedicationUseCase,
    private val medicationPresentationMapper: MedicationPresentationMapper,
    private val medicationDomainMapper: MedicationDomainMapper,
    private val intakePresentationMapper: IntakePresentationMapper,
    private val saveProblemPresentationMapper: SaveProblemPresentationMapper,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<LogMedicationViewState, LogMedicationPresentationNotification>(useCaseExecutor) {
    /** [attackId] is the attack the screen was opened for, or null. */
    fun onEnter(attackId: String?) {
        getMedicationLogUseCase(
            value = attackId,
            onResult = { result ->
                when (result) {
                    is MedicationLogResultDomainModel.Loaded -> updateViewState(
                        LogMedicationViewState.Ready(
                            MedicationLogPresentationModel(
                                defaultTime = result.log.defaultTime,
                                zone = result.log.zone,
                                attackId = result.log.attackId,
                                medications = result.log.medications.map(
                                    medicationPresentationMapper::toPresentation
                                )
                            )
                        )
                    )

                    MedicationLogResultDomainModel.Failed -> notify(SomethingWentWrong)
                }
            }
        )
    }

    fun onMedicationAction(
        medication: MedicationPresentationModel,
        attackId: String?,
        takenAt: Instant
    ) = logIntake(medication.id, medication.name, medication.defaultDose, attackId, takenAt)

    /** Adds the first medication as acute and logs it at once (BRD EC-MED-08). */
    fun onAddAndLogAction(
        name: String,
        category: MedicationCategoryPresentationModel?,
        attackId: String?,
        takenAt: Instant
    ) {
        saveMedicationUseCase(
            value = medicationDomainMapper.toDomain(
                medicationId = null,
                name = name,
                kind = MedicationKindPresentationModel.Acute,
                category = category,
                defaultDose = null
            ),
            onResult = { result ->
                val saved = when (result) {
                    is SaveMedicationResultDomainModel.Saved -> result.medication
                    is SaveMedicationResultDomainModel.Restored -> result.medication
                    else -> null
                }
                if (saved != null) {
                    logSaved(saved, attackId, takenAt)
                } else {
                    reportSaveProblem(result)
                }
            }
        )
    }

    fun onUndoAction(intakeId: String, attackId: String?) {
        undoIntakeUseCase(
            value = intakeId,
            onResult = { result ->
                when (result) {
                    WriteResultDomainModel.Done -> {
                        notify(LogMedicationPresentationNotification.IntakeUndone)
                        onEnter(attackId)
                    }

                    WriteResultDomainModel.StorageFull ->
                        notify(LogMedicationPresentationNotification.StorageFull)

                    WriteResultDomainModel.Failed -> notify(SomethingWentWrong)
                }
            }
        )
    }

    fun onManageAction() =
        emitNavigationEvent(LogMedicationPresentationNavigationEvent.OnManageMedications)

    fun onDoneAction() = emitNavigationEvent(LogMedicationPresentationNavigationEvent.OnDone)

    private fun logSaved(medication: MedicationDomainModel, attackId: String?, takenAt: Instant) =
        logIntake(medication.id, medication.name, medication.defaultDose, attackId, takenAt)

    private fun reportSaveProblem(result: SaveMedicationResultDomainModel) {
        val problem = saveProblemPresentationMapper.toPresentation(result)
        notify(
            when {
                problem != null -> LogMedicationPresentationNotification.NotSaved(problem)
                result == SaveMedicationResultDomainModel.StorageFull ->
                    LogMedicationPresentationNotification.StorageFull
                else -> SomethingWentWrong
            }
        )
    }

    private fun logIntake(
        medicationId: String,
        medicationName: String,
        dose: String?,
        attackId: String?,
        takenAt: Instant
    ) {
        logIntakeUseCase(
            value = LogIntakeRequestDomainModel(
                medicationId,
                medicationName,
                dose,
                attackId,
                takenAt
            ),
            onResult = { result ->
                notify(
                    when (result) {
                        is LogIntakeResultDomainModel.Logged ->
                            LogMedicationPresentationNotification.Logged(
                                intakePresentationMapper.toPresentation(result.intake)
                            )

                        LogIntakeResultDomainModel.InFuture ->
                            LogMedicationPresentationNotification.InFuture

                        LogIntakeResultDomainModel.StorageFull ->
                            LogMedicationPresentationNotification.StorageFull

                        LogIntakeResultDomainModel.Failed -> SomethingWentWrong
                    }
                )
            }
        )
    }
}
