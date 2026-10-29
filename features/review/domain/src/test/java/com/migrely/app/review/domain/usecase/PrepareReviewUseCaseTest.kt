package com.migrely.app.review.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.review.domain.repository.PrepareReviewFlowRepository
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

class PrepareReviewUseCaseTest {
    private val prepareReviewFlowRepository: PrepareReviewFlowRepository = mock()
    private val classUnderTest =
        PrepareReviewUseCase(prepareReviewFlowRepository, CoroutineContextProvider.Default)

    @Test
    fun `When preparing then whether Google Play prepared the flow`() {
        given(prepareReviewFlowRepository.prepareReviewFlow()).willReturn(true)
        assertTrue(classUnderTest.executeInBackground(Unit))

        given(prepareReviewFlowRepository.prepareReviewFlow()).willReturn(false)
        assertFalse(classUnderTest.executeInBackground(Unit))
    }
}
