package com.migrely.app.review.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.review.domain.usecase.PrepareReviewUseCase
import com.migrely.app.review.domain.usecase.RequestReviewUseCase
import com.migrely.app.review.domain.usecase.ShouldRequestReviewUseCase
import com.migrely.app.review.presentation.model.ReviewPromptPresentationNotification

/**
 * Home's request for a rating (BRD §14.4, D130, design D3). Each time home has settled in today's
 * mode ([onEnter]) it asks whether the rules allow a request, has Google Play prepare the flow, and
 * opens it only if home has not been left ([onLeave]) meanwhile. The flow opens at most once in this
 * view model's life, which `:app` makes the whole process (design D4). It draws nothing, so its view
 * state is [Unit].
 */
class ReviewPromptViewModel(
    private val shouldRequestReviewUseCase: ShouldRequestReviewUseCase,
    private val prepareReviewUseCase: PrepareReviewUseCase,
    private val requestReviewUseCase: RequestReviewUseCase,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<Unit, ReviewPromptPresentationNotification>(useCaseExecutor) {
    /** Whether home has settled and has not been left since. */
    private var onHome = false

    /** A round (the rules, then Google Play preparing) is under way; one at a time. */
    private var asking = false

    /**
     * Set when the flow opens, however it ends, or when Google Play cannot prepare it, and never
     * cleared: no second try in this process (EC-REV-05, EC-REV-08).
     */
    private var done = false

    fun onEnter() {
        onHome = true
        if (asking || done) return
        asking = true
        shouldRequestReviewUseCase(
            onResult = { allowed -> if (allowed) prepare() else asking = false },
            onException = { asking = false }
        )
    }

    /** Home was left: a flow still being prepared is dropped, and the next settle tries again. */
    fun onLeave() {
        onHome = false
    }

    private fun prepare() {
        prepareReviewUseCase(
            onResult = { prepared ->
                asking = false
                when {
                    !prepared -> done = true
                    onHome -> {
                        done = true
                        requestReviewUseCase()
                    }
                }
            },
            onException = { asking = false }
        )
    }
}
