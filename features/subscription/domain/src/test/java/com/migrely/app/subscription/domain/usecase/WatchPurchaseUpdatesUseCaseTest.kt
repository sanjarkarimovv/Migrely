package com.migrely.app.subscription.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.subscription.domain.model.BillingAvailabilityDomainModel
import com.migrely.app.subscription.domain.model.PremiumCheckDomainModel
import com.migrely.app.subscription.domain.model.PremiumStatusDomainModel
import com.migrely.app.subscription.domain.repository.GetPurchaseUpdatesRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class WatchPurchaseUpdatesUseCaseTest {
    @Test
    fun `Given Play reports a change with no sheet open then it is checked at once`() = runTest {
        val updates: GetPurchaseUpdatesRepository = mock {
            on { purchaseUpdates() } doReturn flowOf(Unit, Unit)
        }
        val checker: PremiumChecker = mock {
            on { check(null) } doReturn PremiumCheckDomainModel(
                PremiumStatusDomainModel.Lifetime(subscriptionRenewsToo = false),
                BillingAvailabilityDomainModel.Available
            )
        }

        val statuses = WatchPurchaseUpdatesUseCase(
            updates,
            checker,
            CoroutineContextProvider.Default
        )
            .executeInBackground(Unit)
            .toList()

        assertEquals(List(2) { PremiumStatusDomainModel.Lifetime(false) }, statuses)
        verify(checker, times(2)).check(null)
    }
}
