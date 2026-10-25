package com.migrely.app.di

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.billing.datasource.BillingDataSource
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSource
import com.migrely.app.navigation.mapper.PaywallGateNavigationEventDestinationMapper
import com.migrely.app.navigation.mapper.PaywallNavigationEventDestinationMapper
import com.migrely.app.navigation.mapper.PaywallTriggerNavigationEventDestinationMapper
import com.migrely.app.navigation.mapper.PremiumSectionNavigationEventDestinationMapper
import com.migrely.app.premium.PurchaseUpdatesWatcher
import com.migrely.app.subscription.data.mapper.SubscriptionDataMappers
import com.migrely.app.subscription.data.repository.SubscriptionRepository
import com.migrely.app.subscription.domain.usecase.BuyPremiumUseCase
import com.migrely.app.subscription.domain.usecase.DismissTrialReminderUseCase
import com.migrely.app.subscription.domain.usecase.EntitlementRules
import com.migrely.app.subscription.domain.usecase.GetPaywallUseCase
import com.migrely.app.subscription.domain.usecase.GetTrialReminderUseCase
import com.migrely.app.subscription.domain.usecase.ManageSubscriptionUseCase
import com.migrely.app.subscription.domain.usecase.MarkPaywallShownUseCase
import com.migrely.app.subscription.domain.usecase.PremiumChecker
import com.migrely.app.subscription.domain.usecase.RefreshPremiumUseCase
import com.migrely.app.subscription.domain.usecase.RestorePurchasesUseCase
import com.migrely.app.subscription.domain.usecase.ShouldShowPaywallUseCase
import com.migrely.app.subscription.domain.usecase.ShowPaymentMessagesUseCase
import com.migrely.app.subscription.domain.usecase.WatchPurchaseUpdatesUseCase
import com.migrely.app.subscription.presentation.mapper.PaywallPresentationMapper
import com.migrely.app.subscription.presentation.mapper.PaywallProductDomainMapper
import com.migrely.app.subscription.presentation.mapper.PaywallTriggerDomainMapper
import com.migrely.app.subscription.presentation.mapper.PremiumPlanPresentationMapper
import com.migrely.app.subscription.presentation.mapper.PremiumStatusPresentationMapper
import com.migrely.app.subscription.presentation.viewmodel.PaywallGateViewModel
import com.migrely.app.subscription.presentation.viewmodel.PaywallTriggerViewModel
import com.migrely.app.subscription.presentation.viewmodel.PaywallUseCases
import com.migrely.app.subscription.presentation.viewmodel.PaywallViewModel
import com.migrely.app.subscription.presentation.viewmodel.PremiumSectionViewModel
import com.migrely.app.subscription.presentation.viewmodel.TrialBannerViewModel
import com.migrely.app.subscription.ui.di.PaywallDependencies
import com.migrely.app.subscription.ui.di.PaywallGateDependencies
import com.migrely.app.subscription.ui.di.PaywallTriggerDependencies
import com.migrely.app.subscription.ui.di.PremiumSectionDependencies
import com.migrely.app.subscription.ui.di.TrialBannerDependencies
import com.migrely.app.subscription.ui.mapper.PaywallContextPresentationMapper
import com.migrely.app.subscription.ui.mapper.PaywallEventPresenter
import com.migrely.app.subscription.ui.mapper.PaywallNotificationUiMapper
import com.migrely.app.subscription.ui.mapper.PaywallProductPresentationMapper
import com.migrely.app.subscription.ui.mapper.PaywallTriggerNotificationUiMapper
import com.migrely.app.subscription.ui.mapper.PaywallViewStateUiMapper
import com.migrely.app.subscription.ui.mapper.PremiumPlanUiMapper
import com.migrely.app.subscription.ui.mapper.PremiumSectionNotificationUiMapper
import com.migrely.app.subscription.ui.mapper.PremiumSectionViewStateUiMapper
import com.migrely.app.subscription.ui.mapper.PremiumStatusUiMapper
import com.migrely.app.subscription.ui.mapper.TrialBannerViewStateUiMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers

/**
 * Migrely Premium (change add-subscription, BRD D105–D113). One repository serves every repository
 * interface; the billing datasource comes from the build type's own module.
 */
