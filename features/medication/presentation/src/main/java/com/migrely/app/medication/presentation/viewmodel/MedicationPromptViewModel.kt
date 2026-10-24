package com.migrely.app.medication.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.medication.domain.model.AcknowledgeOveruseRequestDomainModel
import com.migrely.app.medication.domain.model.AnswerEffectRequestDomainModel
import com.migrely.app.medication.domain.usecase.AcknowledgeOveruseUseCase
import com.migrely.app.medication.domain.usecase.AnswerEffectUseCase
import com.migrely.app.medication.domain.usecase.GetMedicationPromptUseCase
import com.migrely.app.medication.domain.usecase.SkipEffectQuestionUseCase
import com.migrely.app.medication.presentation.mapper.MedicationPromptDomainMapper
import com.migrely.app.medication.presentation.mapper.MedicationPromptPresentationMapper
import com.migrely.app.medication.presentation.model.IntakeEffectPresentationModel
import com.migrely.app.medication.presentation.model.MedicationPromptPresentationNotification
import com.migrely.app.medication.presentation.model.MedicationPromptViewState
import com.migrely.app.medication.presentation.model.OveruseCategoryPresentationModel
import com.migrely.app.medication.presentation.model.OveruseLevelPresentationModel

/**
 * Home's one medication card (BRD D46): an overuse warning or "Did it help?" (MED-F4, MED-F5). It enters again every time
 * home loads, and loads again after every answer, skip or "Got it", so the next card follows at once. A failed write
 * loads the same card again, so the user can retry.
 */
class MedicationPromptViewModel(
    private val getMedicationPromptUseCase: GetMedicationPromptUseCase,
    private val answerEffectUseCase: AnswerEffectUseCase,
    private val skipEffectQuestionUseCase: SkipEffectQuestionUseCase,
    private val acknowledgeOveruseUseCase: AcknowledgeOveruseUseCase,
    private val medicationPromptPresentationMapper: MedicationPromptPresentationMapper,
    private val medicationPromptDomainMapper: MedicationPromptDomainMapper,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<MedicationPromptViewState, MedicationPromptPresentationNotification>(
    useCaseExecutor
) {
    fun onEnter() = loadPrompt()

    fun onEffectAction(intakeId: String, effect: IntakeEffectPresentationModel) {
        answerEffectUseCase(
            value = AnswerEffectRequestDomainModel(
                intakeId,
                medicationPromptDomainMapper.toDomain(effect)
            ),
            onResult = { loadPrompt() }
        )
    }

    fun onSkipEffectAction(intakeId: String) {
        skipEffectQuestionUseCase(value = intakeId, onResult = { loadPrompt() })
    }

    fun onOveruseSeenAction(
        category: OveruseCategoryPresentationModel,
        level: OveruseLevelPresentationModel
    ) {
        acknowledgeOveruseUseCase(
            value = AcknowledgeOveruseRequestDomainModel(
                medicationPromptDomainMapper.toDomain(category),
                medicationPromptDomainMapper.toDomain(level)
            ),
            onResult = { loadPrompt() }
        )
    }

    private fun loadPrompt() {
        getMedicationPromptUseCase(
            onResult = { prompt ->
                updateViewState(medicationPromptPresentationMapper.toPresentation(prompt))
            }
        )
    }
}
