package com.migrely.app.datasource.billing.datasource

import android.content.Context
import android.content.SharedPreferences
import com.migrely.app.R
import com.migrely.app.datasource.billing.model.BillingConnectionDataModel
import com.migrely.app.datasource.billing.model.BillingOfferDataModel
import com.migrely.app.datasource.billing.model.BillingProductDataModel
import com.migrely.app.datasource.billing.model.BillingProductKindDataModel
import com.migrely.app.datasource.billing.model.BillingPurchaseDataModel
import com.migrely.app.datasource.billing.model.BillingPurchaseRequestDataModel
import com.migrely.app.datasource.billing.model.BillingPurchaseResultDataModel
import com.migrely.app.datasource.billing.model.BillingPurchaseStateDataModel
import com.migrely.app.datasource.exception.BillingDataException
import com.migrely.app.datasource.exception.BillingUnavailableDataException
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * The products Play Console holds for Migrely (docs/release/play-products.md), as the simulated
 * store sells them.
 */
private const val PREMIUM = "migrely_premium"
private const val LIFETIME = "migrely_lifetime"
private const val YEARLY = "yearly"
private const val MONTHLY = "monthly"
private const val TRIAL_OFFER = "free-trial-7d"
private const val YEARLY_TOKEN = "sim-yearly"
private const val YEARLY_TRIAL_TOKEN = "sim-yearly-trial"
private const val MONTHLY_TOKEN = "sim-monthly"
private const val LIFETIME_TOKEN = "sim-lifetime"

/** The US prices of BRD §7.2, written in the phone's currency: fake, but local-looking. */
private const val YEARLY_PRICE = 29.99
private const val MONTHLY_PRICE = 4.99
private const val LIFETIME_PRICE = 59.99

private const val PURCHASES = "purchases"
private const val TRIAL_USED = "trial_used"
private const val MODE = "mode"
private const val FIELD_SEPARATOR = "|"
private const val FIELDS = 6
private const val UPDATES_BUFFER = 8

/**
 * The store's updates while no sheet is open, shared by every instance as the store's file is: the
 * adb receiver's instance settles a pending payment, and the app's hears of it (review I1).
 */
private val storeUpdates = MutableSharedFlow<Unit>(extraBufferCapacity = UPDATES_BUFFER)

/** What the simulated store answers to a connection, set from adb by [SimulatedBillingReceiver]. */
enum class SimulatedStoreMode(val key: String) {
    Available("available"),

    /** Billing connects, but Google Play knows none of Migrely's products yet (BRD D110, D122). */
    NoProducts("no-products"),
    Unavailable("unavailable"),
    Unreachable("unreachable")
}

/**
 * A store for debug builds in place of Google Play (BRD D113), for the emulator walk and QA: the
 * same products with fake prices in the phone's locale, and a 7-day free trial on the yearly plan
 * until it is used. Its purchase sheet can complete a purchase, leave it pending, or cancel; a
 * pending payment is completed or declined from the next sheet, or from adb, which reports it as
 * Google Play does a payment completed at a shop; its subscriptions page ends a subscription or
 * refunds lifetime. The store lives in its own preferences file, which "Delete all
 * data" leaves alone, so a purchase outlives the restart that follows, as it would in Google Play.
 * Purchases carry no signature to check. Release builds do not contain this class.
 */
