package com.migrely.app.subscription.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.subscription.domain.model.BillingAvailabilityDomainModel
import com.migrely.app.subscription.domain.model.BuyPremiumResultDomainModel
import com.migrely.app.subscription.domain.model.ManageSubscriptionResultDomainModel
import com.migrely.app.subscription.domain.model.PaywallDomainModel
import com.migrely.app.subscription.domain.model.PaywallOffersDomainModel
import com.migrely.app.subscription.domain.model.PremiumCheckDomainModel
import com.migrely.app.subscription.domain.model.PremiumStatusDomainModel
import com.migrely.app.subscription.domain.model.PurchaseOutcomeDomainModel
import com.migrely.app.subscription.domain.model.RestorePurchasesResultDomainModel
import com.migrely.app.subscription.domain.usecase.BuyPremiumUseCase
import com.migrely.app.subscription.domain.usecase.GetPaywallUseCase
import com.migrely.app.subscription.domain.usecase.ManageSubscriptionUseCase
import com.migrely.app.subscription.domain.usecase.MarkPaywallShownUseCase
import com.migrely.app.subscription.domain.usecase.RestorePurchasesUseCase
import com.migrely.app.subscription.presentation.mapper.PaywallPresentationMapper
import com.migrely.app.subscription.presentation.mapper.PaywallProductDomainMapper
import com.migrely.app.subscription.presentation.mapper.PaywallTriggerDomainMapper
import com.migrely.app.subscription.presentation.mapper.PremiumStatusPresentationMapper
import com.migrely.app.subscription.presentation.model.PaywallContextPresentationModel
import com.migrely.app.subscription.presentation.model.PaywallPresentationNotification
import com.migrely.app.subscription.presentation.model.PaywallProductPresentationModel
import com.migrely.app.subscription.presentation.model.PaywallViewState
import com.migrely.app.subscription.presentation.navigation.PaywallPresentationNavigationEvent

/** The use cases the paywall runs, grouped. */
class PaywallUseCases(
    val getPaywallUseCase: GetPaywallUseCase,
    val buyPremiumUseCase: BuyPremiumUseCase,
    val restorePurchasesUseCase: RestorePurchasesUseCase,
    val manageSubscriptionUseCase: ManageSubscriptionUseCase,
    val markPaywallShownUseCase: MarkPaywallShownUseCase
)

/**
 * The paywall (BRD §7.3, D109). The UI keeps the chosen product and passes it with each event.
 * Google Play's sheet can outlast a rotation, so, like the doctor report, this view model lives as
 * long as the app and keeps, only for delivery, whether a purchase is going on and an outcome the
 * screen has not taken (ARCHITECTURE section 13).
 */
