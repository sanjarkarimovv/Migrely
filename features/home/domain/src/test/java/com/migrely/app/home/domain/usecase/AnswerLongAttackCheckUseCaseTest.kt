package com.migrely.app.home.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.home.domain.model.AnswerLongAttackResultDomainModel
import com.migrely.app.home.domain.model.AttackLookupResultDomainModel
import com.migrely.app.home.domain.model.CurrentTimeDomainModel
import com.migrely.app.home.domain.model.LongAttackAnswerDomainModel
import com.migrely.app.home.domain.model.LongAttackAnswerRequestDomainModel
import com.migrely.app.home.domain.model.LongAttackCheckDomainModel
import com.migrely.app.home.domain.model.TASHKENT
import com.migrely.app.home.domain.model.WriteResultDomainModel
import com.migrely.app.home.domain.model.attack
import com.migrely.app.home.domain.repository.GetAttackInProgressRepository
import com.migrely.app.home.domain.repository.GetCurrentTimeRepository
import com.migrely.app.home.domain.repository.UpdateLongAttackCheckRepository
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

private val NOW: Instant = Instant.parse("2026-10-23T12:00:00Z")

class AnswerLongAttackCheckUseCaseTest {
    private lateinit var classUnderTest: AnswerLongAttackCheckUseCase
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val getAttackInProgressRepository: GetAttackInProgressRepository = mock()
    private val updateLongAttackCheckRepository: UpdateLongAttackCheckRepository = mock()

    @Before
    fun setUp() {
        classUnderTest = AnswerLongAttackCheckUseCase(
            getCurrentTimeRepository,
            getAttackInProgressRepository,
            updateLongAttackCheckRepository,
            CoroutineContextProvider.Default
        )
        given(
            getCurrentTimeRepository.currentTime()
        ).willReturn(CurrentTimeDomainModel(NOW, TASHKENT))
        given(getAttackInProgressRepository.attackInProgress())
            .willReturn(AttackLookupResultDomainModel.InProgress(attack(id = "a")))
    }

    @Test
    fun `When still going then confirms now`() {
        val expectedCheck = LongAttackCheckDomainModel(confirmedAt = NOW, disabled = false)
        given(updateLongAttackCheckRepository.updateLongAttackCheck("a", expectedCheck))
            .willReturn(WriteResultDomainModel.Written)

        val actual = classUnderTest.executeInBackground(
            LongAttackAnswerRequestDomainModel("a", LongAttackAnswerDomainModel.StillGoing)
        )

        assertEquals(AnswerLongAttackResultDomainModel.Saved, actual)
    }

    @Test
    fun `When don't ask again then disables the check`() {
        val expectedCheck = LongAttackCheckDomainModel(confirmedAt = null, disabled = true)
        given(updateLongAttackCheckRepository.updateLongAttackCheck("a", expectedCheck))
            .willReturn(WriteResultDomainModel.Written)

        val actual = classUnderTest.executeInBackground(
            LongAttackAnswerRequestDomainModel("a", LongAttackAnswerDomainModel.DontAskAgain)
        )

        assertEquals(AnswerLongAttackResultDomainModel.Saved, actual)
    }

    @Test
    fun `Given another attack is in progress when answering then NotInProgress`() {
        val actual = classUnderTest.executeInBackground(
            LongAttackAnswerRequestDomainModel("other", LongAttackAnswerDomainModel.StillGoing)
        )

        assertEquals(AnswerLongAttackResultDomainModel.NotInProgress, actual)
        verify(updateLongAttackCheckRepository, never()).updateLongAttackCheck(any(), any())
    }

    @Test
    fun `Given storage is full when answering then StorageFull`() {
        given(updateLongAttackCheckRepository.updateLongAttackCheck(any(), any()))
            .willReturn(WriteResultDomainModel.StorageFull)

        val actual = classUnderTest.executeInBackground(
            LongAttackAnswerRequestDomainModel("a", LongAttackAnswerDomainModel.DontAskAgain)
        )

        assertEquals(AnswerLongAttackResultDomainModel.StorageFull, actual)
    }
}
