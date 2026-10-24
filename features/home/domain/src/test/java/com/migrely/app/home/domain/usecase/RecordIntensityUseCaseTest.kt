package com.migrely.app.home.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.home.domain.model.AttackLookupResultDomainModel
import com.migrely.app.home.domain.model.CurrentTimeDomainModel
import com.migrely.app.home.domain.model.IntensityLevelDomainModel
import com.migrely.app.home.domain.model.IntensityPointDomainModel
import com.migrely.app.home.domain.model.RecordIntensityResultDomainModel
import com.migrely.app.home.domain.model.TASHKENT
import com.migrely.app.home.domain.model.WriteResultDomainModel
import com.migrely.app.home.domain.model.attack
import com.migrely.app.home.domain.repository.AddIntensityPointRepository
import com.migrely.app.home.domain.repository.GetAttackInProgressRepository
import com.migrely.app.home.domain.repository.GetCurrentTimeRepository
import com.migrely.app.home.domain.repository.ReplaceLatestIntensityPointRepository
import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

private val NOW: Instant = Instant.parse("2026-10-23T10:00:00Z")

class RecordIntensityUseCaseTest {
    private lateinit var classUnderTest: RecordIntensityUseCase
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val getAttackInProgressRepository: GetAttackInProgressRepository = mock()
    private val addIntensityPointRepository: AddIntensityPointRepository = mock()
    private val replaceLatestIntensityPointRepository: ReplaceLatestIntensityPointRepository =
        mock()

    @Before
    fun setUp() {
        classUnderTest = RecordIntensityUseCase(
            getCurrentTimeRepository,
            getAttackInProgressRepository,
            addIntensityPointRepository,
            replaceLatestIntensityPointRepository,
            CoroutineContextProvider.Default
        )
        given(
            getCurrentTimeRepository.currentTime()
        ).willReturn(CurrentTimeDomainModel(NOW, TASHKENT))
    }

    @Test
    fun `Given no points yet when Moderate then adds a point of 6 now`() {
        givenInProgress(points = emptyList())
        val expectedPoint = IntensityPointDomainModel(NOW, 6)
        given(
            addIntensityPointRepository.addIntensityPoint("a", expectedPoint)
        ).willReturn(WriteResultDomainModel.Written)

        val actual = classUnderTest.executeInBackground(IntensityLevelDomainModel.Moderate)

        assertEquals(RecordIntensityResultDomainModel.Recorded, actual)
        verify(addIntensityPointRepository).addIntensityPoint("a", expectedPoint)
    }

    @Test
    fun `Given the last point is 90 seconds old when Severe then replaces it`() {
        givenInProgress(points = listOf(IntensityPointDomainModel(NOW.minusSeconds(90), 6)))
        val expectedPoint = IntensityPointDomainModel(NOW, 9)
        given(replaceLatestIntensityPointRepository.replaceLatestIntensityPoint("a", expectedPoint))
            .willReturn(WriteResultDomainModel.Written)

        val actual = classUnderTest.executeInBackground(IntensityLevelDomainModel.Severe)

        assertEquals(RecordIntensityResultDomainModel.Recorded, actual)
        verify(addIntensityPointRepository, never()).addIntensityPoint(any(), any())
    }

    @Test
    fun `Given the last point is exactly 2 minutes old when Mild then adds a new point`() {
        givenInProgress(
            points = listOf(IntensityPointDomainModel(NOW.minus(Duration.ofMinutes(2)), 6))
        )
        val expectedPoint = IntensityPointDomainModel(NOW, 3)
        given(
            addIntensityPointRepository.addIntensityPoint("a", expectedPoint)
        ).willReturn(WriteResultDomainModel.Written)

        val actual = classUnderTest.executeInBackground(IntensityLevelDomainModel.Mild)

        assertEquals(RecordIntensityResultDomainModel.Recorded, actual)
        verify(
            replaceLatestIntensityPointRepository,
            never()
        ).replaceLatestIntensityPoint(any(), any())
    }

    @Test
    fun `Given no attack in progress when executed then NotInProgress`() {
        given(
            getAttackInProgressRepository.attackInProgress()
        ).willReturn(AttackLookupResultDomainModel.None)

        val actual = classUnderTest.executeInBackground(IntensityLevelDomainModel.Mild)

        assertEquals(RecordIntensityResultDomainModel.NotInProgress, actual)
    }

    @Test
    fun `Given storage is full when executed then StorageFull`() {
        givenInProgress(points = emptyList())
        given(
            addIntensityPointRepository.addIntensityPoint(any(), any())
        ).willReturn(WriteResultDomainModel.StorageFull)

        val actual = classUnderTest.executeInBackground(IntensityLevelDomainModel.Mild)

        assertEquals(RecordIntensityResultDomainModel.StorageFull, actual)
    }

    @Test
    fun `Given the lookup fails when executed then Failed`() {
        given(
            getAttackInProgressRepository.attackInProgress()
        ).willReturn(AttackLookupResultDomainModel.Failed)

        val actual = classUnderTest.executeInBackground(IntensityLevelDomainModel.Mild)

        assertEquals(RecordIntensityResultDomainModel.Failed, actual)
    }

    private fun givenInProgress(points: List<IntensityPointDomainModel>) {
        given(getAttackInProgressRepository.attackInProgress())
            .willReturn(
                AttackLookupResultDomainModel.InProgress(attack(id = "a", intensityPoints = points))
            )
    }
}