@Suppress("TooManyFunctions")
@Module
@InstallIn(SingletonComponent::class)
internal object SubscriptionModule {
    @Provides
    fun providesSubscriptionRepository(
        billingDataSource: BillingDataSource,
        preferencesDataSource: PreferencesDataSource,
        attackDataSource: AttackDataSource,
        clockDataSource: ClockDataSource
    ) = SubscriptionRepository(
        billingDataSource,
        preferencesDataSource,
        attackDataSource,
        clockDataSource,
        SubscriptionDataMappers.create()
    )

    /** One for the app, so checks run one at a time (review M5). */
    @Provides
    @Singleton
    fun providesPremiumChecker(repository: SubscriptionRepository) = PremiumChecker(
        getEntitlementRepository = repository,
        saveEntitlementRepository = repository,
        getOwnedPurchasesRepository = repository,
        acknowledgePurchaseRepository = repository,
        getCurrentTimeRepository = repository,
        entitlementRules = EntitlementRules()
    )

    @Provides
    fun providesRefreshPremiumUseCase(
        premiumChecker: PremiumChecker,
        coroutines: CoroutineContextProvider
    ) = RefreshPremiumUseCase(premiumChecker, coroutines)

    @Provides
    fun providesRestorePurchasesUseCase(
        premiumChecker: PremiumChecker,
        coroutines: CoroutineContextProvider
    ) = RestorePurchasesUseCase(premiumChecker, coroutines)

    @Provides
    fun providesManageSubscriptionUseCase(
        repository: SubscriptionRepository,
        premiumChecker: PremiumChecker,
        coroutines: CoroutineContextProvider
    ) = ManageSubscriptionUseCase(repository, premiumChecker, coroutines)

    @Provides
    fun providesShouldShowPaywallUseCase(
        repository: SubscriptionRepository,
        coroutines: CoroutineContextProvider
    ) = ShouldShowPaywallUseCase(
        getEntitlementRepository = repository,
        getPaywallMarksRepository = repository,
        getAttackInProgressRepository = repository,
        getMigraineCountRepository = repository,
        getBillingAvailabilityRepository = repository,
        getCurrentTimeRepository = repository,
        entitlementRules = EntitlementRules(),
        coroutineContextProvider = coroutines
    )

    @Provides
    fun providesShowPaymentMessagesUseCase(
        repository: SubscriptionRepository,
        premiumChecker: PremiumChecker,
        coroutines: CoroutineContextProvider
    ) = ShowPaymentMessagesUseCase(repository, repository, premiumChecker, coroutines)

    /** Play's purchase changes outside a sheet, checked for as long as the app runs (review I1). */
    @Provides
    @Singleton
    fun providesPurchaseUpdatesWatcher(
        repository: SubscriptionRepository,
        premiumChecker: PremiumChecker,
        coroutines: CoroutineContextProvider
    ): PurchaseUpdatesWatcher {
        val useCase = WatchPurchaseUpdatesUseCase(repository, premiumChecker, coroutines)
        return PurchaseUpdatesWatcher({ useCase.executeInBackground(Unit) }, Dispatchers.IO)
    }

    @Provides
    fun providesPremiumStatusPresentationMapper() =
        PremiumStatusPresentationMapper(PremiumPlanPresentationMapper())

    @Provides
    fun providesPremiumStatusUiMapper() = PremiumStatusUiMapper(PremiumPlanUiMapper())

    /** One for the app, so a purchase going on survives a rotation (ARCHITECTURE section 13). */
    @Provides
    @Singleton
    fun providesPaywallViewModel(
        repository: SubscriptionRepository,
        premiumChecker: PremiumChecker,
        restorePurchasesUseCase: RestorePurchasesUseCase,
        manageSubscriptionUseCase: ManageSubscriptionUseCase,
        premiumStatusPresentationMapper: PremiumStatusPresentationMapper,
        coroutines: CoroutineContextProvider,
        useCaseExecutor: UseCaseExecutor
    ) = PaywallViewModel(
        PaywallUseCases(
            getPaywallUseCase = GetPaywallUseCase(premiumChecker, repository, coroutines),
            buyPremiumUseCase = BuyPremiumUseCase(
                repository,
                repository,
                premiumChecker,
                coroutines
            ),
            restorePurchasesUseCase = restorePurchasesUseCase,
            manageSubscriptionUseCase = manageSubscriptionUseCase,
            markPaywallShownUseCase = MarkPaywallShownUseCase(repository, repository, coroutines)
        ),
        PaywallPresentationMapper(premiumStatusPresentationMapper, PremiumPlanPresentationMapper()),
        premiumStatusPresentationMapper,
        PaywallProductDomainMapper(),
        PaywallTriggerDomainMapper(),
        useCaseExecutor
    )

