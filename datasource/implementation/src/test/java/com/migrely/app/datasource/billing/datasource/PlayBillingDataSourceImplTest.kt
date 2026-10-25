package com.migrely.app.datasource.billing.datasource

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.QueryPurchasesParams
import com.migrely.app.datasource.billing.mapper.PlayProductDetailsMapper
import com.migrely.app.datasource.billing.mapper.PlayPurchaseMapper
import com.migrely.app.datasource.billing.model.BillingConnectionDataModel
import com.migrely.app.datasource.billing.model.BillingPurchaseRequestDataModel
import com.migrely.app.datasource.billing.model.BillingPurchaseResultDataModel
import com.migrely.app.datasource.billing.security.PurchaseSignatureVerifier
import com.migrely.app.datasource.exception.BillingDataException
import com.migrely.app.datasource.exception.BillingUnavailableDataException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

private const val TEST_TIMEOUT_MILLIS = 5_000L
private val REQUEST = BillingPurchaseRequestDataModel("migrely_lifetime", "t-lifetime", null)

private fun result(code: Int): BillingResult =
    BillingResult.newBuilder().setResponseCode(code).setDebugMessage("").build()

/** The Play implementation over a client the test drives (review I4). */
class PlayBillingDataSourceImplTest {
    private val client: BillingClient = mock()
    private val activity: Activity = mock()
    private lateinit var listener: PurchasesUpdatedListener
    private val pauses = mutableListOf<Long>()
    private var ready = false
    private var setupAnswer: Int? = BillingResponseCode.OK
    private var connections = 0
    private var serviceExists = true

    private val classUnderTest by lazy {
        PlayBillingDataSourceImpl(
            context = mock<Context>(),
            currentActivity = { activity },
            playProductDetailsMapper = PlayProductDetailsMapper(),
            playPurchaseMapper = PlayPurchaseMapper(PurchaseSignatureVerifier("", true)),
            newClient = { _, updates ->
                listener = updates
                client
            },
            timings = PlayBillingTimings(
                connectTimeoutMillis = 2_000L,
                sheetTimeoutMillis = 200L,
                messagesTimeoutMillis = 200L,
                acknowledgeBackoffMillis = listOf(1_000L, 2_000L, 4_000L)
            ),
            mainContext = Dispatchers.Unconfined,
            pause = { pauses += it },
            billingServiceExists = { serviceExists }
        )
    }

    /** Billing's builders check strings with TextUtils, which JVM tests otherwise stub out. */
    private lateinit var textUtils: MockedStatic<TextUtils>

    @Before
    fun setUpTextUtils() {
        textUtils = mockStatic(TextUtils::class.java)
        textUtils.`when`<Boolean> { TextUtils.isEmpty(anyOrNull()) }
            .thenAnswer { (it.arguments[0] as CharSequence?).isNullOrEmpty() }
    }

    @After
    fun tearDownTextUtils() {
        textUtils.close()
    }

    init {
        whenever(client.isReady).thenAnswer { ready }
        doAnswer { invocation ->
            connections++
            setupAnswer?.let { code ->
                ready = code == BillingResponseCode.OK
                invocation.getArgument<BillingClientStateListener>(0)
                    .onBillingSetupFinished(result(code))
            }
        }.whenever(client).startConnection(any())
    }

    /** Subscriptions are read first and have none here; one-time purchases follow. */
    private fun givenPurchases(vararg purchases: Purchase) {
        var calls = 0
        doAnswer { invocation ->
            invocation.getArgument<PurchasesResponseListener>(1).onQueryPurchasesResponse(
                result(BillingResponseCode.OK),
                if (calls++ == 0) emptyList() else purchases.toList()
            )
        }.whenever(client).queryPurchasesAsync(any<QueryPurchasesParams>(), any())
    }

    private fun purchase(token: String, suspended: Boolean = false): Purchase = mock {
        on { products } doReturn listOf("migrely_lifetime")
        on { purchaseState } doReturn Purchase.PurchaseState.PURCHASED
        on { purchaseToken } doReturn token
        on { originalJson } doReturn "{}"
        on { signature } doReturn ""
        on { isSuspended } doReturn suspended
    }

