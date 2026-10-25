package com.migrely.app.subscription.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.subscription.domain.model.BillingAvailabilityDomainModel
import com.migrely.app.subscription.domain.model.PremiumCheckDomainModel
import com.migrely.app.subscription.domain.model.PremiumStatusDomainModel
import com.migrely.app.subscription.domain.model.RefreshPremiumRequestDomainModel
import com.migrely.app.subscription.domain.usecase.ManageSubscriptionUseCase
import com.migrely.app.subscription.domain.usecase.RefreshPremiumUseCase
import com.migrely.app.subscription.domain.usecase.RestorePurchasesUseCase
import com.migrely.app.subscription.presentation.mapper.PremiumStatusPresentationMapper
import com.migrely.app.subscription.presentation.model.PremiumSectionPresentationNotification
import com.migrely.app.subscription.presentation.model.PremiumSectionViewState
import com.migrely.app.subscription.presentation.model.RestoreNotePresentationModel
import com.migrely.app.subscription.presentation.navigation.PremiumSectionPresentationNavigationEvent

/**
 * Settings' Premium section (BRD SUB-F2, D112): the status, with the trial's end when known,
 * "Restore purchases" and "Manage subscription". It reads the status on every enter and resume,
 * asking Google Play when the last answer is old, so a purchase made elsewhere shows.
 */
class PremiumSectionViewModel(
    private val refreshPremiumUseCase: RefreshPremiumUseCase,
    private val restorePurchasesUseCase: RestorePurchasesUseCase,
    private val manageSubscriptionUseCase: ManageSubscriptionUseCase,
    private val premiumStatusPresentationMapper: PremiumStatusPresentationMapper,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<PremiumSectionViewState, PremiumSectionPresentationNotification>(
    useCaseExecutor
) {
    fun onEnter() {
        refreshPremiumUseCase(
            value = RefreshPremiumRequestDomainModel(force = false),
            onResult = { status -> show(status, note = null) }
        )
    }

    /** The status row of a user who can buy or change plans opens the paywall. */
    fun onStatusAction() =
        emitNavigationEvent(PremiumSectionPresentationNavigationEvent.OnOpenPaywall)

    fun onRestoreAction() {
        updateViewState(PremiumSectionViewState.Checking)
        restorePurchasesUseCase(onResult = { check -> show(check.status, noteOf(check)) })
    }

    fun onManageAction() {
        manageSubscriptionUseCase(onResult = { status -> show(status, note = null) })
    }

    private fun noteOf(check: PremiumCheckDomainModel): RestoreNotePresentationModel =
        when (check.billing) {
            BillingAvailabilityDomainModel.Unavailable -> RestoreNotePresentationModel.Unavailable
            BillingAvailabilityDomainModel.Unknown -> RestoreNotePresentationModel.Unreachable
            BillingAvailabilityDomainModel.Available -> when (check.status) {
                PremiumStatusDomainModel.Free -> RestoreNotePresentationModel.NothingFound
                PremiumStatusDomainModel.Pending -> RestoreNotePresentationModel.Pending
                is PremiumStatusDomainModel.Subscribed,
                is PremiumStatusDomainModel.Lifetime -> RestoreNotePresentationModel.Restored
            }
        }

    private fun show(status: PremiumStatusDomainModel, note: RestoreNotePresentationModel?) =
        updateViewState(
            PremiumSectionViewState.Ready(
                premiumStatusPresentationMapper.toPresentation(status),
                note
            )
        )
}
