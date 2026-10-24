package com.migrely.app.history.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.history.domain.model.UndoResultDomainModel
import com.migrely.app.history.domain.repository.RestoreDeletedAttackRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

class RestoreDeletedAttackUseCaseTest {
    private val restoreDeletedAttackRepository: RestoreDeletedAttackRepository = mock()
    private val classUnderTest = RestoreDeletedAttackUseCase(
        restoreDeletedAttackRepository,
        CoroutineContextProvider.Default
    )

    @Test
    fun `When executed then returns the repository's outcome`() {
        given(restoreDeletedAttackRepository.restoreDeletedAttack("gone"))
            .willReturn(UndoResultDomainModel.NotPossible)

        assertEquals(UndoResultDomainModel.NotPossible, classUnderTest.executeInBackground("gone"))
    }
}
