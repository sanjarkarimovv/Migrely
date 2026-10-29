package com.migrely.app.review.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.review.domain.repository.PrepareReviewFlowRepository

/**
 * Has Google Play prepare its review flow, so that it can still be dropped if home is left before it
 * opens (BRD D130, change add-in-app-review design D3). Answers whether Google Play prepared it.
 */
class PrepareReviewUseCase(
    private val prepareReviewFlowRepository: PrepareReviewFlowRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, Boolean>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): Boolean =
        prepareReviewFlowRepository.prepareReviewFlow()
}
