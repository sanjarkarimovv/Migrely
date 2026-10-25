package com.migrely.app.datasource.billing.datasource

import android.content.Context
import com.migrely.app.datasource.billing.model.BillingConnectionDataModel
import com.migrely.app.datasource.billing.model.BillingPurchaseRequestDataModel
import com.migrely.app.datasource.billing.model.BillingPurchaseResultDataModel
import com.migrely.app.datasource.billing.model.BillingPurchaseStateDataModel
import com.migrely.app.datasource.exception.BillingDataException
import com.migrely.app.datasource.exception.BillingUnavailableDataException
import java.text.NumberFormat
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyVararg
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

private const val NOW = 1_792_756_800_000L
private val YEARLY_TRIAL =
    BillingPurchaseRequestDataModel("migrely_premium", "sim-yearly-trial", null)
private val MONTHLY = BillingPurchaseRequestDataModel("migrely_premium", "sim-monthly", null)
private val LIFETIME = BillingPurchaseRequestDataModel("migrely_lifetime", "sim-lifetime", null)

class SimulatedBillingDataSourceImplTest {
    private val context: Context = mock {
        on { getString(any()) } doReturn "option"
        on { getString(any(), anyVararg()) } doReturn "product"
    }
    private val store = InMemoryPreferences()
    private var choice: Int? = 0
    private val shownTitles = mutableListOf<String>()

    private val classUnderTest = SimulatedBillingDataSourceImpl(
        context = context,
        store = store,
        sheet = { title, _ ->
            shownTitles += title
            choice
        },
        now = { NOW },
        locale = { Locale.GERMANY }
    )

    @Test
    fun `When asking for products then prices are local and yearly has the trial`() {
        val products = classUnderTest.products(
            listOf("migrely_premium"),
            listOf("migrely_lifetime")
        )

        val premium = products.single { it.productId == "migrely_premium" }
        assertEquals(
            listOf("yearly" to null, "yearly" to "free-trial-7d", "monthly" to null),
            premium.offers.map { it.basePlanId to it.offerId }
        )
        assertEquals(euros(29.99), premium.offers.first().formattedPrice)
        assertEquals("P7D", premium.offers[1].freeTrialPeriod)
        assertEquals(
            euros(59.99),
            products.single { it.productId == "migrely_lifetime" }.offers.single().formattedPrice
        )
    }

    @Test
    fun `Given the trial was used when asking for products then yearly has no trial`() {
        classUnderTest.purchase(YEARLY_TRIAL)

        val offers = classUnderTest.products(listOf("migrely_premium"), emptyList()).single().offers

        assertTrue(offers.none { it.freeTrialPeriod != null })
    }

    @Test
    fun `When completing a purchase then it is owned and can be acknowledged`() {
        val result = classUnderTest.purchase(MONTHLY) as BillingPurchaseResultDataModel.Completed
        val token = result.purchases.single().purchaseToken

        assertEquals(BillingPurchaseStateDataModel.Purchased, result.purchases.single().state)
        assertTrue(classUnderTest.acknowledge(token))
        val owned = classUnderTest.purchases().single()
        assertEquals(listOf("migrely_premium"), owned.productIds)
        assertEquals(NOW, owned.purchaseTimeMillis)
        assertTrue(owned.acknowledged && owned.autoRenewing && owned.signatureValid)
    }

    @Test
    fun `When paying later then the purchase is pending until the next sheet completes it`() {
        choice = 1
        val pending = classUnderTest.purchase(LIFETIME) as BillingPurchaseResultDataModel.Completed
        assertEquals(BillingPurchaseStateDataModel.Pending, pending.purchases.single().state)

        choice = 0
        val completed = classUnderTest.purchase(
            LIFETIME
        ) as BillingPurchaseResultDataModel.Completed

        assertEquals(BillingPurchaseStateDataModel.Purchased, completed.purchases.single().state)
        assertEquals(
            pending.purchases.single().purchaseToken,
            completed.purchases.single().purchaseToken
        )
    }

    @Test
    fun `When declining a pending payment then nothing is owned`() {
        choice = 1
        classUnderTest.purchase(LIFETIME)

        val declined = classUnderTest.purchase(LIFETIME)

        assertEquals(BillingPurchaseResultDataModel.Cancelled, declined)
        assertEquals(emptyList<Any>(), classUnderTest.purchases())
    }

    @Test
    fun `When cancelling the sheet then nothing is owned`() {
        choice = null

        assertEquals(BillingPurchaseResultDataModel.Cancelled, classUnderTest.purchase(MONTHLY))
        assertEquals(emptyList<Any>(), classUnderTest.purchases())
    }

    @Test
    fun `Given lifetime is owned when buying it again then it is already owned`() {
        classUnderTest.purchase(LIFETIME)

        assertEquals(BillingPurchaseResultDataModel.AlreadyOwned, classUnderTest.purchase(LIFETIME))
    }

    @Test
    fun `When upgrading monthly to yearly then the monthly purchase is replaced`() {
        val monthly = classUnderTest.purchase(MONTHLY) as BillingPurchaseResultDataModel.Completed
        val oldToken = monthly.purchases.single().purchaseToken

        classUnderTest.purchase(
            BillingPurchaseRequestDataModel("migrely_premium", "sim-yearly", oldToken)
        )

        assertTrue(classUnderTest.purchases().single().purchaseToken != oldToken)
    }

    @Test
    fun `When ending the subscription on its page then it is no longer owned`() {
        classUnderTest.purchase(MONTHLY)
        choice = 0

        classUnderTest.manageSubscription("migrely_premium")

        assertEquals(emptyList<Any>(), classUnderTest.purchases())
    }

    @Test
    fun `Given billing is unavailable then nothing can be read or bought`() {
        classUnderTest.apply("unavailable")

        assertEquals(BillingConnectionDataModel.Unavailable, classUnderTest.connect())
        assertThrows(BillingUnavailableDataException::class.java) { classUnderTest.purchases() }
        assertEquals(BillingPurchaseResultDataModel.Unavailable, classUnderTest.purchase(MONTHLY))
    }

    @Test
    fun `Given the store is unreachable then reads fail and resetting brings it back`() {
        classUnderTest.purchase(LIFETIME)
        classUnderTest.apply("unreachable")

        assertEquals(BillingConnectionDataModel.Failed, classUnderTest.connect())
        assertThrows(BillingDataException::class.java) {
            classUnderTest.products(emptyList(), emptyList())
        }

        classUnderTest.apply("reset")

        assertEquals(BillingConnectionDataModel.Available, classUnderTest.connect())
        assertEquals(emptyList<Any>(), classUnderTest.purchases())
    }

    private fun euros(amount: Double) =
        NumberFormat.getCurrencyInstance(Locale.GERMANY).format(amount)
}
