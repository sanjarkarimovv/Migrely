package com.migrely.app.review.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.review.domain.usecase.RequestReviewUseCase
import com.migrely.app.review.domain.usecase.ShouldRequestReviewUseCase
import com.migrely.app.review.presentation.model.ReviewPromptPresentationNotification

/**
 * Home's request for a rating (BRD §14.4, D130, design D3). Each time home has settled in today's
 * mode it asks whether the rules allow a request, and runs Google Play's flow when they do. The
 * flow runs at most once in this view model's life, which `:app` makes the whole process (design
 * D4). It draws nothing, so its view state is [Unit].
 */
class ReviewPromptViewModel(
    private val shouldRequestReviewUseCase: ShouldRequestReviewUseCase,
    private val requestReviewUseCase: RequestReviewUseCase,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<Unit, ReviewPromptPresentationNotification>(useCaseExecutor) {
    /** Home loads several times in a row; one question at a time launches one flow. */
    private var asking = false

    /** Set when the flow is launched, however it ends, and never cleared (EC-REV-05, EC-REV-08). */
    private var launched = false

    fun onEnter() {
        if (asking || launched) return
        asking = true
        shouldRequestReviewUseCase(
            onResult = { allowed ->
                asking = false
                if (allowed) {
                    launched = true
                    requestReviewUseCase()
                }
            },
            onException = { asking = false }
        )
    }
}
