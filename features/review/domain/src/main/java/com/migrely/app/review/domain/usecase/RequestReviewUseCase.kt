package com.migrely.app.review.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.review.domain.model.ReviewFlowResultDomainModel
import com.migrely.app.review.domain.repository.GetCurrentTimeRepository
import com.migrely.app.review.domain.repository.LaunchReviewFlowRepository
import com.migrely.app.review.domain.repository.MarkReviewRequestedRepository

/**
 * Runs Google Play's review flow and, when it completes, counts the request with the time it ended
 * (BRD D131). A flow Google Play cannot run counts as nothing. Answers whether it completed.
 */
class RequestReviewUseCase(
    private val launchReviewFlowRepository: LaunchReviewFlowRepository,
    private val markReviewRequestedRepository: MarkReviewRequestedRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, Boolean>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): Boolean {
        val completed = launchReviewFlowRepository.launchReviewFlow() ==
            ReviewFlowResultDomainModel.Completed
        if (completed) {
            markReviewRequestedRepository.markReviewRequested(getCurrentTimeRepository.now())
        }
        return completed
    }
}
