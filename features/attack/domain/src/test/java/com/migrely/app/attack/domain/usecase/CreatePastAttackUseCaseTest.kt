package com.migrely.app.attack.domain.usecase

import com.migrely.app.attack.domain.model.AttackDetailsDomainModel
import com.migrely.app.attack.domain.model.CreatePastAttackRequestDomainModel
import com.migrely.app.attack.domain.model.CreatePastAttackResultDomainModel
import com.migrely.app.attack.domain.model.CurrentTimeDomainModel
import com.migrely.app.attack.domain.model.FIXTURE_NOW
import com.migrely.app.attack.domain.model.FIXTURE_ZONE
import com.migrely.app.attack.domain.model.OverlapLookupResultDomainModel
import com.migrely.app.attack.domain.model.StorePastAttackResultDomainModel
import com.migrely.app.attack.domain.model.TimeFieldDomainModel
import com.migrely.app.attack.domain.model.attackDetails
import com.migrely.app.attack.domain.repository.FindOverlappingAttacksRepository
import com.migrely.app.attack.domain.repository.GetCurrentTimeRepository
import com.migrely.app.attack.domain.repository.StorePastAttackRepository
import com.migrely.app.coroutine.CoroutineContextProvider
import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

private val START: Instant = FIXTURE_NOW.minus(Duration.ofHours(4))
private val END: Instant = FIXTURE_NOW.minus(Duration.ofHours(2))

class CreatePastAttackUseCaseTest {
    private lateinit var classUnderTest: CreatePastAttackUseCase
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val findOverlappingAttacksRepository: FindOverlappingAttacksRepository = mock()
    private val storePastAttackRepository: StorePastAttackRepository = mock()

    @Before
    fun setUp() {
        classUnderTest = CreatePastAttackUseCase(
            getCurrentTimeRepository,
            findOverlappingAttacksRepository,
            storePastAttackRepository,
            CoroutineContextProvider.Default
        )
        given(getCurrentTimeRepository.currentTime())
            .willReturn(CurrentTimeDomainModel(FIXTURE_NOW, FIXTURE_ZONE))
        given(findOverlappingAttacksRepository.overlappingAttacks(anyOrNull(), any(), any()))
            .willReturn(OverlapLookupResultDomainModel.None)
        given(storePastAttackRepository.storePastAttack(any(), any(), any()))
            .willReturn(StorePastAttackResultDomainModel.Stored("new"))
    }

    private fun create(start: Instant = START, end: Instant = END) =
        classUnderTest.executeInBackground(CreatePastAttackRequestDomainModel(start, end))

    @Test
    fun `Given free times when creating then stored in the device zone`() {
        val actual = create()

        assertEquals(CreatePastAttackResultDomainModel.Created("new"), actual)
        verify(storePastAttackRepository).storePastAttack(START, FIXTURE_ZONE, END)
        verify(findOverlappingAttacksRepository).overlappingAttacks(null, START, END)
    }

    @Test
    fun `Given an end 3 minutes after now when creating then within the clock tolerance`() {
        val end = FIXTURE_NOW.plus(Duration.ofMinutes(3))

        assertEquals(CreatePastAttackResultDomainModel.Created("new"), create(end = end))
    }

    @Test
    fun `Given an end beyond the tolerance when creating then in the future`() {
        val actual = create(end = FIXTURE_NOW.plus(Duration.ofMinutes(6)))

        assertEquals(CreatePastAttackResultDomainModel.InFuture(TimeFieldDomainModel.End), actual)
        verify(storePastAttackRepository, never()).storePastAttack(any(), any(), any())
    }

    @Test
    fun `Given a start beyond the tolerance when creating then the start is in the future`() {
        val actual = create(
            start = FIXTURE_NOW.plus(Duration.ofMinutes(10)),
            end = FIXTURE_NOW.plus(Duration.ofMinutes(20))
        )

        assertEquals(
            CreatePastAttackResultDomainModel.InFuture(TimeFieldDomainModel.Start),
            actual
        )
    }

    @Test
    fun `Given an end equal to the start when creating then end before start`() {
        assertEquals(
            CreatePastAttackResultDomainModel.EndBeforeStart,
            create(start = END, end = END)
        )
    }

    @Test
    fun `Given two overlapping attacks when creating then both are named and nothing is stored`() {
        val first = attackDetails("b", START.minusSeconds(600), START.plusSeconds(600))
        val second = attackDetails("c", END.minusSeconds(600), null)
        given(findOverlappingAttacksRepository.overlappingAttacks(null, START, END))
            .willReturn(OverlapLookupResultDomainModel.Found(listOf(first, second)))

        val actual = create()

        assertEquals(
            CreatePastAttackResultDomainModel.Overlaps(listOf(first.range, second.range)),
            actual
        )
        verify(storePastAttackRepository, never()).storePastAttack(any(), any(), any())
    }

