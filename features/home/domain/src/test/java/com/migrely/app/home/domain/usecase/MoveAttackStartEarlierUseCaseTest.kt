package com.migrely.app.home.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.home.domain.model.AttackLookupResultDomainModel
import com.migrely.app.home.domain.model.MoveStartResultDomainModel
import com.migrely.app.home.domain.model.PreviousEndResultDomainModel
import com.migrely.app.home.domain.model.WriteResultDomainModel
import com.migrely.app.home.domain.model.attack
import com.migrely.app.home.domain.repository.GetAttackInProgressRepository
import com.migrely.app.home.domain.repository.GetPreviousAttackEndRepository
import com.migrely.app.home.domain.repository.UpdateAttackStartRepository
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

private val START: Instant = Instant.parse("2026-10-23T09:00:00Z")

class MoveAttackStartEarlierUseCaseTest {
    private lateinit var classUnderTest: MoveAttackStartEarlierUseCase
    private val getAttackInProgressRepository: GetAttackInProgressRepository = mock()
    private val getPreviousAttackEndRepository: GetPreviousAttackEndRepository = mock()
    private val updateAttackStartRepository: UpdateAttackStartRepository = mock()

    @Before
    fun setUp() {
        classUnderTest = MoveAttackStartEarlierUseCase(
            getAttackInProgressRepository,
            getPreviousAttackEndRepository,
            updateAttackStartRepository,
            CoroutineContextProvider.Default
        )
        given(getAttackInProgressRepository.attackInProgress())
            .willReturn(AttackLookupResultDomainModel.InProgress(attack(id = "a", start = START)))
    }

    @Test
    fun `Given no previous attack when moving one hour earlier then moved`() {
        val expectedStart = START.minus(Duration.ofHours(1))
        given(
            getPreviousAttackEndRepository.previousAttackEnd("a", START)
        ).willReturn(PreviousEndResultDomainModel.None)
        given(
            updateAttackStartRepository.updateAttackStart("a", expectedStart)
        ).willReturn(WriteResultDomainModel.Written)

        val actual = classUnderTest.executeInBackground(Duration.ofHours(1))

        assertEquals(MoveStartResultDomainModel.Moved(expectedStart), actual)
    }

    @Test
    fun `Given previous ended 30 min before when moving 1 h earlier then rejected`() {
        val previousEnd = START.minus(Duration.ofMinutes(30))
        given(getPreviousAttackEndRepository.previousAttackEnd("a", START))
            .willReturn(PreviousEndResultDomainModel.Found(previousEnd))

        val actual = classUnderTest.executeInBackground(Duration.ofHours(1))

        assertEquals(MoveStartResultDomainModel.OverlapsPrevious(previousEnd), actual)
        verify(updateAttackStartRepository, never()).updateAttackStart(any(), any())
    }

    @Test
    fun `Given the previous attack ended exactly at the new start when moving then moved`() {
        val newStart = START.minus(Duration.ofMinutes(30))
        given(getPreviousAttackEndRepository.previousAttackEnd("a", START))
            .willReturn(PreviousEndResultDomainModel.Found(newStart))
        given(
            updateAttackStartRepository.updateAttackStart("a", newStart)
        ).willReturn(WriteResultDomainModel.Written)

        val actual = classUnderTest.executeInBackground(Duration.ofMinutes(30))

        assertEquals(MoveStartResultDomainModel.Moved(newStart), actual)
    }

    @Test
    fun `Given no attack in progress when executed then NotInProgress`() {
        given(
            getAttackInProgressRepository.attackInProgress()
        ).willReturn(AttackLookupResultDomainModel.None)

        val actual = classUnderTest.executeInBackground(Duration.ofMinutes(15))

        assertEquals(MoveStartResultDomainModel.NotInProgress, actual)
    }

    @Test
    fun `Given storage is full when executed then StorageFull`() {
        given(
            getPreviousAttackEndRepository.previousAttackEnd("a", START)
        ).willReturn(PreviousEndResultDomainModel.None)
        given(
            updateAttackStartRepository.updateAttackStart(any(), any())
        ).willReturn(WriteResultDomainModel.StorageFull)

        val actual = classUnderTest.executeInBackground(Duration.ofMinutes(15))

        assertEquals(MoveStartResultDomainModel.StorageFull, actual)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given a zero duration when executed then rejects the request`() {
        classUnderTest.executeInBackground(Duration.ZERO)
    }
}
