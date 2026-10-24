package com.migrely.app.onboarding.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.onboarding.domain.model.OnboardingWriteResultDomainModel
import com.migrely.app.onboarding.domain.usecase.FinishOnboardingUseCase
import com.migrely.app.onboarding.domain.usecase.SaveAttackFrequencyUseCase
import com.migrely.app.onboarding.domain.usecase.SaveGoalUseCase
import com.migrely.app.onboarding.domain.usecase.SavePeriodTrackingUseCase
import com.migrely.app.onboarding.presentation.mapper.AttackFrequencyDomainMapper
import com.migrely.app.onboarding.presentation.mapper.OnboardingGoalDomainMapper
import com.migrely.app.onboarding.presentation.model.AttackFrequencyPresentationModel
import com.migrely.app.onboarding.presentation.model.OnboardingGoalPresentationModel
import com.migrely.app.onboarding.presentation.model.OnboardingPresentationNotification
import com.migrely.app.onboarding.presentation.model.OnboardingViewState
import com.migrely.app.onboarding.presentation.navigation.OnboardingPresentationNavigationEvent

/**
 * Onboarding (BRD ONB-F1–F3, D76–D79). The UI keeps the step and moves on as soon as an answer is chosen; the view
 * model saves it. A failed save is reported and never holds the user back, and finishing always leads home.
 */
class OnboardingViewModel(
    private val saveGoalUseCase: SaveGoalUseCase,
    private val saveAttackFrequencyUseCase: SaveAttackFrequencyUseCase,
    private val savePeriodTrackingUseCase: SavePeriodTrackingUseCase,
    private val finishOnboardingUseCase: FinishOnboardingUseCase,
    private val onboardingGoalDomainMapper: OnboardingGoalDomainMapper,
    private val attackFrequencyDomainMapper: AttackFrequencyDomainMapper,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<OnboardingViewState, OnboardingPresentationNotification>(useCaseExecutor) {
    fun onEnter() = updateViewState(OnboardingViewState.Ready)

    fun onGoalAction(goal: OnboardingGoalPresentationModel) =
        saveGoalUseCase(value = onboardingGoalDomainMapper.toDomain(goal), onResult = ::reportSave)

    fun onFrequencyAction(frequency: AttackFrequencyPresentationModel) = saveAttackFrequencyUseCase(
        value = attackFrequencyDomainMapper.toDomain(frequency),
        onResult = ::reportSave
    )

    fun onPeriodTrackingAction(on: Boolean) =
        savePeriodTrackingUseCase(value = on, onResult = ::reportSave)

    /** The last step is done or skipped. */
    fun onFinishAction() = finishOnboardingUseCase(
        value = Unit,
        onResult = { emitNavigationEvent(OnboardingPresentationNavigationEvent.OnFinished) },
        onException = { emitNavigationEvent(OnboardingPresentationNavigationEvent.OnFinished) }
    )

    fun onRestoreAction() =
        emitNavigationEvent(OnboardingPresentationNavigationEvent.OnRestoreBackup)

    private fun reportSave(result: OnboardingWriteResultDomainModel) {
        when (result) {
            OnboardingWriteResultDomainModel.Done -> Unit
            OnboardingWriteResultDomainModel.Failed -> notify(
                OnboardingPresentationNotification.NotSaved
            )
        }
    }
}
