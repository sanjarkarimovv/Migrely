package com.migrely.app.history.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.history.domain.repository.HasPremiumRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class HasPremiumUseCaseTest {
    @Test
    fun `When asking then the entitlement decides`() {
        val repository: HasPremiumRepository = mock { on { hasPremium() } doReturn true }

        assertEquals(
            true,
            HasPremiumUseCase(
                repository,
                CoroutineContextProvider.Default
            ).executeInBackground(Unit)
        )
    }
}
