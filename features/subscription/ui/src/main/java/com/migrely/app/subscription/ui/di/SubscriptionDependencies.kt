package com.migrely.app.subscription.ui.di

import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.view.BaseComposeHolder
import com.migrely.app.subscription.presentation.model.PaywallPresentationNotification
import com.migrely.app.subscription.presentation.model.PaywallTriggerPresentationNotification
import com.migrely.app.subscription.presentation.model.PaywallViewState
import com.migrely.app.subscription.presentation.model.PremiumSectionPresentationNotification
import com.migrely.app.subscription.presentation.model.PremiumSectionViewState
import com.migrely.app.subscription.presentation.navigation.PaywallGatePresentationNavigationEvent
import com.migrely.app.subscription.presentation.navigation.PaywallPresentationNavigationEvent
import com.migrely.app.subscription.presentation.navigation.PaywallTriggerPresentationNavigationEvent
import com.migrely.app.subscription.presentation.navigation.PremiumSectionPresentationNavigationEvent
import com.migrely.app.subscription.presentation.viewmodel.PaywallGateViewModel
import com.migrely.app.subscription.presentation.viewmodel.PaywallTriggerViewModel
import com.migrely.app.subscription.presentation.viewmodel.PaywallViewModel
import com.migrely.app.subscription.presentation.viewmodel.PremiumSectionViewModel
import com.migrely.app.subscription.presentation.viewmodel.TrialBannerViewModel
import com.migrely.app.subscription.ui.mapper.PaywallContextPresentationMapper
import com.migrely.app.subscription.ui.mapper.PaywallEventPresenter
import com.migrely.app.subscription.ui.mapper.PaywallProductPresentationMapper
import com.migrely.app.subscription.ui.mapper.PaywallViewStateUiMapper
import com.migrely.app.subscription.ui.mapper.PremiumSectionViewStateUiMapper
import com.migrely.app.subscription.ui.mapper.TrialBannerViewStateUiMapper

/** The paywall screen (BRD D109). */
class PaywallDependencies(
    val paywallViewModel: PaywallViewModel,
    val paywallViewStateUiMapper: PaywallViewStateUiMapper,
    val paywallContextPresentationMapper: PaywallContextPresentationMapper,
    val paywallProductPresentationMapper: PaywallProductPresentationMapper,
    val paywallEventPresenter: PaywallEventPresenter,
    navigationMapper: NavigationEventDestinationMapper<PaywallPresentationNavigationEvent>,
    notificationMapper: NotificationUiMapper<PaywallPresentationNotification>
) : BaseComposeHolder<PaywallViewState, PaywallPresentationNotification>(
    paywallViewModel,
    navigationMapper,
    notificationMapper
)

/** The Premium section that `:app` puts at the top of Settings (BRD D112, design D3). */
class PremiumSectionDependencies(
    val premiumSectionViewModel: PremiumSectionViewModel,
    val premiumSectionViewStateUiMapper: PremiumSectionViewStateUiMapper,
    navigationMapper: NavigationEventDestinationMapper<PremiumSectionPresentationNavigationEvent>,
    notificationMapper: NotificationUiMapper<PremiumSectionPresentationNotification>
) : BaseComposeHolder<PremiumSectionViewState, PremiumSectionPresentationNotification>(
    premiumSectionViewModel,
    navigationMapper,
    notificationMapper
)

/**
 * The trial reminder that `:app` puts on home (BRD D111). It neither navigates nor shows
 * messages, so it has no observer.
 */
class TrialBannerDependencies(
    val trialBannerViewModel: TrialBannerViewModel,
    val trialBannerViewStateUiMapper: TrialBannerViewStateUiMapper
)

/** The paywall after the 5th migraine, which `:app` puts on home (BRD D110). */
class PaywallTriggerDependencies(
    val paywallTriggerViewModel: PaywallTriggerViewModel,
    navigationMapper: NavigationEventDestinationMapper<PaywallTriggerPresentationNavigationEvent>,
    notificationMapper: NotificationUiMapper<PaywallTriggerPresentationNotification>
) : BaseComposeHolder<Unit, PaywallTriggerPresentationNotification>(
    paywallTriggerViewModel,
    navigationMapper,
    notificationMapper
)

/** The step between onboarding and home (design D4). */
class PaywallGateDependencies(
    val paywallGateViewModel: PaywallGateViewModel,
    navigationMapper: NavigationEventDestinationMapper<PaywallGatePresentationNavigationEvent>,
    notificationMapper: NotificationUiMapper<PaywallTriggerPresentationNotification>
) : BaseComposeHolder<Unit, PaywallTriggerPresentationNotification>(
    paywallGateViewModel,
    navigationMapper,
    notificationMapper
)
