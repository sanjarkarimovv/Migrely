package com.migrely.app.datasource.billing.datasource

import com.migrely.app.datasource.billing.model.BillingConnectionDataModel
import com.migrely.app.datasource.billing.model.BillingProductDataModel
import com.migrely.app.datasource.billing.model.BillingPurchaseDataModel
import com.migrely.app.datasource.billing.model.BillingPurchaseRequestDataModel
import com.migrely.app.datasource.billing.model.BillingPurchaseResultDataModel
import kotlinx.coroutines.flow.Flow

/**
 * Google Play's billing (BRD D105, D107), feature-agnostic: it knows product ids, not what they
 * unlock. Every function but [purchaseUpdates] blocks and must be called off the main thread.
 * [products] and [purchases] throw
 * [com.migrely.app.datasource.exception.BillingUnavailableDataException] when billing cannot be
 * used on this phone and [com.migrely.app.datasource.exception.BillingDataException] when Google
 * Play does not answer.
 */
interface BillingDataSource {
    /** Connects when needed, and says whether billing can be used. Never throws. */
    fun connect(): BillingConnectionDataModel

    /**
     * The products Google Play knows among those asked for, with its local prices; unknown ids are
     * left out.
     */
    fun products(
        subscriptionIds: List<String>,
        oneTimeIds: List<String>
    ): List<BillingProductDataModel>

    /**
     * The purchases this Google account owns now: active subscriptions and one-time products,
     * pending ones included.
     */
    fun purchases(): List<BillingPurchaseDataModel>

    /**
     * A signal each time Google Play reports a purchase change while no purchase sheet waits for
     * it, such as a pending payment completed at a shop. Collect it for as long as the app runs.
     */
    fun purchaseUpdates(): Flow<Unit>

    /**
     * Opens Google Play's purchase sheet and waits until it closes, or gives up after a long while
     * with [BillingPurchaseResultDataModel.TimedOut]. Never throws.
     */
    fun purchase(request: BillingPurchaseRequestDataModel): BillingPurchaseResultDataModel

    /**
     * Acknowledges a purchase, which Google Play refunds when it is not acknowledged within 3 days,
     * trying again a few times before it gives up.
     */
    fun acknowledge(purchaseToken: String): Boolean

    /**
     * Lets Google Play show its own messages, such as a payment that failed during a grace period
     * or an account hold (BRD EC-BIL-03, EC-BIL-04), and waits until they close. True when the user
     * changed a subscription there.
     */
    fun showInAppMessages(): Boolean

    /**
     * Opens Google Play's page for managing the subscription [productId], where it can be
     * cancelled.
     */
    fun manageSubscription(productId: String)
}
