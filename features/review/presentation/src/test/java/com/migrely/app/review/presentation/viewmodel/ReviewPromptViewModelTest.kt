package com.migrely.app.review.presentation.viewmodel

import com.migrely.app.architecture.domain.exception.DomainException
import com.migrely.app.architecture.domain.exception.UnknownDomainException
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.review.domain.usecase.RequestReviewUseCase
import com.migrely.app.review.domain.usecase.ShouldRequestReviewUseCase
import com.migrely.app.review.presentation.model.ReviewPromptPresentationNotification
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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

    @Mock private lateinit var requestReviewUseCase: RequestReviewUseCase

    @Before
    fun setUp() {
        classUnderTest = ReviewPromptViewModel(
            shouldRequestReviewUseCase,
            requestReviewUseCase,
            useCaseExecutor
        )
    }

    private fun verifyAsked(times: Int) = verify(useCaseExecutor, times(times))
        .execute(eq(shouldRequestReviewUseCase), any<(Boolean) -> Unit>(), anyException())

    private fun verifyLaunched(times: Int) = verify(useCaseExecutor, times(times))
        .execute(eq(requestReviewUseCase), any<(Boolean) -> Unit>(), anyException())

    private fun anyException() = any<(DomainException) -> Unit>()

    @Test
    fun `Given the rules allow it when home has settled then the flow is launched`() {
        shouldRequestReviewUseCase.givenSuccessfulExecution(true)

        classUnderTest.onEnter()

        verifyLaunched(1)
    }

    @Test
    fun `Given the rules do not allow it when home has settled then nothing is launched`() {
        shouldRequestReviewUseCase.givenSuccessfulExecution(false)

        classUnderTest.onEnter()

        verify(useCaseExecutor, never())
            .execute(eq(requestReviewUseCase), any<(Boolean) -> Unit>(), anyException())
    }

    @Test
    fun `Given the rules said no when home settles again then the rules are asked again`() {
        shouldRequestReviewUseCase.givenSuccessfulExecution(false)

        classUnderTest.onEnter()
        classUnderTest.onEnter()

        verifyAsked(2)
    }

    @Test
    fun `Given a question is open when home settles again then it is not asked twice`() {
        classUnderTest.onEnter()
        classUnderTest.onEnter()

        verifyAsked(1)
    }

    @Test
    fun `Given the flow was launched when home settles again then nothing more is asked`() {
        shouldRequestReviewUseCase.givenSuccessfulExecution(true)

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
