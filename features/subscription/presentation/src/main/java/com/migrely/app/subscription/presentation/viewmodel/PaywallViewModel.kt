package com.migrely.app.subscription.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.subscription.domain.model.BillingAvailabilityDomainModel
import com.migrely.app.subscription.domain.model.BuyPremiumResultDomainModel
import com.migrely.app.subscription.domain.model.PremiumCheckDomainModel
import com.migrely.app.subscription.domain.model.PremiumStatusDomainModel
import com.migrely.app.subscription.domain.model.PurchaseOutcomeDomainModel
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
    private var working = false
    private var undelivered: PaywallPresentationNotification? = null

    /**
     * A new paywall: an outcome left from an earlier one is not shown again. One that showed itself
     * is marked as it appears, so it counts toward the 7-day limit (design D5).
     */
    fun onEnter(context: PaywallContextPresentationModel) {
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

    fun onOutcomeShownAction() {
        undelivered = null
    }

    fun onRetryAction(context: PaywallContextPresentationModel) = load(context)

    fun onBuyAction(
        context: PaywallContextPresentationModel,
        product: PaywallProductPresentationModel
    ) {
        // Two taps in one frame open one sheet.
        if (working) return
        working = true
        updateViewState(PaywallViewState.Working)
        useCases.buyPremiumUseCase(
            value = paywallProductDomainMapper.toDomain(product),
            onResult = { result ->
                working = false
                outcomeOf(result)?.let(::deliver)
                load(context)
            }
        )
    }

    fun onRestoreAction(context: PaywallContextPresentationModel) {
        if (working) return
        working = true
        updateViewState(PaywallViewState.Working)
        useCases.restorePurchasesUseCase(
            onResult = { check ->
                working = false
                deliver(restoredOf(check))
                load(context)
            }
        )
    }

    /** Google Play's page, to cancel a subscription that renews under lifetime (BRD EC-BIL-09). */
    fun onManageAction() {
        useCases.manageSubscriptionUseCase(
            onResult = { status ->
                if (status.unlocks()) {
                    deliver(
                        PaywallPresentationNotification.Unlocked(
                            premiumStatusPresentationMapper.toPresentation(status)
                        )
                    )
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
            // The paywall shows these itself once it loads again.
            PurchaseOutcomeDomainModel.Pending,
            PurchaseOutcomeDomainModel.Cancelled,
            PurchaseOutcomeDomainModel.Unavailable -> null
        }

    private fun restoredOf(check: PremiumCheckDomainModel): PaywallPresentationNotification = when {
        check.status.unlocks() -> PaywallPresentationNotification.Unlocked(
            premiumStatusPresentationMapper.toPresentation(check.status)
        )
        check.billing == BillingAvailabilityDomainModel.Unknown ->
            PaywallPresentationNotification.PlayUnreachable
        else -> PaywallPresentationNotification.NothingToRestore
    }

    private fun deliver(notification: PaywallPresentationNotification) {
        undelivered = notification
        notify(notification)
    }

    /**
     * A premium user who reaches a feature's paywall, for example before the first check after an
     * install finished, sees its success and goes on; from Settings, a subscriber sees the plans.
     */
    private fun load(context: PaywallContextPresentationModel) {
        if (working) {
            updateViewState(PaywallViewState.Working)
            return
        }
        useCases.getPaywallUseCase(
            onResult = { paywall ->
                if (!working) {
                    updateViewState(paywallPresentationMapper.toPresentation(paywall))
                    if (context != PaywallContextPresentationModel.Settings &&
                        paywall.status.unlocks()
                    ) {
                        deliver(
                            PaywallPresentationNotification.Unlocked(
                                premiumStatusPresentationMapper.toPresentation(paywall.status)
                            )
                        )
                    }
                }
            }
        )
    }

    private fun PremiumStatusDomainModel.unlocks() = when (this) {
        PremiumStatusDomainModel.Free, PremiumStatusDomainModel.Pending -> false
        is PremiumStatusDomainModel.Subscribed, is PremiumStatusDomainModel.Lifetime -> true
    }
}
