package com.migrely.app.review.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.review.domain.model.ReviewMarksDomainModel
import com.migrely.app.review.domain.repository.GetAttackInProgressRepository
import com.migrely.app.review.domain.repository.GetCurrentTimeRepository
import com.migrely.app.review.domain.repository.GetLastPaywallShownRepository
import com.migrely.app.review.domain.repository.GetMigraineCountRepository
import com.migrely.app.review.domain.repository.GetPreviousExitRepository
import com.migrely.app.review.domain.repository.GetReviewMarksRepository
import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions

private val NOW: Instant = Instant.parse("2026-11-20T18:00:00Z")

class ShouldRequestReviewUseCaseTest {
    private val getMigraineCountRepository: GetMigraineCountRepository = mock {
        on { migraineCount() } doReturn 3
    }
    private val getLastPaywallShownRepository: GetLastPaywallShownRepository = mock {
        on { lastPaywallShown() } doReturn null
    }
    private val getPreviousExitRepository: GetPreviousExitRepository = mock {
        on { previousExitCrashed() } doReturn false
    }
    private val getReviewMarksRepository: GetReviewMarksRepository = mock {
        on { reviewMarks() } doReturn ReviewMarksDomainModel(0, null)
    }
    private val getAttackInProgressRepository: GetAttackInProgressRepository = mock {
        on { attackInProgress() } doReturn false
    }
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock {
        on { now() } doReturn NOW
    }
    private val classUnderTest = ShouldRequestReviewUseCase(
        getMigraineCountRepository,
        getLastPaywallShownRepository,
        getPreviousExitRepository,
        getReviewMarksRepository,
        getAttackInProgressRepository,
        getCurrentTimeRepository,
        CoroutineContextProvider.Default
    )

    private fun asks() = classUnderTest.executeInBackground(Unit)

    @Test
    fun `Given the 3rd migraine ended and nothing else in the way then the app asks`() {
        assertTrue(asks())
    }

    @Test
    fun `Given more migraines than 3 then the app asks`() {
        given(getMigraineCountRepository.migraineCount()).willReturn(12)

        assertTrue(asks())
    }

    @Test
    fun `Given only 2 migraines, as after deleting one of 3, then the app does not ask`() {
        given(getMigraineCountRepository.migraineCount()).willReturn(2)

        assertFalse(asks())
    }

    @Test
    fun `Given the migraines cannot be read then the app does not ask`() {
        given(getMigraineCountRepository.migraineCount()).willReturn(null)

        assertFalse(asks())
    }

    @Test
    fun `Given an attack in progress then the app does not ask`() {
        given(getAttackInProgressRepository.attackInProgress()).willReturn(true)

        assertFalse(asks())
    }

    @Test
    fun `Given whether an attack is in progress cannot be told then the app does not ask`() {
        given(getAttackInProgressRepository.attackInProgress()).willReturn(null)

        assertFalse(asks())
    }

    @Test
    fun `Given a paywall showed itself just under 3 days ago then the app does not ask`() {
        given(getLastPaywallShownRepository.lastPaywallShown())
            .willReturn(NOW.minus(Duration.ofDays(3)).plusSeconds(1))

        assertFalse(asks())
    }

    @Test
    fun `Given a paywall showed itself 3 days ago then the app asks`() {
        given(getLastPaywallShownRepository.lastPaywallShown())
            .willReturn(NOW.minus(Duration.ofDays(3)))

        assertTrue(asks())
    }

    @Test
    fun `Given the previous run ended with a crash or an ANR then the app does not ask`() {
        given(getPreviousExitRepository.previousExitCrashed()).willReturn(true)

        assertFalse(asks())
    }

    @Test
    fun `Given the last request was just under 120 days ago then the app does not ask`() {
        given(getReviewMarksRepository.reviewMarks()).willReturn(
            ReviewMarksDomainModel(1, NOW.minus(Duration.ofDays(120)).plusSeconds(1))
        )

        assertFalse(asks())
    }

    @Test
    fun `Given the last of 2 requests was 120 days ago then the app asks again`() {
        given(getReviewMarksRepository.reviewMarks())
            .willReturn(ReviewMarksDomainModel(2, NOW.minus(Duration.ofDays(120))))

        assertTrue(asks())
    }

    @Test
    fun `Given 3 requests already, however long ago, then the app never asks again`() {
        given(getReviewMarksRepository.reviewMarks())
            .willReturn(ReviewMarksDomainModel(3, NOW.minus(Duration.ofDays(900))))

        assertFalse(asks())
    }

    @Test
    fun `Given a count without a time then only the count limits the app`() {
        given(getReviewMarksRepository.reviewMarks()).willReturn(ReviewMarksDomainModel(1, null))

        assertTrue(asks())
    }

    @Test
    fun `Given an earlier rule says no then the attack in progress is not read`() {
        given(getMigraineCountRepository.migraineCount()).willReturn(1)

        asks()

        verifyNoInteractions(getAttackInProgressRepository)
    }
}
