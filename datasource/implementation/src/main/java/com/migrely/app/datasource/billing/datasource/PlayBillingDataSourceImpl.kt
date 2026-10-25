package com.migrely.app.datasource.billing.datasource

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.InAppMessageParams
import com.android.billingclient.api.InAppMessageResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.migrely.app.datasource.billing.mapper.PlayProductDetailsMapper
import com.migrely.app.datasource.billing.mapper.PlayPurchaseMapper
import com.migrely.app.datasource.billing.model.BillingConnectionDataModel
import com.migrely.app.datasource.billing.model.BillingProductDataModel
import com.migrely.app.datasource.billing.model.BillingPurchaseDataModel
import com.migrely.app.datasource.billing.model.BillingPurchaseRequestDataModel
import com.migrely.app.datasource.billing.model.BillingPurchaseResultDataModel
import com.migrely.app.datasource.exception.BillingDataException
import com.migrely.app.datasource.exception.BillingUnavailableDataException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

private const val SUBSCRIPTIONS_PAGE =
    "https://play.google.com/store/account/subscriptions?sku=%s&package=%s"
private const val UPDATES_BUFFER = 8

/** Launch answers worth one more try once the connection is back (review M10). */
private val RETRIED_LAUNCH = setOf(
    BillingResponseCode.SERVICE_DISCONNECTED,
    BillingResponseCode.NETWORK_ERROR
)

/** How long [PlayBillingDataSourceImpl] waits for Google Play; tests shorten the waits. */
class PlayBillingTimings(
    val connectTimeoutMillis: Long = 5_000L,
    /** An open sheet gives up after this, and the next check reads what happened (review M11). */
    val sheetTimeoutMillis: Long = 10 * 60_000L,
    val messagesTimeoutMillis: Long = 10 * 60_000L,
    /** The waits before each retry of an acknowledgement that failed (review I2). */
    val acknowledgeBackoffMillis: List<Long> = listOf(1_000L, 2_000L, 4_000L)
)

/**
 * Google Play Billing Library 8 (BRD D105, D107). One client for the app: it accepts pending
 * purchases, reconnects by itself when the Play Store drops the connection, and keeps the product
 * details it read, which a purchase needs.
 * - Google Play answers on the main thread, so each blocking call waits for its answer on the
 *   caller's thread. Callers that arrive while a connection is being set up share it.
 * - [currentActivity] is the activity on screen, which the purchase sheet and Play's messages open
 *   over.
 * - An update Google Play sends while no sheet waits, such as a pending payment completed later,
 *   goes to [purchaseUpdates].
 * - [newClient] builds the client; tests pass their own.
 */