class SimulatedBillingDataSourceImpl(
    private val context: Context,
    private val store: SharedPreferences,
    private val sheet: SimulatedSheet,
    private val now: () -> Long = System::currentTimeMillis,
    private val locale: () -> Locale = Locale::getDefault
) : BillingDataSource {
    override fun connect(): BillingConnectionDataModel = when (mode()) {
        SimulatedStoreMode.Available,
        SimulatedStoreMode.NoProducts -> BillingConnectionDataModel.Available
        SimulatedStoreMode.Unavailable -> BillingConnectionDataModel.Unavailable
        SimulatedStoreMode.Unreachable -> BillingConnectionDataModel.Failed
    }

    override fun products(
        subscriptionIds: List<String>,
        oneTimeIds: List<String>
    ): List<BillingProductDataModel> {
        requireAvailable()
        if (mode() == SimulatedStoreMode.NoProducts) return emptyList()
        val premium = BillingProductDataModel(
            PREMIUM,
            BillingProductKindDataModel.Subscription,
            listOfNotNull(
                offer(YEARLY, null, YEARLY_TOKEN, YEARLY_PRICE, "P1Y"),
                offer(YEARLY, TRIAL_OFFER, YEARLY_TRIAL_TOKEN, YEARLY_PRICE, "P1Y", "P7D")
                    .takeUnless { store.getBoolean(TRIAL_USED, false) },
                offer(MONTHLY, null, MONTHLY_TOKEN, MONTHLY_PRICE, "P1M")
            )
        )
        val lifetime = BillingProductDataModel(
            LIFETIME,
            BillingProductKindDataModel.OneTime,
            listOf(offer(null, null, LIFETIME_TOKEN, LIFETIME_PRICE, null))
        )
        return listOf(premium).filter { it.productId in subscriptionIds } +
            listOf(lifetime).filter { it.productId in oneTimeIds }
    }

    override fun purchases(): List<BillingPurchaseDataModel> {
        requireAvailable()
        return stored()
    }

    override fun purchase(
        request: BillingPurchaseRequestDataModel
    ): BillingPurchaseResultDataModel {
        when (mode()) {
            SimulatedStoreMode.Available -> Unit
            // As Google Play answers ITEM_UNAVAILABLE for a product it does not know.
            SimulatedStoreMode.NoProducts,
            SimulatedStoreMode.Unavailable -> return BillingPurchaseResultDataModel.Unavailable
            SimulatedStoreMode.Unreachable -> return BillingPurchaseResultDataModel.Failed
        }
        val owned = stored()
        val pending = owned.firstOrNull { it.state == BillingPurchaseStateDataModel.Pending }
        return when {
            pending != null -> settle(pending)
            owned.any { request.productId in it.productIds } && request.oldPurchaseToken == null ->
                BillingPurchaseResultDataModel.AlreadyOwned
            else -> buy(request)
        }
    }

    override fun acknowledge(purchaseToken: String): Boolean {
        if (connect() != BillingConnectionDataModel.Available) return false
        save(
            stored().map {
                if (it.purchaseToken ==
                    purchaseToken
                ) {
                    it.copy(acknowledged = true)
                } else {
                    it
                }
            }
        )
        return true
    }

    override fun purchaseUpdates(): Flow<Unit> = storeUpdates.asSharedFlow()

    override fun showInAppMessages(): Boolean = false

    override fun manageSubscription(productId: String) {
        val owned = stored().filter { it.state == BillingPurchaseStateDataModel.Purchased }
        val labels = owned.map { purchase ->
            if (LIFETIME in purchase.productIds) {
                R.string.simulated_store_refund_lifetime
            } else {
                R.string.simulated_store_end_subscription
            }
        }.map(context::getString) + context.getString(R.string.simulated_store_close)
        val chosen = sheet.choose(context.getString(R.string.simulated_store_manage_title), labels)
        owned.getOrNull(chosen ?: return)?.let { ended ->
            save(stored().filterNot { it.purchaseToken == ended.purchaseToken })
        }
    }

    /**
     * Sets what the store answers, empties it, or settles a pending payment with no sheet open, for
     * QA from adb.
     */
    fun apply(command: String) {
        val mode = SimulatedStoreMode.entries.firstOrNull { it.key == command }
        when {
            mode != null -> store.edit().putString(MODE, mode.key).apply()
            command == "reset" -> store.edit().clear().apply()
            command == "complete-pending" -> settleOutside(complete = true)
            command == "decline-pending" -> settleOutside(complete = false)
        }
    }

    private fun settleOutside(complete: Boolean) {
        val pending = stored().firstOrNull { it.state == BillingPurchaseStateDataModel.Pending }
            ?: return
        save(
            if (complete) {
                stored().map {
                    if (it.purchaseToken == pending.purchaseToken) {
                        it.copy(state = BillingPurchaseStateDataModel.Purchased)
                    } else {
                        it
                    }
                }
            } else {
                stored().filterNot { it.purchaseToken == pending.purchaseToken }
            }
        )
        storeUpdates.tryEmit(Unit)
    }

    private fun buy(request: BillingPurchaseRequestDataModel): BillingPurchaseResultDataModel {
        val title = context.getString(R.string.simulated_store_title) + "\n" + describe(request)
        val options = listOf(
            R.string.simulated_store_complete,
            R.string.simulated_store_pending,
            R.string.simulated_store_cancel
        ).map(context::getString)
        val state = when (sheet.choose(title, options)) {
            0 -> BillingPurchaseStateDataModel.Purchased
            1 -> BillingPurchaseStateDataModel.Pending
            else -> return BillingPurchaseResultDataModel.Cancelled
        }
        val purchase = BillingPurchaseDataModel(
            productIds = listOf(request.productId),
            state = state,
            purchaseTimeMillis = now(),
            autoRenewing = request.productId == PREMIUM,
            purchaseToken = "sim-" + UUID.randomUUID(),
            acknowledged = false,
            signatureValid = true
        )
        // A replaced subscription ends as the new one starts (BRD EC-BIL-10).
        save(stored().filterNot { it.purchaseToken == request.oldPurchaseToken } + purchase)
        // Like Google Play, the trial is only for new subscribers: any subscription uses it up.
        if (request.productId == PREMIUM) {
            store.edit().putBoolean(TRIAL_USED, true).apply()
        }
        return BillingPurchaseResultDataModel.Completed(listOf(purchase))
    }

    private fun settle(pending: BillingPurchaseDataModel): BillingPurchaseResultDataModel {
        val options = listOf(
            R.string.simulated_store_complete_pending,
            R.string.simulated_store_decline_pending,
            R.string.simulated_store_cancel
        ).map(context::getString)
        return when (
            sheet.choose(
                context.getString(R.string.simulated_store_pending_title),
                options
            )
        ) {
            0 -> {
                val completed = pending.copy(state = BillingPurchaseStateDataModel.Purchased)
                save(
                    stored().map {
                        if (it.purchaseToken ==
                            pending.purchaseToken
                        ) {
                            completed
                        } else {
                            it
                        }
                    }
                )
                BillingPurchaseResultDataModel.Completed(listOf(completed))
            }

            1 -> {
                save(stored().filterNot { it.purchaseToken == pending.purchaseToken })
                BillingPurchaseResultDataModel.Cancelled
            }

            else -> BillingPurchaseResultDataModel.Cancelled
        }
    }

    private fun describe(request: BillingPurchaseRequestDataModel): String =
        when (request.offerToken) {
            YEARLY_TOKEN -> context.getString(R.string.simulated_store_yearly, price(YEARLY_PRICE))
            YEARLY_TRIAL_TOKEN ->
                context.getString(R.string.simulated_store_yearly_trial, price(YEARLY_PRICE))
            MONTHLY_TOKEN -> context.getString(
                R.string.simulated_store_monthly,
                price(MONTHLY_PRICE)
            )
            else -> context.getString(R.string.simulated_store_lifetime, price(LIFETIME_PRICE))
        }

    private fun offer(
        basePlanId: String?,
        offerId: String?,
        token: String,
        amount: Double,
        period: String?,
        trial: String? = null
    ) = BillingOfferDataModel(basePlanId, offerId, token, price(amount), period, trial)

    private fun price(amount: Double): String =
        NumberFormat.getCurrencyInstance(locale()).format(amount)

    private fun mode(): SimulatedStoreMode = store.getString(MODE, null)
        ?.let { key -> SimulatedStoreMode.entries.firstOrNull { it.key == key } }
        ?: SimulatedStoreMode.Available

    private fun requireAvailable() {
        when (mode()) {
            SimulatedStoreMode.Available, SimulatedStoreMode.NoProducts -> Unit
            SimulatedStoreMode.Unavailable -> throw BillingUnavailableDataException()
            SimulatedStoreMode.Unreachable -> throw BillingDataException(
                "The simulated store is unreachable"
            )
        }
    }

    private fun stored(): List<BillingPurchaseDataModel> =
        store.getStringSet(PURCHASES, emptySet()).orEmpty().mapNotNull(::decode).sortedBy {
            it.purchaseTimeMillis
        }

    private fun save(purchases: List<BillingPurchaseDataModel>) {
        store.edit().putStringSet(PURCHASES, purchases.map(::encode).toSet()).commit()
    }

    private fun encode(purchase: BillingPurchaseDataModel) = listOf(
        purchase.productIds.single(),
        purchase.purchaseToken,
        purchase.purchaseTimeMillis,
        purchase.state.name,
        purchase.autoRenewing,
        purchase.acknowledged
    ).joinToString(FIELD_SEPARATOR)

    private fun decode(line: String): BillingPurchaseDataModel? {
        val fields = line.split(FIELD_SEPARATOR)
        if (fields.size != FIELDS) return null
        return BillingPurchaseDataModel(
            productIds = listOf(fields[0]),
            state = BillingPurchaseStateDataModel.entries.firstOrNull { it.name == fields[3] }
                ?: return null,
            purchaseTimeMillis = fields[2].toLongOrNull() ?: return null,
            autoRenewing = fields[4].toBoolean(),
            purchaseToken = fields[1],
            acknowledged = fields[5].toBoolean(),
            signatureValid = true
        )
    }
}

/**
 * The simulated store's sheets: [choose] shows [options] under [title] and returns the index
 * chosen, or null.
 */
fun interface SimulatedSheet {
    fun choose(title: String, options: List<String>): Int?
}
