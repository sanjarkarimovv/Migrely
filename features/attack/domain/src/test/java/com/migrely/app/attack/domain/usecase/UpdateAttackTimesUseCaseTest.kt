package com.migrely.app.attack.domain.usecase

import com.migrely.app.attack.domain.model.AttackDetailsDomainModel
import com.migrely.app.attack.domain.model.AttackDetailsLookupResultDomainModel
import com.migrely.app.attack.domain.model.AttackRangeDomainModel
import com.migrely.app.attack.domain.model.CurrentTimeDomainModel
import com.migrely.app.attack.domain.model.FIXTURE_NOW
import com.migrely.app.attack.domain.model.FIXTURE_START
import com.migrely.app.attack.domain.model.FIXTURE_ZONE
import com.migrely.app.attack.domain.model.IntensityPointDomainModel
import com.migrely.app.attack.domain.model.OverlapLookupResultDomainModel
import com.migrely.app.attack.domain.model.TimeFieldDomainModel
import com.migrely.app.attack.domain.model.UpdateAttackTimesRequestDomainModel
import com.migrely.app.attack.domain.model.UpdateAttackTimesResultDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.model.attackDetails
import com.migrely.app.attack.domain.repository.FindOverlappingAttacksRepository
import com.migrely.app.attack.domain.repository.GetAttackDetailsRepository
import com.migrely.app.attack.domain.repository.GetCurrentTimeRepository
import com.migrely.app.attack.domain.repository.UpdateAttackTimesRepository
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

private val END: Instant = FIXTURE_START.plus(Duration.ofHours(3))

class UpdateAttackTimesUseCaseTest {
    private lateinit var classUnderTest: UpdateAttackTimesUseCase
    private val getAttackDetailsRepository: GetAttackDetailsRepository = mock()
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val findOverlappingAttacksRepository: FindOverlappingAttacksRepository = mock()
    private val updateAttackTimesRepository: UpdateAttackTimesRepository = mock()

    @Before
    fun setUp() {
        classUnderTest = UpdateAttackTimesUseCase(
            getAttackDetailsRepository,
            getCurrentTimeRepository,
            findOverlappingAttacksRepository,
            updateAttackTimesRepository,
            CoroutineContextProvider.Default
        )
        given(
            getCurrentTimeRepository.currentTime()
        ).willReturn(CurrentTimeDomainModel(FIXTURE_NOW, FIXTURE_ZONE))
        given(findOverlappingAttacksRepository.overlappingAttacks(anyOrNull(), any(), any()))
            .willReturn(OverlapLookupResultDomainModel.None)
        given(updateAttackTimesRepository.updateAttackTimes(any(), any(), anyOrNull(), any()))
            .willReturn(WriteResultDomainModel.Written)
    }

    private fun givenAttack(
        end: Instant? = END,
        points: List<IntensityPointDomainModel> = emptyList()
    ) {
        given(getAttackDetailsRepository.attackDetails("a")).willReturn(
            AttackDetailsLookupResultDomainModel.Found(
                attackDetails(end = end, intensityPoints = points)
            )
        )
    }

    private fun update(start: Instant, end: Instant?) =
        classUnderTest.executeInBackground(UpdateAttackTimesRequestDomainModel("a", start, end))

    @Test
    fun `Given a valid earlier start when updating then stored with the previous times`() {
        givenAttack()
        val newStart = FIXTURE_START.minus(Duration.ofMinutes(25))

        val actual = update(newStart, END)

        assertEquals(
            UpdateAttackTimesResultDomainModel.Updated(FIXTURE_START, END, emptyList()),
            actual
        )
        verify(updateAttackTimesRepository).updateAttackTimes("a", newStart, END, emptyList())
    }

    @Test
    fun `Given an end 20 minutes in the future when updating then rejected`() {
        givenAttack()

        val actual = update(FIXTURE_START, FIXTURE_NOW.plus(Duration.ofMinutes(20)))

        assertEquals(UpdateAttackTimesResultDomainModel.InFuture(TimeFieldDomainModel.End), actual)
        verify(updateAttackTimesRepository, never()).updateAttackTimes(
            any(),
            any(),
            anyOrNull(),
            any()
        )
    }

    @Test
    fun `Given an end 3 minutes after now when updating then within the clock tolerance`() {
        givenAttack()
        val end = FIXTURE_NOW.plus(Duration.ofMinutes(3))

        val actual = update(FIXTURE_START, end)

        assertEquals(
            UpdateAttackTimesResultDomainModel.Updated(FIXTURE_START, END, emptyList()),
            actual
        )
    }