    @Provides
    @Singleton
    fun providesPaywallDependencies(
        paywallViewModel: PaywallViewModel,
        purchaseUpdatesWatcher: PurchaseUpdatesWatcher,
        premiumStatusUiMapper: PremiumStatusUiMapper
    ): PaywallDependencies {
        // The mapper and the screen share one presenter, so outcomes reach this screen.
        val presenter = PaywallEventPresenter()
        return PaywallDependencies(
            paywallViewModel = paywallViewModel,
            purchaseUpdates = purchaseUpdatesWatcher.updates,
            paywallViewStateUiMapper = PaywallViewStateUiMapper(
                premiumStatusUiMapper,
                PremiumPlanUiMapper()
            ),
            paywallContextPresentationMapper = PaywallContextPresentationMapper(),
            paywallProductPresentationMapper = PaywallProductPresentationMapper(),
            paywallEventPresenter = presenter,
            navigationMapper = PaywallNavigationEventDestinationMapper(),
            notificationMapper = PaywallNotificationUiMapper(presenter, premiumStatusUiMapper)
        )
    }

    @Provides
    fun providesPremiumSectionDependencies(
        refreshPremiumUseCase: RefreshPremiumUseCase,
        restorePurchasesUseCase: RestorePurchasesUseCase,
        manageSubscriptionUseCase: ManageSubscriptionUseCase,
        premiumStatusPresentationMapper: PremiumStatusPresentationMapper,
        premiumStatusUiMapper: PremiumStatusUiMapper,
        purchaseUpdatesWatcher: PurchaseUpdatesWatcher,
        useCaseExecutor: UseCaseExecutor
    ) = PremiumSectionDependencies(
        premiumSectionViewModel = PremiumSectionViewModel(
            refreshPremiumUseCase,
            restorePurchasesUseCase,
            manageSubscriptionUseCase,
            premiumStatusPresentationMapper,
            useCaseExecutor
        ),
        purchaseUpdates = purchaseUpdatesWatcher.updates,
        premiumSectionViewStateUiMapper = PremiumSectionViewStateUiMapper(premiumStatusUiMapper),
        navigationMapper = PremiumSectionNavigationEventDestinationMapper(),
        notificationMapper = PremiumSectionNotificationUiMapper()
    )

    @Provides
    fun providesTrialBannerDependencies(
        repository: SubscriptionRepository,
        manageSubscriptionUseCase: ManageSubscriptionUseCase,
        coroutines: CoroutineContextProvider,
        useCaseExecutor: UseCaseExecutor
    ) = TrialBannerDependencies(
        trialBannerViewModel = TrialBannerViewModel(
            GetTrialReminderUseCase(repository, repository, repository, coroutines),
            DismissTrialReminderUseCase(repository, coroutines),
            manageSubscriptionUseCase,
            useCaseExecutor
        ),
        trialBannerViewStateUiMapper = TrialBannerViewStateUiMapper()
    )

    @Provides
    fun providesPaywallTriggerDependencies(
        shouldShowPaywallUseCase: ShouldShowPaywallUseCase,
        useCaseExecutor: UseCaseExecutor
    ) = PaywallTriggerDependencies(
        paywallTriggerViewModel = PaywallTriggerViewModel(
            shouldShowPaywallUseCase,
            useCaseExecutor
        ),
        navigationMapper = PaywallTriggerNavigationEventDestinationMapper(),
        notificationMapper = PaywallTriggerNotificationUiMapper()
    )

    @Provides
    fun providesPaywallGateDependencies(
        shouldShowPaywallUseCase: ShouldShowPaywallUseCase,
        useCaseExecutor: UseCaseExecutor
    ) = PaywallGateDependencies(
        paywallGateViewModel = PaywallGateViewModel(shouldShowPaywallUseCase, useCaseExecutor),
        navigationMapper = PaywallGateNavigationEventDestinationMapper(),
        notificationMapper = PaywallTriggerNotificationUiMapper()
    )
}
