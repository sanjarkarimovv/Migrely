package com.migrely.app.datasource.billing.mapper

import com.android.billingclient.api.Purchase
import com.migrely.app.datasource.billing.model.BillingPurchaseDataModel
import com.migrely.app.datasource.billing.model.BillingPurchaseStateDataModel
import com.migrely.app.datasource.billing.security.PurchaseSignatureVerifier
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@RunWith(Parameterized::class)
class PlayPurchaseMapperTest(
    private val playState: Int,
    private val signatureValid: Boolean,
    private val expectedState: BillingPurchaseStateDataModel
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}, valid {1} -> {2}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                Purchase.PurchaseState.PURCHASED,
                true,
                BillingPurchaseStateDataModel.Purchased
            ),
            arrayOf(Purchase.PurchaseState.PENDING, true, BillingPurchaseStateDataModel.Pending),
            arrayOf(
                Purchase.PurchaseState.UNSPECIFIED_STATE,
                false,
                BillingPurchaseStateDataModel.Unspecified
            )
        )
    }

    @Test
    fun `When mapping then keeps what the entitlement needs and checks the signature`() {
        val verifier = mock<PurchaseSignatureVerifier> {
            on { isValid("{json}", "signature") } doReturn signatureValid
        }
        val purchase = mock<Purchase> {
            on { products } doReturn listOf("migrely_premium")
            on { purchaseState } doReturn playState
            on { purchaseTime } doReturn 1_790_000_000_000L
            on { isAutoRenewing } doReturn true
            on { purchaseToken } doReturn "token"
            on { isAcknowledged } doReturn false
            on { originalJson } doReturn "{json}"
            on { signature } doReturn "signature"
        }

        assertEquals(
            BillingPurchaseDataModel(
                productIds = listOf("migrely_premium"),
                state = expectedState,
                purchaseTimeMillis = 1_790_000_000_000L,
                autoRenewing = true,
                purchaseToken = "token",
                acknowledged = false,
                signatureValid = signatureValid
            ),
            PlayPurchaseMapper(verifier).toData(purchase)
        )
    }
}