    @Test
    fun `Given a start at now when updating then stored, and a millisecond later rejected`() {
        givenAttack(end = null)

        val atNow = update(FIXTURE_NOW, null)
        val after = update(FIXTURE_NOW.plusMillis(1), null)

        // Updated carries the times before the change.
        assertEquals(
            UpdateAttackTimesResultDomainModel.Updated(FIXTURE_START, null, emptyList()),
            atNow
        )
        assertEquals(
            UpdateAttackTimesResultDomainModel.InFuture(TimeFieldDomainModel.Start),
            after
        )
    }

    @Test
    fun `Given an ended attack moved to start 2 minutes from now when updating then rejected`() {
        // QA #89: such an attack would lie wholly after a start tapped now.
        givenAttack()

        val actual =
            update(FIXTURE_NOW.plus(Duration.ofMinutes(2)), FIXTURE_NOW.plus(Duration.ofMinutes(4)))

        assertEquals(
            UpdateAttackTimesResultDomainModel.InFuture(TimeFieldDomainModel.Start),
            actual
        )
    }

    @Test
    fun `Given a start 6 minutes after now when updating then rejected`() {
        givenAttack(end = null)

        val actual = update(FIXTURE_NOW.plus(Duration.ofMinutes(6)), null)

        assertEquals(
            UpdateAttackTimesResultDomainModel.InFuture(TimeFieldDomainModel.Start),
            actual
        )
    }

    @Test
    fun `Given an end before the start when updating then rejected`() {
        givenAttack()

        val actual = update(FIXTURE_START, FIXTURE_START.minus(Duration.ofMinutes(1)))

        assertEquals(UpdateAttackTimesResultDomainModel.EndBeforeStart, actual)
    }

    @Test
    fun `Given an end equal to the start when updating then rejected`() {
        givenAttack()

        assertEquals(
            UpdateAttackTimesResultDomainModel.EndBeforeStart,
            update(FIXTURE_START, FIXTURE_START)
        )
    }

    @Test
    fun `Given an attack in progress when an end is sent then the end stays empty`() {
        givenAttack(end = null)
        val newStart = FIXTURE_START.minus(Duration.ofMinutes(10))

        update(newStart, END)

        verify(updateAttackTimesRepository).updateAttackTimes("a", newStart, null, emptyList())
        verify(findOverlappingAttacksRepository).overlappingAttacks("a", newStart, FIXTURE_NOW)
    }

    @Test
    fun `Given another attack overlaps when updating then rejected with its times`() {
        givenAttack()
        val otherStart = FIXTURE_START.minus(Duration.ofHours(2))
        val otherEnd = FIXTURE_START.minus(Duration.ofMinutes(30))
        val newStart = FIXTURE_START.minus(Duration.ofHours(1))
        given(findOverlappingAttacksRepository.overlappingAttacks("a", newStart, END))
            .willReturn(found(attackDetails("b", otherStart, otherEnd)))

        val actual = update(newStart, END)

        assertEquals(
            UpdateAttackTimesResultDomainModel.Overlaps(
                listOf(AttackRangeDomainModel(otherStart, otherEnd))
            ),
            actual
        )
        verify(updateAttackTimesRepository, never()).updateAttackTimes(
            any(),
            any(),
            anyOrNull(),
            any()
        )
    }

    @Test
    fun `Given a shorter end when points fall outside then they are removed and returned`() {
        val inside = IntensityPointDomainModel(FIXTURE_START.plus(Duration.ofMinutes(10)), 6)
        val outside = IntensityPointDomainModel(FIXTURE_START.plus(Duration.ofMinutes(150)), 8)
        givenAttack(points = listOf(inside, outside))
        val newEnd = FIXTURE_START.plus(Duration.ofHours(2))

        val actual = update(FIXTURE_START, newEnd)

        assertEquals(
            UpdateAttackTimesResultDomainModel.Updated(FIXTURE_START, END, listOf(outside)),
            actual
        )
        verify(
            updateAttackTimesRepository
        ).updateAttackTimes("a", FIXTURE_START, newEnd, listOf(outside))
    }

    @Test
    fun `Given a point exactly at the new end when updating then it stays`() {
        val atEnd = IntensityPointDomainModel(FIXTURE_START.plus(Duration.ofHours(2)), 5)
        givenAttack(points = listOf(atEnd))

        val actual = update(FIXTURE_START, atEnd.at)

        assertEquals(
            UpdateAttackTimesResultDomainModel.Updated(FIXTURE_START, END, emptyList()),
            actual
        )
    }