    private fun givenLifetimeDetails() {
        val oneTime: ProductDetails.OneTimePurchaseOfferDetails = mock {
            on { formattedPrice } doReturn "€59.99"
            on { offerToken } doReturn "t-lifetime"
        }
        val details: ProductDetails = mock {
            on { productId } doReturn "migrely_lifetime"
            on { productType } doReturn BillingClient.ProductType.INAPP
            on { oneTimePurchaseOfferDetails } doReturn oneTime
            on { oneTimePurchaseOfferDetailsList } doReturn listOf(oneTime)
            // The flow's builder checks every product's package name (Billing 8.3.0's zza()).
            on { zza() } doReturn "com.migrely.app"
        }
        // Asked as a subscription first, which it is not, then as a one-time product.
        var calls = 0
        doAnswer { invocation ->
            invocation.getArgument<ProductDetailsResponseListener>(1).onProductDetailsResponse(
                result(BillingResponseCode.OK),
                QueryProductDetailsResult.create(
                    if (calls++ == 0) emptyList() else listOf(details),
                    emptyList()
                )
            )
        }.whenever(client).queryProductDetailsAsync(any<QueryProductDetailsParams>(), any())
    }

    private fun givenLaunchAnswers(vararg codes: Int, onLaunch: () -> Unit = {}) {
        var calls = 0
        doAnswer {
            onLaunch()
            result(codes[minOf(calls++, codes.lastIndex)])
        }.whenever(client).launchBillingFlow(any(), any<BillingFlowParams>())
    }

    @Test
    fun `Given each setup answer when connecting then it maps, and failures can be retried`() {
        setupAnswer = BillingResponseCode.BILLING_UNAVAILABLE
        assertEquals(BillingConnectionDataModel.Unavailable, classUnderTest.connect())

        setupAnswer = BillingResponseCode.SERVICE_UNAVAILABLE
        assertEquals(BillingConnectionDataModel.Failed, classUnderTest.connect())

        setupAnswer = BillingResponseCode.OK
        assertEquals(BillingConnectionDataModel.Available, classUnderTest.connect())
        assertEquals(BillingConnectionDataModel.Available, classUnderTest.connect())
        assertEquals(3, connections)
    }

    @Test
    fun `Given the phone has no billing service when connecting then it is unavailable at once`() {
        serviceExists = false

        assertEquals(BillingConnectionDataModel.Unavailable, classUnderTest.connect())
        assertEquals(0, connections)
    }

    @Test
    fun `Given Play never answers the setup when connecting then it gives up in time`() {
        setupAnswer = null

        assertEquals(BillingConnectionDataModel.Failed, classUnderTest.connect())
    }

    @Test
    fun `Given a connection is being set up when a second call connects then they share it`() {
        setupAnswer = null
        val started = CountDownLatch(1)
        lateinit var setup: BillingClientStateListener
        doAnswer { invocation ->
            connections++
            setup = invocation.getArgument(0)
            started.countDown()
        }.whenever(client).startConnection(any())
        val answers = mutableListOf<BillingConnectionDataModel>()

        val first =
            thread { classUnderTest.connect().let { synchronized(answers) { answers += it } } }
        assertTrue(started.await(TEST_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS))
        val second =
            thread { classUnderTest.connect().let { synchronized(answers) { answers += it } } }
        Thread.sleep(100L)
        ready = true
        setup.onBillingSetupFinished(result(BillingResponseCode.OK))
        first.join(TEST_TIMEOUT_MILLIS)
        second.join(TEST_TIMEOUT_MILLIS)

        assertEquals(1, connections)
        assertEquals(
            listOf(BillingConnectionDataModel.Available, BillingConnectionDataModel.Available),
            answers
        )
    }

    @Test
    fun `Given a suspended subscription when reading purchases then it is left out`() {
        givenPurchases(purchase("active"), purchase("on-hold", suspended = true))

        assertEquals(listOf("active"), classUnderTest.purchases().map { it.purchaseToken })
    }

    @Test
    fun `Given Play's answer to a query then no billing and no answer are told apart`() {
        listOf(
            BillingResponseCode.BILLING_UNAVAILABLE to BillingUnavailableDataException::class.java,
            BillingResponseCode.ITEM_UNAVAILABLE to BillingUnavailableDataException::class.java,
            BillingResponseCode.SERVICE_UNAVAILABLE to BillingDataException::class.java
        ).forEach { (code, expected) ->
            doAnswer { invocation ->
                invocation.getArgument<PurchasesResponseListener>(1)
                    .onQueryPurchasesResponse(result(code), emptyList())
            }.whenever(client).queryPurchasesAsync(any<QueryPurchasesParams>(), any())

            assertThrows(expected) { classUnderTest.purchases() }
        }
    }

    @Test
    fun `Given a sheet is open when Play's update comes then the sheet gets it`() {
        givenLifetimeDetails()
        val bought = purchase("new")
        givenLaunchAnswers(BillingResponseCode.OK) {
            listener.onPurchasesUpdated(result(BillingResponseCode.OK), listOf(bought))
        }

        val sheet = classUnderTest.purchase(REQUEST) as BillingPurchaseResultDataModel.Completed

        assertEquals("new", sheet.purchases.single().purchaseToken)
    }

