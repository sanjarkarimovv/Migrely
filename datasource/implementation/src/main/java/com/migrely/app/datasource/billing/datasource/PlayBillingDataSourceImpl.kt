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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

private const val CONNECT_TIMEOUT_MILLIS = 5_000L
private const val MESSAGES_TIMEOUT_MILLIS = 10 * 60_000L
private const val SUBSCRIPTIONS_PAGE =
    "https://play.google.com/store/account/subscriptions?sku=%s&package=%s"

/**
 * Google Play Billing Library 8 (BRD D105, D107). One client for the app: it accepts pending
 * purchases, reconnects by itself when the Play Store drops the connection, and keeps the product
 * details it read, which a purchase needs. Google Play answers on the main thread, so each blocking
 * call waits for its answer on the caller's thread. [currentActivity] is the activity on screen,
 * which the purchase sheet and Play's messages open over.
 */
class PlayBillingDataSourceImpl(
    private val context: Context,
    private val currentActivity: () -> Activity?,
    private val playProductDetailsMapper: PlayProductDetailsMapper,
    private val playPurchaseMapper: PlayPurchaseMapper
) : BillingDataSource {
    /** The purchase sheet that is open, which the next update from Google Play closes. */
    private val openSheet = AtomicReference<CompletableDeferred<BillingPurchaseResultDataModel>?>()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        val outcome = if (result.responseCode == BillingResponseCode.OK) {
            val bought = purchases.orEmpty().map(playPurchaseMapper::toData)
            BillingPurchaseResultDataModel.Completed(bought)
        } else {
            sheetFailureOf(result.responseCode)
        }
        // An update with no sheet open, such as a pending payment that completed, is read at the
        // next check.
        openSheet.getAndSet(null)?.complete(outcome)
    }

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .enableAutoServiceReconnection()
        .build()

    private val connecting = Mutex()
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

    override fun purchase(
        request: BillingPurchaseRequestDataModel
    ): BillingPurchaseResultDataModel = runBlocking {
        when (connected()) {
            BillingConnectionDataModel.Available -> launch(request)
            BillingConnectionDataModel.Unavailable -> BillingPurchaseResultDataModel.Unavailable
            BillingConnectionDataModel.Failed -> BillingPurchaseResultDataModel.Failed
        }
    }

    override fun acknowledge(purchaseToken: String): Boolean = runBlocking {
        connected() == BillingConnectionDataModel.Available &&
            client.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchaseToken).build()
            ).responseCode == BillingResponseCode.OK
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
        val shown = withContext(Dispatchers.Main) {
            client.showInAppMessages(activity, params) { result ->
                answer.complete(
                    result.responseCode ==
                        InAppMessageResult.InAppMessageResponseCode.SUBSCRIPTION_STATUS_UPDATED
                )
            }
        }
        shown.responseCode == BillingResponseCode.OK &&
            withTimeoutOrNull(MESSAGES_TIMEOUT_MILLIS) { answer.await() } == true
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

    private suspend fun launch(
        request: BillingPurchaseRequestDataModel
    ): BillingPurchaseResultDataModel {
        val details = detailsOf(request.productId)
        val activity = currentActivity()
        if (details == null || activity == null) return BillingPurchaseResultDataModel.Failed
        val product = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .apply { request.offerToken?.let(::setOfferToken) }
            .build()
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(product))
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
        val sheet = CompletableDeferred<BillingPurchaseResultDataModel>()
        // Only one sheet at a time: a second one closes the first unanswered.
        openSheet.getAndSet(sheet)?.complete(BillingPurchaseResultDataModel.Cancelled)
        val launched = withContext(Dispatchers.Main) { client.launchBillingFlow(activity, params) }
        return if (launched.responseCode == BillingResponseCode.OK) {
            sheet.await()
        } else {
            openSheet.compareAndSet(sheet, null)
            sheetFailureOf(launched.responseCode)
        }
    }

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

    private suspend fun connected(): BillingConnectionDataModel = connecting.withLock {
        if (client.isReady) return@withLock BillingConnectionDataModel.Available
        val setup = CompletableDeferred<Int>()
        withContext(Dispatchers.Main) {
            client.startConnection(
                object : BillingClientStateListener {
                    override fun onBillingSetupFinished(result: BillingResult) {
                        setup.complete(result.responseCode)
                    }

                    // The client reconnects by itself at the next call
                    // (enableAutoServiceReconnection).
                    override fun onBillingServiceDisconnected() = Unit
                }
            )
        }
        when (withTimeoutOrNull(CONNECT_TIMEOUT_MILLIS) { setup.await() }) {
            BillingResponseCode.OK -> BillingConnectionDataModel.Available
            BillingResponseCode.BILLING_UNAVAILABLE -> BillingConnectionDataModel.Unavailable
            else -> BillingConnectionDataModel.Failed
        }
    }

    private fun BillingResult.throwUnlessOk() {
        when (responseCode) {
            BillingResponseCode.OK -> Unit
            BillingResponseCode.BILLING_UNAVAILABLE -> throw BillingUnavailableDataException()
            else -> throw BillingDataException("Google Play answered $responseCode: $debugMessage")
        }
    }
}

private fun sheetFailureOf(responseCode: Int): BillingPurchaseResultDataModel =
    when (responseCode) {
        BillingResponseCode.USER_CANCELED -> BillingPurchaseResultDataModel.Cancelled
        BillingResponseCode.ITEM_ALREADY_OWNED -> BillingPurchaseResultDataModel.AlreadyOwned
        BillingResponseCode.BILLING_UNAVAILABLE -> BillingPurchaseResultDataModel.Unavailable
        else -> BillingPurchaseResultDataModel.Failed
    }