@Suppress("LongParameterList", "TooManyFunctions")
class PlayBillingDataSourceImpl(
    private val context: Context,
    private val currentActivity: () -> Activity?,
    private val playProductDetailsMapper: PlayProductDetailsMapper,
    private val playPurchaseMapper: PlayPurchaseMapper,
    newClient: (Context, PurchasesUpdatedListener) -> BillingClient = ::playBillingClient,
    private val timings: PlayBillingTimings = PlayBillingTimings(),
    private val mainContext: CoroutineContext = Dispatchers.Main,
    private val pause: suspend (Long) -> Unit = { delay(it) }
) : BillingDataSource {
    /** The purchase sheet that is open, which the next update from Google Play closes. */
    private val openSheet = AtomicReference<CompletableDeferred<BillingPurchaseResultDataModel>?>()

    private val updates = MutableSharedFlow<Unit>(extraBufferCapacity = UPDATES_BUFFER)

    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        val sheet = openSheet.getAndSet(null)
        when {
            sheet != null -> sheet.complete(
                if (result.responseCode == BillingResponseCode.OK) {
                    val bought = purchases.orEmpty().map(playPurchaseMapper::toData)
                    BillingPurchaseResultDataModel.Completed(bought)
                } else {
                    sheetResultOf(result.responseCode)
                }
            )

            result.responseCode == BillingResponseCode.OK -> updates.tryEmit(Unit)
        }
    }

    private val client: BillingClient = newClient(context, purchasesUpdatedListener)

    private val mainScope = CoroutineScope(SupervisorJob() + mainContext)
    private val connectionLock = Any()
    private var pendingSetup: CompletableDeferred<Int>? = null
    private val productDetails = ConcurrentHashMap<String, ProductDetails>()

    override fun connect(): BillingConnectionDataModel = runBlocking { connected() }

    override fun products(
        subscriptionIds: List<String>,
        oneTimeIds: List<String>
    ): List<BillingProductDataModel> = runBlocking {
        requireConnected()
        val subscriptions = queryProducts(subscriptionIds, BillingClient.ProductType.SUBS)
        val oneTime = queryProducts(oneTimeIds, BillingClient.ProductType.INAPP)
        (subscriptions + oneTime).mapNotNull(playProductDetailsMapper::toData)
    }

    override fun purchases(): List<BillingPurchaseDataModel> = runBlocking {
        requireConnected()
        listOf(BillingClient.ProductType.SUBS, BillingClient.ProductType.INAPP).flatMap { type ->
            val result = client.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(type).build()
            )
            result.billingResult.throwUnlessOk()
            result.purchasesList
                // A suspended subscription is on account hold: it unlocks nothing (BRD EC-BIL-04).
                .filterNot(Purchase::isSuspended)
                .map(playPurchaseMapper::toData)
        }
    }

    override fun purchaseUpdates(): Flow<Unit> = updates.asSharedFlow()

    override fun purchase(
        request: BillingPurchaseRequestDataModel
    ): BillingPurchaseResultDataModel = runBlocking {
        when (connected()) {
            BillingConnectionDataModel.Available -> launch(request, retried = false)
            BillingConnectionDataModel.Unavailable -> BillingPurchaseResultDataModel.Unavailable
            BillingConnectionDataModel.Failed -> BillingPurchaseResultDataModel.Failed
        }
    }

    /** Tries again after each wait of [PlayBillingTimings.acknowledgeBackoffMillis] (review I2). */
    override fun acknowledge(purchaseToken: String): Boolean = runBlocking {
        val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchaseToken).build()
        var acknowledged = acknowledgeOnce(params)
        for (wait in timings.acknowledgeBackoffMillis) {
            if (acknowledged) break
            pause(wait)
            acknowledged = acknowledgeOnce(params)
        }
        acknowledged
    }

    override fun showInAppMessages(): Boolean = runBlocking {
        val activity = currentActivity()
        if (activity == null || connected() != BillingConnectionDataModel.Available) {
            return@runBlocking false
        }
        val answer = CompletableDeferred<Boolean>()
        val params = InAppMessageParams.newBuilder()
            .addInAppMessageCategoryToShow(InAppMessageParams.InAppMessageCategoryId.TRANSACTIONAL)
            .build()
        val shown = withContext(mainContext) {
            client.showInAppMessages(activity, params) { result ->
                answer.complete(
                    result.responseCode ==
                        InAppMessageResult.InAppMessageResponseCode.SUBSCRIPTION_STATUS_UPDATED
                )
            }
        }
        shown.responseCode == BillingResponseCode.OK &&
            withTimeoutOrNull(timings.messagesTimeoutMillis) { answer.await() } == true
    }

    override fun manageSubscription(productId: String) {
        val page = Uri.parse(SUBSCRIPTIONS_PAGE.format(productId, context.packageName))
        val intent = Intent(Intent.ACTION_VIEW, page)
        val activity = currentActivity()
        try {
            if (activity != null) {
                activity.startActivity(intent)
            } else {
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        } catch (_: ActivityNotFoundException) {
            // Neither Google Play nor a browser can open it; there is nothing else to offer.
        }
    }

    private suspend fun acknowledgeOnce(params: AcknowledgePurchaseParams): Boolean =
        connected() == BillingConnectionDataModel.Available &&
            client.acknowledgePurchase(params).responseCode == BillingResponseCode.OK

    /**
     * Opens the sheet and waits for Google Play's update. A launch the connection dropped is tried
     * once more; a sheet that never answers gives up after [PlayBillingTimings.sheetTimeoutMillis].
     */
    private suspend fun launch(
        request: BillingPurchaseRequestDataModel,
        retried: Boolean
    ): BillingPurchaseResultDataModel {
        val details = detailsOf(request.productId)
        val activity = currentActivity()
        if (details == null || activity == null) return BillingPurchaseResultDataModel.Failed
        val sheet = CompletableDeferred<BillingPurchaseResultDataModel>()
        // Only one sheet at a time: a second one closes the first unanswered.
        openSheet.getAndSet(sheet)?.complete(BillingPurchaseResultDataModel.Cancelled)
        val code = withContext(mainContext) {
            client.launchBillingFlow(activity, flowParams(details, request))
        }.responseCode
        if (code != BillingResponseCode.OK) openSheet.compareAndSet(sheet, null)
        return when {
            code == BillingResponseCode.OK ->
                withTimeoutOrNull(timings.sheetTimeoutMillis) { sheet.await() }
                    ?: BillingPurchaseResultDataModel.TimedOut.also {
                        openSheet.compareAndSet(sheet, null)
                    }

            !retried && code in RETRIED_LAUNCH &&
                connected() == BillingConnectionDataModel.Available ->
                launch(request, retried = true)

            else -> sheetResultOf(code)
        }
    }

    private fun flowParams(details: ProductDetails, request: BillingPurchaseRequestDataModel) =
        BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .apply { request.offerToken?.let(::setOfferToken) }
                        .build()
                )
            )
            .apply {
                request.oldPurchaseToken?.let { oldToken ->
                    setSubscriptionUpdateParams(
                        BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                            .setOldPurchaseToken(oldToken)
                            .setSubscriptionReplacementMode(
                                BillingFlowParams.SubscriptionUpdateParams.ReplacementMode
                                    .WITH_TIME_PRORATION
                            )
                            .build()
                    )
                }
            }
            .build()

    /** The details read last, or read now: a purchase sheet needs them, not only the product id. */
    private suspend fun detailsOf(productId: String): ProductDetails? = productDetails[productId]
        ?: try {
            queryProducts(listOf(productId), BillingClient.ProductType.SUBS)
                .ifEmpty { queryProducts(listOf(productId), BillingClient.ProductType.INAPP) }
                .firstOrNull()
        } catch (_: BillingDataException) {
            null
        }

    private suspend fun queryProducts(ids: List<String>, type: String): List<ProductDetails> {
        if (ids.isEmpty()) return emptyList()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                ids.map { id ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(id)
                        .setProductType(type)
                        .build()
                }
            )
            .build()
        val result = client.queryProductDetails(params)
        result.billingResult.throwUnlessOk()
        return result.productDetailsList.orEmpty().onEach { productDetails[it.productId] = it }
    }

    private suspend fun requireConnected() {
        when (connected()) {
            BillingConnectionDataModel.Available -> Unit
            BillingConnectionDataModel.Unavailable -> throw BillingUnavailableDataException()
            BillingConnectionDataModel.Failed -> throw BillingDataException("Not connected")
        }
    }

    /**
     * Ready, or set up a connection: callers that arrive meanwhile wait for the same one. One that
     * Google Play does not answer in time is dropped, so the next call tries afresh.
     */
    private suspend fun connected(): BillingConnectionDataModel {
        if (client.isReady) return BillingConnectionDataModel.Available
        val setup = synchronized(connectionLock) {
            pendingSetup ?: CompletableDeferred<Int>().also { started ->
                pendingSetup = started
                mainScope.launch { client.startConnection(listenerFor(started)) }
            }
        }
        val code = withTimeoutOrNull(timings.connectTimeoutMillis) { setup.await() }
        synchronized(connectionLock) { if (pendingSetup === setup) pendingSetup = null }
        return when (code) {
            BillingResponseCode.OK -> BillingConnectionDataModel.Available
            BillingResponseCode.BILLING_UNAVAILABLE -> BillingConnectionDataModel.Unavailable
            else -> BillingConnectionDataModel.Failed
        }
    }

    private fun listenerFor(setup: CompletableDeferred<Int>) = object : BillingClientStateListener {
        override fun onBillingSetupFinished(result: BillingResult) {
            setup.complete(result.responseCode)
        }

        // The client reconnects by itself at the next call (enableAutoServiceReconnection).
        override fun onBillingServiceDisconnected() = Unit
    }

    private fun BillingResult.throwUnlessOk() {
        when (responseCode) {
            BillingResponseCode.OK -> Unit
            BillingResponseCode.BILLING_UNAVAILABLE,
            BillingResponseCode.ITEM_UNAVAILABLE -> throw BillingUnavailableDataException()
            else -> throw BillingDataException("Google Play answered $responseCode: $debugMessage")
        }
    }
}

/** The app's one client: pending purchases accepted, and it reconnects by itself. */
private fun playBillingClient(context: Context, listener: PurchasesUpdatedListener): BillingClient =
    BillingClient.newBuilder(context)
        .setListener(listener)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()

/** How a sheet that did not complete closed; an unavailable item is not "try again" (review M10). */
internal fun sheetResultOf(responseCode: Int): BillingPurchaseResultDataModel =
    when (responseCode) {
        BillingResponseCode.USER_CANCELED -> BillingPurchaseResultDataModel.Cancelled
        BillingResponseCode.ITEM_ALREADY_OWNED -> BillingPurchaseResultDataModel.AlreadyOwned
        BillingResponseCode.BILLING_UNAVAILABLE,
        BillingResponseCode.ITEM_UNAVAILABLE -> BillingPurchaseResultDataModel.Unavailable
        else -> BillingPurchaseResultDataModel.Failed
    }