class PaywallViewModel(
    private val useCases: PaywallUseCases,
    private val paywallPresentationMapper: PaywallPresentationMapper,
    private val premiumStatusPresentationMapper: PremiumStatusPresentationMapper,
    private val paywallProductDomainMapper: PaywallProductDomainMapper,
    private val paywallTriggerDomainMapper: PaywallTriggerDomainMapper,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<PaywallViewState, PaywallPresentationNotification>(useCaseExecutor) {
    /**
     * Purchases and restores that have not answered yet. The paywall is working only while one is
     * out, so it never stays on "Waiting": each answers, as a result or a failure, and Google
     * Play's sheet gives up after a while (review M11).
     */
    private var inFlight = 0
    private val working get() = inFlight > 0
    private var undelivered: PaywallPresentationNotification? = null

    /** A tap on "Try again" is out: one more does not start a second reading. */
    private var retrying = false

    /** Counts the paywalls opened, so a reading from an earlier one is not shown on this one. */
    private var visit = 0

    /**
     * A new paywall: an outcome left from an earlier one is not shown again, and a reading still
     * running from it is dropped; this one's own reading finds what has happened since. One that
     * showed itself is marked as it appears, so it counts toward the 7-day limit (design D5).
     */
    fun onEnter(context: PaywallContextPresentationModel) {
        visit++
        retrying = false
        undelivered = null
        paywallTriggerDomainMapper.toDomain(context)?.let { trigger ->
            useCases.markPaywallShownUseCase(value = trigger)
        }
        load(context)
    }

    /** The screen listens again, after a rotation for example: it gets the state and an outcome. */
    fun onResume(context: PaywallContextPresentationModel) {
        load(context)
        undelivered?.let(::notify)
    }

    /**
     * Google Play reported a purchase change while the paywall shows, such as a pending payment
     * that completed: it reads Premium again, and shows its success once Premium is on, whatever
     * it was opened from (review I1).
     */
    fun onPurchaseUpdatedAction(context: PaywallContextPresentationModel) =
        load(context, celebrate = true)

    fun onOutcomeShownAction() {
        undelivered = null
    }

    /** The notice's button: it answers at once, and says so if Google Play still gives no answer. */
    fun onRetryAction(context: PaywallContextPresentationModel) {
        if (retrying || working) return
        retrying = true
        updateViewState(PaywallViewState.Loading)
        load(context, retried = true)
    }

    fun onBuyAction(
        context: PaywallContextPresentationModel,
        product: PaywallProductPresentationModel
    ) {
        // Two taps in one frame open one sheet.
        if (working) return
        inFlight++
        // An outcome the screen missed is about an earlier tap: a rotation must not bring it back.
        undelivered = null
        updateViewState(PaywallViewState.Working)
        useCases.buyPremiumUseCase(
            value = paywallProductDomainMapper.toDomain(product),
            onResult = { result ->
                inFlight--
                outcomeOf(result)?.let(::deliver)
                load(
                    context,
                    sheetRefused = result.outcome == PurchaseOutcomeDomainModel.Unavailable
                )
            },
            onException = { answered(context) }
        )
    }

    fun onRestoreAction(context: PaywallContextPresentationModel) {
        if (working) return
        inFlight++
        updateViewState(PaywallViewState.Working)
        useCases.restorePurchasesUseCase(
            onResult = { result ->
                inFlight--
                deliver(restoredOf(result))
                load(context)
            },
            onException = { answered(context) }
        )
    }

    /** Google Play's page, to cancel a subscription that renews under lifetime (BRD EC-BIL-09). */
    fun onManageAction() {
        useCases.manageSubscriptionUseCase(
            onResult = { result ->
                when (result) {
                    is ManageSubscriptionResultDomainModel.Opened -> if (result.status.unlocks()) {
                        deliver(
                            PaywallPresentationNotification.Unlocked(
                                premiumStatusPresentationMapper.toPresentation(result.status)
                            )
                        )
                    }
                    is ManageSubscriptionResultDomainModel.Offline ->
                        deliver(PaywallPresentationNotification.OfflineToManage)
                }
            }
        )
    }

    fun onCloseAction(context: PaywallContextPresentationModel) =
        emitNavigationEvent(PaywallPresentationNavigationEvent.OnClose(context))

    fun onContinueAction(context: PaywallContextPresentationModel) =
        emitNavigationEvent(PaywallPresentationNavigationEvent.OnPremiumUnlocked(context))

    private fun outcomeOf(result: BuyPremiumResultDomainModel): PaywallPresentationNotification? =
        when (result.outcome) {
            PurchaseOutcomeDomainModel.Unlocked -> PaywallPresentationNotification.Unlocked(
                premiumStatusPresentationMapper.toPresentation(result.status)
            )
            PurchaseOutcomeDomainModel.Failed -> PaywallPresentationNotification.SomethingWentWrong
            PurchaseOutcomeDomainModel.Offline -> PaywallPresentationNotification.OfflineToBuy
            // Pending shows once the paywall loads again, and a cancel shows nothing (BRD D153).
            // Unavailable waits for that load, which knows whether Play still offers the products.
            PurchaseOutcomeDomainModel.Pending,
            PurchaseOutcomeDomainModel.Cancelled,
            PurchaseOutcomeDomainModel.Unavailable -> null
        }

    private fun restoredOf(result: RestorePurchasesResultDomainModel) = when (result) {
        is RestorePurchasesResultDomainModel.Checked -> restoredOf(result.check)
        is RestorePurchasesResultDomainModel.Offline ->
            PaywallPresentationNotification.OfflineToRestore
    }

    private fun restoredOf(check: PremiumCheckDomainModel): PaywallPresentationNotification = when {
        check.status.unlocks() -> PaywallPresentationNotification.Unlocked(
            premiumStatusPresentationMapper.toPresentation(check.status)
        )
        check.billing == BillingAvailabilityDomainModel.Unknown ->
            PaywallPresentationNotification.PlayUnreachable
        else -> PaywallPresentationNotification.NothingToRestore
    }

    /** A call that failed before it could answer: the paywall leaves "Waiting" and says so. */
    private fun answered(context: PaywallContextPresentationModel) {
        inFlight--
        deliver(PaywallPresentationNotification.SomethingWentWrong)
        load(context)
    }

    private fun deliver(notification: PaywallPresentationNotification) {
        undelivered = notification
        notify(notification)
    }

    /**
     * A premium user who reaches a feature's paywall, for example before the first check after an
     * install finished, sees its success and goes on; from Settings, a subscriber sees the plans.
     */
    private fun load(
        context: PaywallContextPresentationModel,
        celebrate: Boolean = false,
        sheetRefused: Boolean = false,
        retried: Boolean = false
    ) {
        if (working) {
            updateViewState(PaywallViewState.Working)
            return
        }
        val visitOfThisReading = visit
        useCases.getPaywallUseCase(
            onResult = { paywall ->
                if (visitOfThisReading == visit) {
                    if (retried) retrying = false
                    val unreachable = saysUnreachable(paywall.offers, sheetRefused, retried)
                    show(paywall, context, celebrate, unreachable)
                }
            },
            // Only what the executor reports: a throw inside a background use case does not get
            // here. "Getting prices" lasts until the reading answers, and only connecting to Google
            // Play has a timeout, so Close and Restore stay available meanwhile.
            onException = {
                if (retried && visitOfThisReading == visit) {
                    retrying = false
                    updateViewState(PaywallViewState.Unreachable)
                    deliver(PaywallPresentationNotification.PlayUnreachable)
                }
            }
        )
    }

    private fun show(
        paywall: PaywallDomainModel,
        context: PaywallContextPresentationModel,
        celebrate: Boolean,
        unreachable: Boolean
    ) {
        if (working) return
        updateViewState(paywallPresentationMapper.toPresentation(paywall))
        if ((celebrate || context != PaywallContextPresentationModel.Settings) &&
            paywall.status.unlocks()
        ) {
            deliver(
                PaywallPresentationNotification.Unlocked(
                    premiumStatusPresentationMapper.toPresentation(paywall.status)
                )
            )
        } else if (unreachable) {
            deliver(PaywallPresentationNotification.PlayUnreachable)
        }
    }

    /**
     * After a refused sheet, cards that come back say why the tap did nothing; with no cards, the
     * paywall's own notice says it (BRD EC-BIL-16). After "Try again", the notice that returns says
     * the tap was heard (BRD D153).
     */
    private fun saysUnreachable(
        offers: PaywallOffersDomainModel,
        sheetRefused: Boolean,
        retried: Boolean
    ) = when (offers) {
        is PaywallOffersDomainModel.Loaded -> sheetRefused
        PaywallOffersDomainModel.Unreachable -> retried
        PaywallOffersDomainModel.Unavailable -> false
    }

    private fun PremiumStatusDomainModel.unlocks() = when (this) {
        PremiumStatusDomainModel.Free, PremiumStatusDomainModel.Pending -> false
        is PremiumStatusDomainModel.Subscribed, is PremiumStatusDomainModel.Lifetime -> true
    }
}
