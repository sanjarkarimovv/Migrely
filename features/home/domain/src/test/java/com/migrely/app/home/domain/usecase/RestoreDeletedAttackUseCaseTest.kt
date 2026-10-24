package com.migrely.app.home.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.home.domain.model.UndoResultDomainModel
import com.migrely.app.home.domain.repository.RestoreDeletedAttackRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

class RestoreDeletedAttackUseCaseTest {
    private val repository: RestoreDeletedAttackRepository = mock()
    private val classUnderTest =
        RestoreDeletedAttackUseCase(repository, CoroutineContextProvider.Default)

    @Test
    fun `Given the attack can come back when restoring then undone`() {
        given(repository.restoreDeletedAttack("a")).willReturn(UndoResultDomainModel.Undone)

        assertEquals(UndoResultDomainModel.Undone, classUnderTest.executeInBackground("a"))
    }

    @Test
    fun `Given another attack started since when restoring then not possible`() {
        given(repository.restoreDeletedAttack("a")).willReturn(UndoResultDomainModel.NotPossible)

        assertEquals(UndoResultDomainModel.NotPossible, classUnderTest.executeInBackground("a"))
    }
}
