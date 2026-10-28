package com.migrely.app.review.data.repository

import com.migrely.app.datasource.appinfo.datasource.AppInfoDataSource
import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.attack.model.AttackDataModel
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.exception.FileDataException
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSource
import com.migrely.app.datasource.preferences.model.ReviewRequestsDataModel
import com.migrely.app.datasource.review.datasource.ReviewDataSource
import com.migrely.app.datasource.review.model.ReviewFlowDataModel
import com.migrely.app.review.data.mapper.ReviewFlowDomainMapper
import com.migrely.app.review.domain.model.ReviewFlowResultDomainModel
import com.migrely.app.review.domain.model.ReviewMarksDomainModel
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

private val NOW: Instant = Instant.parse("2026-11-20T18:00:00Z")

class ReviewRepositoryTest {
    private val attackDataSource: AttackDataSource = mock()
    private val preferencesDataSource: PreferencesDataSource = mock()
    private val appInfoDataSource: AppInfoDataSource = mock()
    private val clockDataSource: ClockDataSource = mock { on { now() } doReturn NOW }
    private val reviewDataSource: ReviewDataSource = mock()
    private val classUnderTest = ReviewRepository(
        attackDataSource,
        preferencesDataSource,
        appInfoDataSource,
        clockDataSource,
        reviewDataSource,
        ReviewFlowDomainMapper()
    )

    @Test
    fun `When counting migraines then every attack not deleted up to now`() {
        val attacks = List(3) { mock<AttackDataModel>() }
        given(attackDataSource.overlapping(null, Instant.EPOCH, NOW)).willReturn(attacks)

        assertEquals(3, classUnderTest.migraineCount())
    }

    @Test
    fun `Given storage fails then the count and the attack in progress are unknown`() {
        given(attackDataSource.overlapping(null, Instant.EPOCH, NOW))
            .willThrow(DatabaseDataException())
        given(attackDataSource.inProgress()).willThrow(DatabaseDataException())

        assertNull(classUnderTest.migraineCount())
        assertNull(classUnderTest.attackInProgress())
    }

    @Test
    fun `When asking about an attack in progress then whether one is going on`() {
        assertEquals(false, classUnderTest.attackInProgress())

        val attack = mock<AttackDataModel>()
        given(attackDataSource.inProgress()).willReturn(attack)

        assertEquals(true, classUnderTest.attackInProgress())
    }

    @Test
    fun `When reading the marks then the paywall's time and the requests, as instants`() {
        given(preferencesDataSource.lastPaywallShownMillis()).willReturn(NOW.toEpochMilli())
        given(preferencesDataSource.reviewRequests())
            .willReturn(ReviewRequestsDataModel(2, NOW.toEpochMilli()))

        assertEquals(NOW, classUnderTest.lastPaywallShown())
        assertEquals(ReviewMarksDomainModel(2, NOW), classUnderTest.reviewMarks())
    }

    @Test
    fun `Given nothing recorded then no paywall time and no request`() {
        given(preferencesDataSource.lastPaywallShownMillis()).willReturn(null)
        given(preferencesDataSource.reviewRequests()).willReturn(ReviewRequestsDataModel(0, null))

        assertNull(classUnderTest.lastPaywallShown())
        assertEquals(ReviewMarksDomainModel(0, null), classUnderTest.reviewMarks())
    }

    @Test
    fun `When asking how the previous run ended then the app info answers`() {
        given(appInfoDataSource.previousExitCrashed()).willReturn(true)
        assertTrue(classUnderTest.previousExitCrashed())

        given(appInfoDataSource.previousExitCrashed()).willReturn(false)
        assertFalse(classUnderTest.previousExitCrashed())
    }

    @Test
    fun `When launching the flow then Google Play's answer, for the domain`() {
        given(reviewDataSource.launchReviewFlow()).willReturn(ReviewFlowDataModel.Completed)
        assertEquals(ReviewFlowResultDomainModel.Completed, classUnderTest.launchReviewFlow())

        given(reviewDataSource.launchReviewFlow()).willReturn(ReviewFlowDataModel.Unavailable)
        assertEquals(ReviewFlowResultDomainModel.Unavailable, classUnderTest.launchReviewFlow())
    }

    @Test
    fun `When marking a request then its time in milliseconds, and a failed write is dropped`() {
        classUnderTest.markReviewRequested(NOW)
        verify(preferencesDataSource).markReviewRequested(NOW.toEpochMilli())

        given(preferencesDataSource.markReviewRequested(NOW.toEpochMilli()))
            .willThrow(FileDataException("Preferences not saved"))
        classUnderTest.markReviewRequested(NOW)
    }

    @Test
    fun `When asking the time then the clock's`() {
        assertEquals(NOW, classUnderTest.now())
    }
}