    @Test
    fun `Given storage is full when updating then storage full`() {
        givenAttack()
        given(updateAttackTimesRepository.updateAttackTimes(any(), any(), anyOrNull(), any()))
            .willReturn(WriteResultDomainModel.StorageFull)

        assertEquals(UpdateAttackTimesResultDomainModel.StorageFull, update(FIXTURE_START, END))
    }

    @Test
    fun `Given the attack is gone when updating then not found`() {
        given(getAttackDetailsRepository.attackDetails("a"))
            .willReturn(AttackDetailsLookupResultDomainModel.NotFound)

        assertEquals(UpdateAttackTimesResultDomainModel.NotFound, update(FIXTURE_START, END))
    }

    @Test
    fun `Given the previous end is seconds into the picked minute then the start snaps to it`() {
        givenAttack()
        val pickedStart = FIXTURE_START.minus(Duration.ofMinutes(30))
        val otherEnd = pickedStart.plusSeconds(42)
        given(findOverlappingAttacksRepository.overlappingAttacks("a", pickedStart, END))
            .willReturn(found(attackDetails("b", otherEnd.minus(Duration.ofHours(1)), otherEnd)))

        val actual = update(pickedStart, END)

        assertEquals(
            UpdateAttackTimesResultDomainModel.Updated(FIXTURE_START, END, emptyList()),
            actual
        )
        verify(updateAttackTimesRepository).updateAttackTimes("a", otherEnd, END, emptyList())
    }

    @Test
    fun `Given the next start is seconds before the picked end then the end snaps to it`() {
        givenAttack()
        val nextStart = END.plus(Duration.ofMinutes(10)).minusSeconds(20)
        val pickedEnd = END.plus(Duration.ofMinutes(10))
        given(findOverlappingAttacksRepository.overlappingAttacks("a", FIXTURE_START, pickedEnd))
            .willReturn(found(attackDetails("b", nextStart, nextStart.plus(Duration.ofHours(1)))))
        given(getCurrentTimeRepository.currentTime())
            .willReturn(CurrentTimeDomainModel(pickedEnd.plus(Duration.ofHours(1)), FIXTURE_ZONE))

        update(FIXTURE_START, pickedEnd)

        verify(updateAttackTimesRepository).updateAttackTimes(
            "a",
            FIXTURE_START,
            nextStart,
            emptyList()
        )
    }

    @Test
    fun `Given two attacks overlap when updating then both are named oldest first`() {
        givenAttack()
        val newStart = FIXTURE_START.minus(Duration.ofHours(5))
        val first = attackDetails("b", newStart.plusSeconds(600), newStart.plusSeconds(3_600))
        val second = attackDetails("c", FIXTURE_START.minus(Duration.ofHours(2)), null)
        given(findOverlappingAttacksRepository.overlappingAttacks("a", newStart, END))
            .willReturn(found(first, second))

        val actual = update(newStart, END)

        assertEquals(
            UpdateAttackTimesResultDomainModel.Overlaps(listOf(first.range, second.range)),
            actual
        )
    }

    @Test
    fun `Given one neighbour snaps and one overlaps when updating then only that one is named`() {
        givenAttack()
        val pickedStart = FIXTURE_START.minus(Duration.ofHours(2))
        val previousEnd = pickedStart.plusSeconds(30)
        val previous = attackDetails("b", pickedStart.minus(Duration.ofHours(1)), previousEnd)
        val overlapping = attackDetails(
            "c",
            FIXTURE_START.minus(Duration.ofHours(1)),
            FIXTURE_START.minus(Duration.ofMinutes(20))
        )
        given(findOverlappingAttacksRepository.overlappingAttacks("a", pickedStart, END))
            .willReturn(found(previous, overlapping))
        given(findOverlappingAttacksRepository.overlappingAttacks("a", previousEnd, END))
            .willReturn(found(overlapping))

        val actual = update(pickedStart, END)

        assertEquals(
            UpdateAttackTimesResultDomainModel.Overlaps(listOf(overlapping.range)),
            actual
        )
    }

    @Test
    fun `Given the overlap lookup fails when updating then failed`() {
        givenAttack()
        given(findOverlappingAttacksRepository.overlappingAttacks(anyOrNull(), any(), any()))
            .willReturn(OverlapLookupResultDomainModel.Failed)

        assertEquals(UpdateAttackTimesResultDomainModel.Failed, update(FIXTURE_START, END))
    }

    private fun found(vararg attacks: AttackDetailsDomainModel) =
        OverlapLookupResultDomainModel.Found(attacks.toList())
}