    @Test
    fun `Given an attack ended seconds into the start when creating then the start snaps`() {
        val previousEnd = START.plusSeconds(40)
        given(findOverlappingAttacksRepository.overlappingAttacks(null, START, END))
            .willReturn(found(attackDetails("b", START.minus(Duration.ofHours(1)), previousEnd)))

        val actual = create()

        assertEquals(CreatePastAttackResultDomainModel.Created("new"), actual)
        verify(storePastAttackRepository).storePastAttack(previousEnd, FIXTURE_ZONE, END)
    }

    @Test
    fun `Given the next attack starts seconds before the end when creating then the end snaps`() {
        val nextStart = END.minusSeconds(20)
        given(findOverlappingAttacksRepository.overlappingAttacks(null, START, END))
            .willReturn(found(attackDetails("b", nextStart, null)))

        create()

        verify(storePastAttackRepository).storePastAttack(START, FIXTURE_ZONE, nextStart)
    }

    @Test
    fun `Given snapping moves the start past the end when creating then end before start`() {
        val end = START.plusSeconds(30)
        given(findOverlappingAttacksRepository.overlappingAttacks(null, START, end))
            .willReturn(found(attackDetails("b", START.minusSeconds(600), START.plusSeconds(45))))

        assertEquals(CreatePastAttackResultDomainModel.EndBeforeStart, create(end = end))
        verify(storePastAttackRepository, never()).storePastAttack(any(), any(), any())
    }

    @Test
    fun `Given another attack was stored in between when creating then it is named`() {
        val other = attackDetails("b", START, END)
        given(storePastAttackRepository.storePastAttack(any(), any(), any()))
            .willReturn(StorePastAttackResultDomainModel.Overlapping)
        given(findOverlappingAttacksRepository.overlappingAttacks(null, START, END))
            .willReturn(OverlapLookupResultDomainModel.None)
            .willReturn(found(other))

        assertEquals(
            CreatePastAttackResultDomainModel.Overlaps(listOf(other.range)),
            create()
        )
    }

    @Test
    fun `Given the lookup fails when creating then failed`() {
        given(findOverlappingAttacksRepository.overlappingAttacks(anyOrNull(), any(), any()))
            .willReturn(OverlapLookupResultDomainModel.Failed)

        assertEquals(CreatePastAttackResultDomainModel.Failed, create())
    }

    @Test
    fun `Given storage is full when creating then storage full`() {
        given(storePastAttackRepository.storePastAttack(any(), any(), any()))
            .willReturn(StorePastAttackResultDomainModel.StorageFull)

        assertEquals(CreatePastAttackResultDomainModel.StorageFull, create())
    }

    @Test
    fun `Given storing fails when creating then failed`() {
        given(storePastAttackRepository.storePastAttack(any(), any(), any()))
            .willReturn(StorePastAttackResultDomainModel.Failed)

        assertEquals(CreatePastAttackResultDomainModel.Failed, create())
    }

    @Test
    fun `Given a short attack inside the first minute when creating then the start snaps`() {
        val shortEnd = START.plusSeconds(40)
        given(findOverlappingAttacksRepository.overlappingAttacks(null, START, END))
            .willReturn(found(attackDetails("b", START.plusSeconds(10), shortEnd)))

        assertEquals(CreatePastAttackResultDomainModel.Created("new"), create())
        verify(storePastAttackRepository).storePastAttack(shortEnd, FIXTURE_ZONE, END)
    }

    @Test
    fun `Given neighbours seconds into both minutes when creating then both times snap`() {
        val previousEnd = START.plusSeconds(25)
        val nextStart = END.minusSeconds(15)
        given(findOverlappingAttacksRepository.overlappingAttacks(null, START, END)).willReturn(
            found(
                attackDetails("b", START.minus(Duration.ofHours(1)), previousEnd),
                attackDetails("c", nextStart, null)
            )
        )

        assertEquals(CreatePastAttackResultDomainModel.Created("new"), create())
        verify(storePastAttackRepository).storePastAttack(previousEnd, FIXTURE_ZONE, nextStart)
    }

    private fun found(vararg attacks: AttackDetailsDomainModel) =
        OverlapLookupResultDomainModel.Found(attacks.toList())
}
