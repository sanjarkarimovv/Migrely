package com.migrely.app.attack.domain.usecase

import com.migrely.app.attack.domain.model.AttackDetailsLookupResultDomainModel
import com.migrely.app.attack.domain.model.CurrentTimeDomainModel
import com.migrely.app.attack.domain.model.FIXTURE_NOW
import com.migrely.app.attack.domain.model.FIXTURE_START
import com.migrely.app.attack.domain.model.FIXTURE_ZONE
import com.migrely.app.attack.domain.model.IntensityPointDomainModel
import com.migrely.app.attack.domain.model.OverlapLookupResultDomainModel
import com.migrely.app.attack.domain.model.UndoTimesChangeRequestDomainModel
import com.migrely.app.attack.domain.model.UndoTimesChangeResultDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.model.attackDetails
import com.migrely.app.attack.domain.repository.FindOverlappingAttackRepository
import com.migrely.app.attack.domain.repository.GetAttackDetailsRepository
import com.migrely.app.attack.domain.repository.GetCurrentTimeRepository
import com.migrely.app.attack.domain.repository.RestoreAttackTimesRepository
import com.migrely.app.coroutine.CoroutineContextProvider
import java.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

private val OLD_END = FIXTURE_START.plus(Duration.ofHours(4))
private val REMOVED = listOf(IntensityPointDomainModel(FIXTURE_START.plus(Duration.ofHours(3)), 8))
private val REQUEST = UndoTimesChangeRequestDomainModel("a", FIXTURE_START, OLD_END, REMOVED)

class UndoTimesChangeUseCaseTest {
    private lateinit var classUnderTest: UndoTimesChangeUseCase
    private val getAttackDetailsRepository: GetAttackDetailsRepository = mock()
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val findOverlappingAttackRepository: FindOverlappingAttackRepository = mock()
    private val restoreAttackTimesRepository: RestoreAttackTimesRepository = mock()

    @Before
    fun setUp() {
        classUnderTest = UndoTimesChangeUseCase(
            getAttackDetailsRepository,
            getCurrentTimeRepository,
            findOverlappingAttackRepository,
            restoreAttackTimesRepository,
            CoroutineContextProvider.Default
        )
        given(
            getCurrentTimeRepository.currentTime()
        ).willReturn(CurrentTimeDomainModel(FIXTURE_NOW, FIXTURE_ZONE))
        given(getAttackDetailsRepository.attackDetails("a"))
            .willReturn(AttackDetailsLookupResultDomainModel.Found(attackDetails()))
        given(restoreAttackTimesRepository.restoreAttackTimes(any(), any(), anyOrNull(), any()))
            .willReturn(WriteResultDomainModel.Written)
    }

    @Test
    fun `Given nothing overlaps the old times when undoing then restores times and points`() {
        given(findOverlappingAttackRepository.overlappingAttack("a", FIXTURE_START, OLD_END))
            .willReturn(OverlapLookupResultDomainModel.None)

        val actual = classUnderTest.executeInBackground(REQUEST)

        assertEquals(UndoTimesChangeResultDomainModel.Restored, actual)
        verify(
            restoreAttackTimesRepository
        ).restoreAttackTimes("a", FIXTURE_START, OLD_END, REMOVED)
    }

    @Test
    fun `Given another attack now overlaps the old times when undoing then not possible`() {
        given(findOverlappingAttackRepository.overlappingAttack("a", FIXTURE_START, OLD_END))
            .willReturn(OverlapLookupResultDomainModel.Found(OLD_END.minusSeconds(60), null))

        val actual = classUnderTest.executeInBackground(REQUEST)

        assertEquals(UndoTimesChangeResultDomainModel.NotPossible, actual)
        verify(restoreAttackTimesRepository, never()).restoreAttackTimes(
            any(),
            any(),
            anyOrNull(),
            any()
        )
    }

    @Test
    fun `Given the attack is gone when undoing then not possible`() {
        given(getAttackDetailsRepository.attackDetails("a"))
            .willReturn(AttackDetailsLookupResultDomainModel.NotFound)

        assertEquals(
            UndoTimesChangeResultDomainModel.NotPossible,
            classUnderTest.executeInBackground(REQUEST)
        )
    }
}
