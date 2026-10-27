package com.migrely.app.home.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.home.domain.model.AttackLookupResultDomainModel
import com.migrely.app.home.domain.model.UndoResultDomainModel
import com.migrely.app.home.domain.model.WriteResultDomainModel
import com.migrely.app.home.domain.model.attack
import com.migrely.app.home.domain.repository.DeleteAttackRepository
import com.migrely.app.home.domain.repository.GetAttackInProgressRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class UndoAttackStartUseCaseTest {
    private lateinit var classUnderTest: UndoAttackStartUseCase
    private val getAttackInProgressRepository: GetAttackInProgressRepository = mock()
    private val deleteAttackRepository: DeleteAttackRepository = mock()

    @Before
    fun setUp() {
        classUnderTest = UndoAttackStartUseCase(
            getAttackInProgressRepository,
            deleteAttackRepository,
            CoroutineContextProvider.Default
        )
    }

    @Test
    fun `Given the attack is still in progress when executed then deletes it`() {
        given(getAttackInProgressRepository.attackInProgress())
            .willReturn(AttackLookupResultDomainModel.InProgress(attack(id = "a")))
        given(deleteAttackRepository.deleteAttack("a")).willReturn(WriteResultDomainModel.Written)

        val actual = classUnderTest.executeInBackground("a")

        assertEquals(UndoResultDomainModel.Undone, actual)
    }

    @Test
    fun `Given a different attack is in progress when executed then does not delete`() {
        given(getAttackInProgressRepository.attackInProgress())
            .willReturn(AttackLookupResultDomainModel.InProgress(attack(id = "b")))

        val actual = classUnderTest.executeInBackground("a")

        assertEquals(UndoResultDomainModel.NotPossible, actual)
        verify(deleteAttackRepository, never()).deleteAttack(any())
    }

    @Test
    fun `Given no attack is in progress when executed then does not delete`() {
        given(
            getAttackInProgressRepository.attackInProgress()
        ).willReturn(AttackLookupResultDomainModel.None)

        val actual = classUnderTest.executeInBackground("a")

        assertEquals(UndoResultDomainModel.NotPossible, actual)
        verify(deleteAttackRepository, never()).deleteAttack(any())
    }

    @Test
    fun `Given the lookup fails when executed then Failed`() {
        given(
            getAttackInProgressRepository.attackInProgress()
        ).willReturn(AttackLookupResultDomainModel.Failed)

        val actual = classUnderTest.executeInBackground("a")

        assertEquals(UndoResultDomainModel.Failed, actual)
    }

    @Test
    fun `Given the delete fails when executed then Failed`() {
        given(getAttackInProgressRepository.attackInProgress())
            .willReturn(AttackLookupResultDomainModel.InProgress(attack(id = "a")))
        given(deleteAttackRepository.deleteAttack("a")).willReturn(WriteResultDomainModel.Failed)

        val actual = classUnderTest.executeInBackground("a")

        assertEquals(UndoResultDomainModel.Failed, actual)
    }
}
