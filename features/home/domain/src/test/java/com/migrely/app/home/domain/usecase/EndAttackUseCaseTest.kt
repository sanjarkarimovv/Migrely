package com.migrely.app.home.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.home.domain.model.AttackLookupResultDomainModel
import com.migrely.app.home.domain.model.CurrentTimeDomainModel
import com.migrely.app.home.domain.model.EndAttackRequestDomainModel
import com.migrely.app.home.domain.model.EndAttackResultDomainModel
import com.migrely.app.home.domain.model.TASHKENT
import com.migrely.app.home.domain.model.WriteResultDomainModel
import com.migrely.app.home.domain.model.attack
import com.migrely.app.home.domain.repository.EndAttackRepository
import com.migrely.app.home.domain.repository.GetAttackInProgressRepository
import com.migrely.app.home.domain.repository.GetCurrentTimeRepository
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

private val START: Instant = Instant.parse("2026-10-20T09:00:00Z")
private val NOW: Instant = Instant.parse("2026-10-23T12:00:00Z")

class EndAttackUseCaseTest {
    private lateinit var classUnderTest: EndAttackUseCase
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val getAttackInProgressRepository: GetAttackInProgressRepository = mock()
    private val endAttackRepository: EndAttackRepository = mock()

    @Before
    fun setUp() {
        classUnderTest = EndAttackUseCase(
            getCurrentTimeRepository,
            getAttackInProgressRepository,
            endAttackRepository,
            CoroutineContextProvider.Default
        )
        given(
            getCurrentTimeRepository.currentTime()
        ).willReturn(CurrentTimeDomainModel(NOW, TASHKENT))
        given(getAttackInProgressRepository.attackInProgress())
            .willReturn(AttackLookupResultDomainModel.InProgress(attack(id = "a", start = START)))
    }

    @Test
    fun `Given no end time when executed then ends now`() {
        given(endAttackRepository.endAttack("a", NOW)).willReturn(WriteResultDomainModel.Written)

        val actual = classUnderTest.executeInBackground(EndAttackRequestDomainModel(end = null))

        assertEquals(EndAttackResultDomainModel.Ended("a"), actual)
    }

    @Test
    fun `Given a picked time between start and now when executed then ends at it`() {
        val picked = START.plus(Duration.ofHours(30))
        given(endAttackRepository.endAttack("a", picked)).willReturn(WriteResultDomainModel.Written)

        val actual = classUnderTest.executeInBackground(EndAttackRequestDomainModel(end = picked))

        assertEquals(EndAttackResultDomainModel.Ended("a"), actual)
    }

    @Test
    fun `Given a picked time before the start when executed then EndOutOfRange`() {
        val actual = classUnderTest.executeInBackground(
            EndAttackRequestDomainModel(end = START.minusSeconds(1))
        )

        assertEquals(EndAttackResultDomainModel.EndOutOfRange, actual)
        verify(endAttackRepository, never()).endAttack(any(), any())
    }

    @Test
    fun `Given a picked time in the future when executed then EndOutOfRange`() {
        val actual = classUnderTest.executeInBackground(
            EndAttackRequestDomainModel(end = NOW.plusSeconds(60))
        )

        assertEquals(EndAttackResultDomainModel.EndOutOfRange, actual)
    }

    @Test
    fun `Given no attack in progress when executed then NotInProgress`() {
        given(
            getAttackInProgressRepository.attackInProgress()
        ).willReturn(AttackLookupResultDomainModel.None)

        val actual = classUnderTest.executeInBackground(EndAttackRequestDomainModel(end = null))

        assertEquals(EndAttackResultDomainModel.NotInProgress, actual)
    }

    @Test
    fun `Given storage is full when executed then StorageFull`() {
        given(
            endAttackRepository.endAttack(any(), any())
        ).willReturn(WriteResultDomainModel.StorageFull)

        val actual = classUnderTest.executeInBackground(EndAttackRequestDomainModel(end = null))

        assertEquals(EndAttackResultDomainModel.StorageFull, actual)
    }
}
