package com.migrely.app.attack.domain.usecase

import com.migrely.app.attack.domain.model.AddIntensityPointRequestDomainModel
import com.migrely.app.attack.domain.model.AddIntensityPointResultDomainModel
import com.migrely.app.attack.domain.model.AttackDetailsLookupResultDomainModel
import com.migrely.app.attack.domain.model.CurrentTimeDomainModel
import com.migrely.app.attack.domain.model.FIXTURE_NOW
import com.migrely.app.attack.domain.model.FIXTURE_START
import com.migrely.app.attack.domain.model.FIXTURE_ZONE
import com.migrely.app.attack.domain.model.IntensityPointDomainModel
import com.migrely.app.attack.domain.model.MaximumRecordedResultDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.model.attackDetails
import com.migrely.app.attack.domain.repository.AddIntensityPointRepository
import com.migrely.app.attack.domain.repository.GetAttackDetailsRepository
import com.migrely.app.attack.domain.repository.GetCurrentTimeRepository
import com.migrely.app.attack.domain.repository.GetMaximumRecordedRepository
import com.migrely.app.coroutine.CoroutineContextProvider
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

private val END: Instant = FIXTURE_START.plus(Duration.ofHours(3))
private val INSIDE: Instant = FIXTURE_START.plus(Duration.ofHours(1))

class AddIntensityPointUseCaseTest {
    private lateinit var classUnderTest: AddIntensityPointUseCase
    private val getAttackDetailsRepository: GetAttackDetailsRepository = mock()
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val getMaximumRecordedRepository: GetMaximumRecordedRepository = mock()
    private val addIntensityPointRepository: AddIntensityPointRepository = mock()

    @Before
    fun setUp() {
        classUnderTest = AddIntensityPointUseCase(
            getAttackDetailsRepository,
            getCurrentTimeRepository,
            getMaximumRecordedRepository,
            addIntensityPointRepository,
            CoroutineContextProvider.Default
        )
        given(
            getCurrentTimeRepository.currentTime()
        ).willReturn(CurrentTimeDomainModel(FIXTURE_NOW, FIXTURE_ZONE))
        given(
            addIntensityPointRepository.addIntensityPoint(any(), any())
        ).willReturn(WriteResultDomainModel.Written)
        given(
            getMaximumRecordedRepository.maximumRecorded()
        ).willReturn(MaximumRecordedResultDomainModel.Recorded)
        givenAttack(end = END)
    }

    private fun givenAttack(end: Instant?) {
        given(getAttackDetailsRepository.attackDetails("a"))
            .willReturn(AttackDetailsLookupResultDomainModel.Found(attackDetails(end = end)))
    }

    private fun add(at: Instant, value: Int) = classUnderTest.executeInBackground(
        AddIntensityPointRequestDomainModel("a", IntensityPointDomainModel(at, value))
    )

    @Test
    fun `Given a time inside the attack when adding 8 then added`() {
        val actual = add(INSIDE, 8)

        assertEquals(AddIntensityPointResultDomainModel.Added(firstMaximum = false), actual)
        verify(
            addIntensityPointRepository
        ).addIntensityPoint("a", IntensityPointDomainModel(INSIDE, 8))
    }

    @Test
    fun `Given a time after the end when adding then outside the attack`() {
        assertEquals(AddIntensityPointResultDomainModel.OutsideAttack, add(END.plusSeconds(60), 5))
        verify(addIntensityPointRepository, never()).addIntensityPoint(any(), any())
    }

    @Test
    fun `Given a time before the start when adding then outside the attack`() {
        assertEquals(
            AddIntensityPointResultDomainModel.OutsideAttack,
            add(FIXTURE_START.minusSeconds(1), 5)
        )
    }

    @Test
    fun `Given an attack in progress and a time beyond the tolerance when adding then in future`() {
        givenAttack(end = null)

        assertEquals(
            AddIntensityPointResultDomainModel.InFuture,
            add(FIXTURE_NOW.plus(Duration.ofMinutes(6)), 5)
        )
    }

    @Test
    fun `Given no 10 was ever recorded when adding 10 then first maximum`() {
        given(
            getMaximumRecordedRepository.maximumRecorded()
        ).willReturn(MaximumRecordedResultDomainModel.NotRecorded)

        assertEquals(AddIntensityPointResultDomainModel.Added(firstMaximum = true), add(INSIDE, 10))
    }

    @Test
    fun `Given a 10 was recorded before when adding 10 then not first`() {
        assertEquals(
            AddIntensityPointResultDomainModel.Added(firstMaximum = false),
            add(INSIDE, 10)
        )
    }

    @Test
    fun `Given the check fails when adding 10 then the card is not shown`() {
        given(
            getMaximumRecordedRepository.maximumRecorded()
        ).willReturn(MaximumRecordedResultDomainModel.Failed)

        assertEquals(
            AddIntensityPointResultDomainModel.Added(firstMaximum = false),
            add(INSIDE, 10)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given a value of 11 when adding then a programming error`() {
        add(INSIDE, 11)
    }
}
