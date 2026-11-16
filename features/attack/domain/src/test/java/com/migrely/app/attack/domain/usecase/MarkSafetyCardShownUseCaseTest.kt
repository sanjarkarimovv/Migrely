package com.migrely.app.attack.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.attack.domain.repository.MarkSafetyCardShownRepository
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class MarkSafetyCardShownUseCaseTest {
    private val markSafetyCardShownRepository: MarkSafetyCardShownRepository = mock()
    private val classUnderTest =
        MarkSafetyCardShownUseCase(markSafetyCardShownRepository, CoroutineContextProvider.Default)

    @Test
    fun `When the card appears then its moment is kept`() {
        classUnderTest.executeInBackground(Unit)

        verify(markSafetyCardShownRepository).markSafetyCardShown()
    }
}
