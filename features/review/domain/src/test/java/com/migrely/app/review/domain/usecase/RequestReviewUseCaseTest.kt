package com.migrely.app.review.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.review.domain.model.ReviewFlowResultDomainModel
import com.migrely.app.review.domain.repository.GetAttackInProgressRepository
import com.migrely.app.review.domain.repository.GetCurrentTimeRepository
import com.migrely.app.review.domain.repository.GetLastPaywallShownRepository
import com.migrely.app.review.domain.repository.LaunchReviewFlowRepository
import com.migrely.app.review.domain.repository.MarkReviewRequestedRepository
import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

private val ENDED: Instant = Instant.parse("2026-11-20T18:00:05Z")

class RequestReviewUseCaseTest {
    private val getAttackInProgressRepository: GetAttackInProgressRepository = mock {
        on { attackInProgress() } doReturn false
    }
    private val getLastPaywallShownRepository: GetLastPaywallShownRepository = mock {
        on { lastPaywallShown() } doReturn null
    }
    private val launchReviewFlowRepository: LaunchReviewFlowRepository = mock()
    private val markReviewRequestedRepository: MarkReviewRequestedRepository = mock()
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock {
        on { now() } doReturn ENDED
    }
    private val classUnderTest = RequestReviewUseCase(
        getAttackInProgressRepository,
        getLastPaywallShownRepository,
        launchReviewFlowRepository,
        markReviewRequestedRepository,
        getCurrentTimeRepository,
        CoroutineContextProvider.Default
    )

    @Test
    fun `Given the flow completes then the request counts with the time it ended`() {
        given(launchReviewFlowRepository.launchReviewFlow())
            .willReturn(ReviewFlowResultDomainModel.Completed)

        assertTrue(classUnderTest.executeInBackground(Unit))
        verify(markReviewRequestedRepository).markReviewRequested(ENDED)
    }

    @Test
    fun `Given Google Play cannot run the flow then nothing counts`() {
        given(launchReviewFlowRepository.launchReviewFlow())
            .willReturn(ReviewFlowResultDomainModel.Unavailable)

        assertFalse(classUnderTest.executeInBackground(Unit))
        verify(markReviewRequestedRepository, never()).markReviewRequested(any())
    }

    @Test
    fun `Given an attack started while Google Play prepared then nothing opens or counts`() {
        given(getAttackInProgressRepository.attackInProgress()).willReturn(true)

        assertFalse(classUnderTest.executeInBackground(Unit))
        verifyNoInteractions(launchReviewFlowRepository, markReviewRequestedRepository)
    }

    @Test
    fun `Given whether an attack is in progress cannot be told then nothing opens`() {
        given(getAttackInProgressRepository.attackInProgress()).willReturn(null)

        assertFalse(classUnderTest.executeInBackground(Unit))
        verifyNoInteractions(launchReviewFlowRepository)
    }

    @Test
    fun `Given a paywall showed itself while Google Play prepared then nothing opens or counts`() {
        given(getLastPaywallShownRepository.lastPaywallShown())
            .willReturn(ENDED.minus(Duration.ofSeconds(1)))

        assertFalse(classUnderTest.executeInBackground(Unit))
        verifyNoInteractions(launchReviewFlowRepository, markReviewRequestedRepository)
    }
}
