package com.migrely.app.review.presentation.viewmodel

import com.migrely.app.architecture.domain.exception.DomainException
import com.migrely.app.architecture.domain.exception.UnknownDomainException
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.review.domain.usecase.PrepareReviewUseCase
import com.migrely.app.review.domain.usecase.RequestReviewUseCase
import com.migrely.app.review.domain.usecase.ShouldRequestReviewUseCase
import com.migrely.app.review.presentation.model.ReviewPromptPresentationNotification
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.willAnswer
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
class ReviewPromptViewModelTest :
    BaseViewModelTest<Unit, ReviewPromptPresentationNotification, ReviewPromptViewModel>() {
    @Mock private lateinit var shouldRequestReviewUseCase: ShouldRequestReviewUseCase

    @Mock private lateinit var prepareReviewUseCase: PrepareReviewUseCase

    @Mock private lateinit var requestReviewUseCase: RequestReviewUseCase

    @Before
    fun setUp() {
        classUnderTest = ReviewPromptViewModel(
            shouldRequestReviewUseCase,
            prepareReviewUseCase,
            requestReviewUseCase,
            useCaseExecutor
        )
    }

    private fun anyException() = any<(DomainException) -> Unit>()

    private fun verifyAsked(times: Int) = verify(useCaseExecutor, times(times))
        .execute(eq(shouldRequestReviewUseCase), any<(Boolean) -> Unit>(), anyException())

    private fun verifyPrepared(times: Int) = verify(useCaseExecutor, times(times))
        .execute(eq(prepareReviewUseCase), any<(Boolean) -> Unit>(), anyException())

    private fun verifyLaunched(times: Int) = verify(useCaseExecutor, times(times))
        .execute(eq(requestReviewUseCase), any<(Boolean) -> Unit>(), anyException())

    /** Google Play prepares the flow, and [meanwhile] happens before it answers. */
    private fun givenPreparedAfter(prepared: Boolean, meanwhile: () -> Unit = {}) {
        willAnswer { invocation ->
            meanwhile()
            invocation.getArgument<(Boolean) -> Unit>(1)(prepared)
        }.given(useCaseExecutor)
            .execute(eq(prepareReviewUseCase), any<(Boolean) -> Unit>(), anyException())
    }

    @Test
    fun `Given the rules allow it and Google Play prepares while home stays then the flow opens`() {
        shouldRequestReviewUseCase.givenSuccessfulExecution(true)
        givenPreparedAfter(true)

        classUnderTest.onEnter()

        verifyLaunched(1)
    }

    @Test
    fun `Given the rules do not allow it then nothing is prepared or opened`() {
        shouldRequestReviewUseCase.givenSuccessfulExecution(false)

        classUnderTest.onEnter()

        verifyPrepared(0)
        verify(useCaseExecutor, never())
            .execute(eq(requestReviewUseCase), any<(Boolean) -> Unit>(), anyException())
    }

    @Test
    fun `Given home was left while Google Play prepared then nothing opens, and it tries again`() {
        shouldRequestReviewUseCase.givenSuccessfulExecution(true)
        givenPreparedAfter(true) { classUnderTest.onLeave() }

        classUnderTest.onEnter()
        verifyLaunched(0)

        givenPreparedAfter(true)
        classUnderTest.onEnter()
        verifyAsked(2)
        verifyLaunched(1)
    }

    @Test
    fun `Given Google Play cannot prepare the flow then it is never tried again in this process`() {
        shouldRequestReviewUseCase.givenSuccessfulExecution(true)
        givenPreparedAfter(false)

        classUnderTest.onEnter()
        classUnderTest.onEnter()

        verifyAsked(1)
        verifyLaunched(0)
    }

    @Test
    fun `Given the rules said no when home settles again then the rules are asked again`() {
        shouldRequestReviewUseCase.givenSuccessfulExecution(false)

        classUnderTest.onEnter()
        classUnderTest.onEnter()

        verifyAsked(2)
    }

    @Test
    fun `Given a round is under way when home settles again then it is not asked twice`() {
        classUnderTest.onEnter()
        classUnderTest.onEnter()

        verifyAsked(1)
    }

    @Test
    fun `Given the flow opened when home settles again then nothing more is asked`() {
        shouldRequestReviewUseCase.givenSuccessfulExecution(true)
        givenPreparedAfter(true)

        classUnderTest.onEnter()
        classUnderTest.onEnter()
        classUnderTest.onEnter()

        verifyAsked(1)
        verifyLaunched(1)
    }

    @Test
    fun `Given the question failed when home settles again then it is asked again`() {
        shouldRequestReviewUseCase.givenFailedExecution(UnknownDomainException())

        classUnderTest.onEnter()
        classUnderTest.onEnter()

        verifyAsked(2)
    }
}