    @Test
    fun `Given no sheet is open when Play's update comes then the app-wide flow hears it`() =
        runBlocking {
            val heard = async(Dispatchers.Default, start = CoroutineStart.UNDISPATCHED) {
                withTimeout(TEST_TIMEOUT_MILLIS) { classUnderTest.purchaseUpdates().first() }
            }

            listener.onPurchasesUpdated(result(BillingResponseCode.OK), listOf(purchase("shop")))

            assertEquals(Unit, heard.await())
        }

    @Test
    fun `Given each way a sheet closes then the result says it`() {
        givenLifetimeDetails()
        mapOf(
            BillingResponseCode.USER_CANCELED to BillingPurchaseResultDataModel.Cancelled,
            BillingResponseCode.ITEM_ALREADY_OWNED to BillingPurchaseResultDataModel.AlreadyOwned,
            BillingResponseCode.ITEM_UNAVAILABLE to BillingPurchaseResultDataModel.Unavailable,
            BillingResponseCode.BILLING_UNAVAILABLE to BillingPurchaseResultDataModel.Unavailable,
            BillingResponseCode.DEVELOPER_ERROR to BillingPurchaseResultDataModel.Failed
        ).forEach { (code, expected) ->
            givenLaunchAnswers(code)

            assertEquals(expected, classUnderTest.purchase(REQUEST))
        }
    }

    @Test
    fun `Given the connection dropped as the sheet opened then it is tried once more`() {
        givenLifetimeDetails()
        givenLaunchAnswers(BillingResponseCode.SERVICE_DISCONNECTED, BillingResponseCode.OK) {
            listener.onPurchasesUpdated(
                result(BillingResponseCode.USER_CANCELED),
                null
            )
        }

        assertEquals(BillingPurchaseResultDataModel.Cancelled, classUnderTest.purchase(REQUEST))
        verify(client, times(2)).launchBillingFlow(any(), any<BillingFlowParams>())
    }

    @Test
    fun `Given the network fails twice as the sheet opens then the second answer stands`() {
        givenLifetimeDetails()
        givenLaunchAnswers(BillingResponseCode.NETWORK_ERROR, BillingResponseCode.NETWORK_ERROR)

        assertEquals(BillingPurchaseResultDataModel.Failed, classUnderTest.purchase(REQUEST))
        verify(client, times(2)).launchBillingFlow(any(), any<BillingFlowParams>())
    }

    @Test
    fun `Given the sheet never answers then it times out, and a late update goes to the flow`() =
        runBlocking {
            givenLifetimeDetails()
            givenLaunchAnswers(BillingResponseCode.OK)

            assertEquals(BillingPurchaseResultDataModel.TimedOut, classUnderTest.purchase(REQUEST))

            val heard = async(Dispatchers.Default, start = CoroutineStart.UNDISPATCHED) {
                withTimeout(TEST_TIMEOUT_MILLIS) { classUnderTest.purchaseUpdates().first() }
            }
            listener.onPurchasesUpdated(result(BillingResponseCode.OK), listOf(purchase("late")))
            assertEquals(Unit, heard.await())
        }

    @Test
    fun `Given acknowledging fails twice then it is tried again after 1 and 2 seconds`() {
        var attempts = 0
        doAnswer { invocation ->
            attempts++
            val code = if (attempts < 3) BillingResponseCode.ERROR else BillingResponseCode.OK
            invocation.getArgument<AcknowledgePurchaseResponseListener>(1)
                .onAcknowledgePurchaseResponse(result(code))
        }.whenever(client).acknowledgePurchase(any<AcknowledgePurchaseParams>(), any())

        assertTrue(classUnderTest.acknowledge("token"))
        assertEquals(3, attempts)
        assertEquals(listOf(1_000L, 2_000L), pauses)
    }

    @Test
    fun `Given acknowledging keeps failing then it gives up after 3 retries`() {
        doAnswer { invocation ->
            invocation.getArgument<AcknowledgePurchaseResponseListener>(1)
                .onAcknowledgePurchaseResponse(result(BillingResponseCode.SERVICE_UNAVAILABLE))
        }.whenever(client).acknowledgePurchase(any<AcknowledgePurchaseParams>(), any())

        assertFalse(classUnderTest.acknowledge("token"))
        assertEquals(listOf(1_000L, 2_000L, 4_000L), pauses)
    }
}
