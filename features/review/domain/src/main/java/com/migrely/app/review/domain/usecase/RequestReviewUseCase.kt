package com.migrely.app.review.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.review.domain.model.ReviewFlowResultDomainModel
import com.migrely.app.review.domain.repository.GetAttackInProgressRepository
import com.migrely.app.review.domain.repository.GetCurrentTimeRepository
import com.migrely.app.review.domain.repository.GetLastPaywallShownRepository
import com.migrely.app.review.domain.repository.LaunchReviewFlowRepository
import com.migrely.app.review.domain.repository.MarkReviewRequestedRepository

/**
 * Opens the flow Google Play prepared and, when it completes, counts the request with the time it
 * ended (BRD D131). Just before opening, it checks again that no attack has started and no paywall
 * has shown itself while Google Play prepared (BRD D130, design D3); then nothing opens or counts.
 * A flow Google Play cannot run counts as nothing. Answers whether it completed.
 */
@Suppress("LongParameterList")
class RequestReviewUseCase(
    private val getAttackInProgressRepository: GetAttackInProgressRepository,
    private val getLastPaywallShownRepository: GetLastPaywallShownRepository,
    private val launchReviewFlowRepository: LaunchReviewFlowRepository,
    private val markReviewRequestedRepository: MarkReviewRequestedRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, Boolean>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): Boolean {
        val stillCalm = longSinceLastPaywall(
            getLastPaywallShownRepository.lastPaywallShown(),
            getCurrentTimeRepository.now()
        ) && getAttackInProgressRepository.attackInProgress() == false
        if (!stillCalm) return false
        val completed = launchReviewFlowRepository.launchReviewFlow() ==
            ReviewFlowResultDomainModel.Completed
        if (completed) {
            markReviewRequestedRepository.markReviewRequested(getCurrentTimeRepository.now())
        }
        return completed
    }
}
