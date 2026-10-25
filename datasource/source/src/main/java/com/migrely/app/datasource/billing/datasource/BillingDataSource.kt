package com.migrely.app.datasource.billing.datasource

import com.migrely.app.datasource.billing.model.BillingConnectionDataModel
import com.migrely.app.datasource.billing.model.BillingProductDataModel
import com.migrely.app.datasource.billing.model.BillingPurchaseDataModel
import com.migrely.app.datasource.billing.model.BillingPurchaseRequestDataModel
import com.migrely.app.datasource.billing.model.BillingPurchaseResultDataModel

/**
 * Google Play's billing (BRD D105, D107), feature-agnostic: it knows product ids, not what they
 * unlock. Every function blocks and must be called off the main thread. [products] and [purchases]
 * throw [com.migrely.app.datasource.exception.BillingUnavailableDataException] when billing cannot
 * be used on this phone and [com.migrely.app.datasource.exception.BillingDataException] when Google
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

    /** Opens Google Play's purchase sheet and waits until it closes. Never throws. */
    fun purchase(request: BillingPurchaseRequestDataModel): BillingPurchaseResultDataModel

    /**
     * Acknowledges a purchase, which Google Play refunds when it is not acknowledged within 3 days.
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
