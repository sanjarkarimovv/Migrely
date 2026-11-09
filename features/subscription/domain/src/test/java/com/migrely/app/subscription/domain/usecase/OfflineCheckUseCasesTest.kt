package com.migrely.app.subscription.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.subscription.domain.model.BillingAvailabilityDomainModel
import com.migrely.app.subscription.domain.model.BuyPremiumResultDomainModel
import com.migrely.app.subscription.domain.model.EntitlementDomainModel
import com.migrely.app.subscription.domain.model.ManageSubscriptionResultDomainModel
import com.migrely.app.subscription.domain.model.NetworkStateDomainModel
import com.migrely.app.subscription.domain.model.PremiumCheckDomainModel
import com.migrely.app.subscription.domain.model.PremiumPlanDomainModel
import com.migrely.app.subscription.domain.model.PremiumStatusDomainModel
import com.migrely.app.subscription.domain.model.PurchaseOutcomeDomainModel
import com.migrely.app.subscription.domain.model.PurchaseSheetResultDomainModel
import com.migrely.app.subscription.domain.model.RestorePurchasesResultDomainModel
import com.migrely.app.subscription.domain.repository.GetCurrentTimeRepository
import com.migrely.app.subscription.domain.repository.GetEntitlementRepository
import com.migrely.app.subscription.domain.repository.GetNetworkStateRepository
import com.migrely.app.subscription.domain.repository.LaunchPurchaseRepository
import com.migrely.app.subscription.domain.repository.ManageSubscriptionRepository
import com.migrely.app.subscription.domain.repository.SaveLaunchedPurchaseRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

private val KEPT = PremiumStatusDomainModel.Subscribed(PremiumPlanDomainModel.Monthly, null)
private val CHECKED = PremiumCheckDomainModel(
    PremiumStatusDomainModel.Free,
    BillingAvailabilityDomainModel.Available
)

/**
 * The offline check of buy, restore and manage (BRD D166, EC-BIL-19, EC-BIL-20): offline, Google
 * Play is never asked and the kept status comes back; online or unknown, Play is asked as before.
 */
class OfflineCheckUseCasesTest {
    private val getNetworkStateRepository: GetNetworkStateRepository = mock()
    private val premiumChecker: PremiumChecker = mock {
        on { check(anyOrNull()) } doReturn CHECKED
        on { kept() } doReturn KEPT
    }
    private val launchPurchaseRepository: LaunchPurchaseRepository = mock {
        on { launchPurchase(any()) } doReturn PurchaseSheetResultDomainModel.Cancelled
    }
    private val saveLaunchedPurchaseRepository: SaveLaunchedPurchaseRepository = mock()
    private val manageSubscriptionRepository: ManageSubscriptionRepository = mock()
    private val coroutines = CoroutineContextProvider.Default

    private val buy = BuyPremiumUseCase(
        getNetworkStateRepository,
        mock<GetEntitlementRepository> {
            on { entitlement() } doReturn EntitlementDomainModel.NONE
        },
        saveLaunchedPurchaseRepository,
        mock<GetCurrentTimeRepository> { on { now() } doReturn NOW },
        launchPurchaseRepository,
        premiumChecker,
        EntitlementRules(),
        coroutines
    )
    private val restore =
        RestorePurchasesUseCase(getNetworkStateRepository, premiumChecker, coroutines)
    private val manage = ManageSubscriptionUseCase(
        getNetworkStateRepository,
        manageSubscriptionRepository,
        premiumChecker,
        coroutines
    )

    private fun networkIs(state: NetworkStateDomainModel) {
        given(getNetworkStateRepository.networkState()).willReturn(state)
    }

    @Test
    fun `Given the phone is offline when buying then no sheet opens and nothing is kept`() {
        networkIs(NetworkStateDomainModel.Offline)

        val result = buy.executeInBackground(YEARLY_TRIAL)

        assertEquals(BuyPremiumResultDomainModel(PurchaseOutcomeDomainModel.Offline, KEPT), result)
        verifyNoInteractions(launchPurchaseRepository, saveLaunchedPurchaseRepository)
        verify(premiumChecker, never()).check(anyOrNull())
    }

    @Test
    fun `Given the phone is online or its state is unknown when buying then the sheet opens`() {
        listOf(NetworkStateDomainModel.Online, NetworkStateDomainModel.Unknown).forEach { state ->
            networkIs(state)

            val result = buy.executeInBackground(YEARLY_TRIAL)

            assertEquals(state.name, PurchaseOutcomeDomainModel.Cancelled, result.outcome)
        }
        verify(launchPurchaseRepository, times(2)).launchPurchase(any())
    }

    @Test
    fun `Given the phone is offline when restoring then Play is not asked`() {
        networkIs(NetworkStateDomainModel.Offline)

        assertEquals(
            RestorePurchasesResultDomainModel.Offline(KEPT),
            restore.executeInBackground(Unit)
        )
        verify(premiumChecker, never()).check(anyOrNull())
    }

    @Test
    fun `Given the phone is online or its state is unknown when restoring then Play is asked`() {
        listOf(NetworkStateDomainModel.Online, NetworkStateDomainModel.Unknown).forEach { state ->
            networkIs(state)

            assertEquals(
                state.name,
                RestorePurchasesResultDomainModel.Checked(CHECKED),
                restore.executeInBackground(Unit)
            )
        }
    }

    @Test
    fun `Given the phone is offline when managing then Play's page does not open`() {
        networkIs(NetworkStateDomainModel.Offline)

        assertEquals(
            ManageSubscriptionResultDomainModel.Offline(KEPT),
            manage.executeInBackground(Unit)
        )
        verifyNoInteractions(manageSubscriptionRepository)
        verify(premiumChecker, never()).check(anyOrNull())
    }

    @Test
    fun `Given the phone is online or its state is unknown when managing then Play's page opens`() {
        listOf(NetworkStateDomainModel.Online, NetworkStateDomainModel.Unknown).forEach { state ->
            networkIs(state)

            assertEquals(
                state.name,
                ManageSubscriptionResultDomainModel.Opened(CHECKED.status),
                manage.executeInBackground(Unit)
            )
        }
        verify(manageSubscriptionRepository, times(2)).manageSubscription()
    }
}
