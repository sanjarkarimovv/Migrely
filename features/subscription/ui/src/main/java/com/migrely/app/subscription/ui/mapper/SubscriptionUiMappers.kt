package com.migrely.app.subscription.ui.mapper

import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.notification.model.UiNotification
import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.subscription.presentation.model.PaywallContextPresentationModel
import com.migrely.app.subscription.presentation.model.PaywallPresentationNotification
import com.migrely.app.subscription.presentation.model.PaywallProductPresentationModel
import com.migrely.app.subscription.presentation.model.PaywallViewState
import com.migrely.app.subscription.presentation.model.PremiumPlanPresentationModel
import com.migrely.app.subscription.presentation.model.PremiumSectionPresentationNotification
import com.migrely.app.subscription.presentation.model.PremiumSectionViewState
import com.migrely.app.subscription.presentation.model.PremiumStatusPresentationModel
import com.migrely.app.subscription.presentation.model.RestoreNotePresentationModel
import com.migrely.app.subscription.presentation.model.TrialBannerViewState
import com.migrely.app.subscription.ui.model.PaywallContextUiModel
import com.migrely.app.subscription.ui.model.PaywallEventUiModel
import com.migrely.app.subscription.ui.model.PaywallMessageBehaviourUiModel
import com.migrely.app.subscription.ui.model.PaywallMessageUiModel
import com.migrely.app.subscription.ui.model.PaywallOffersUiModel
import com.migrely.app.subscription.ui.model.PaywallProductUiModel
import com.migrely.app.subscription.ui.model.PaywallViewStateUiModel
import com.migrely.app.subscription.ui.model.PremiumPlanUiModel
import com.migrely.app.subscription.ui.model.PremiumSectionUiModel
import com.migrely.app.subscription.ui.model.PremiumSectionViewStateUiModel
import com.migrely.app.subscription.ui.model.PremiumStatusUiModel
import com.migrely.app.subscription.ui.model.RestoreNoteUiModel
import com.migrely.app.subscription.ui.model.TrialBannerViewStateUiModel
import com.migrely.app.subscription.ui.model.TrialLengthUiModel
import java.time.Period
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class PremiumPlanUiMapper {
    fun toUi(plan: PremiumPlanPresentationModel): PremiumPlanUiModel = when (plan) {
        PremiumPlanPresentationModel.Yearly -> PremiumPlanUiModel.Yearly
        PremiumPlanPresentationModel.Monthly -> PremiumPlanUiModel.Monthly
        PremiumPlanPresentationModel.Lifetime -> PremiumPlanUiModel.Lifetime
    }
}

class PremiumStatusUiMapper(private val premiumPlanUiMapper: PremiumPlanUiMapper) {
    fun toUi(status: PremiumStatusPresentationModel): PremiumStatusUiModel = when (status) {
        PremiumStatusPresentationModel.Free -> PremiumStatusUiModel.Free
        PremiumStatusPresentationModel.Pending -> PremiumStatusUiModel.Pending
        is PremiumStatusPresentationModel.Subscribed -> PremiumStatusUiModel.Subscribed(
            status.plan?.let(premiumPlanUiMapper::toUi),
            status.trialEnd?.toEpochMilli()
        )
        is PremiumStatusPresentationModel.Lifetime ->
            PremiumStatusUiModel.Lifetime(status.subscriptionRenewsToo)
    }
}

class PaywallViewStateUiMapper(
    private val premiumStatusUiMapper: PremiumStatusUiMapper,
    private val premiumPlanUiMapper: PremiumPlanUiMapper
) {
    fun toUi(viewState: PaywallViewState): PaywallViewStateUiModel = when (viewState) {
        PaywallViewState.Loading -> PaywallViewStateUiModel.Loading
        is PaywallViewState.Ready -> PaywallViewStateUiModel.Ready(
            PaywallOffersUiModel(
                premiumStatusUiMapper.toUi(viewState.status),
                viewState.products.map(::product)
            )
        )
        PaywallViewState.Working -> PaywallViewStateUiModel.Working
        PaywallViewState.Unavailable -> PaywallViewStateUiModel.Unavailable
        PaywallViewState.Unreachable -> PaywallViewStateUiModel.Unreachable
    }

    private fun product(product: PaywallProductPresentationModel) = PaywallProductUiModel(
        plan = premiumPlanUiMapper.toUi(product.plan),
        price = product.price,
        trial = product.freeTrial?.let {
            TrialLengthUiModel(months = it.toTotalMonths().toInt(), days = it.days)
        },
        offerToken = product.offerToken,
        current = product.current,
        choosable = product.choosable
    )
}

/** The card chosen, for the view model to buy. */
class PaywallProductPresentationMapper {
    fun toPresentation(product: PaywallProductUiModel) = PaywallProductPresentationModel(
        plan = when (product.plan) {
            PremiumPlanUiModel.Yearly -> PremiumPlanPresentationModel.Yearly
            PremiumPlanUiModel.Monthly -> PremiumPlanPresentationModel.Monthly
            PremiumPlanUiModel.Lifetime -> PremiumPlanPresentationModel.Lifetime
        },
        price = product.price,
        freeTrial = product.trial?.let { Period.of(0, it.months, it.days) },
        offerToken = product.offerToken,
        current = product.current,
        choosable = product.choosable
    )
}

class PaywallContextPresentationMapper {
    fun toPresentation(context: PaywallContextUiModel): PaywallContextPresentationModel =
        when (context) {
            PaywallContextUiModel.Onboarding -> PaywallContextPresentationModel.Onboarding
            PaywallContextUiModel.Patterns -> PaywallContextPresentationModel.Patterns
            PaywallContextUiModel.Report -> PaywallContextPresentationModel.Report
            PaywallContextUiModel.Insights -> PaywallContextPresentationModel.Insights
            PaywallContextUiModel.Settings -> PaywallContextPresentationModel.Settings
        }
}

/**
 * Tone and timing of each paywall message (BRD EC-UX-16; QA #56): 8 s each, stretched to the
 * accessibility timeout. Finding no purchase is information; the others are errors.
 */
class PaywallMessageBehaviourUiMapper {
    fun toUi(message: PaywallMessageUiModel): PaywallMessageBehaviourUiModel = when (message) {
        PaywallMessageUiModel.NothingToRestore ->
            PaywallMessageBehaviourUiModel(SnackbarTone.Information, READING_MILLIS)

        PaywallMessageUiModel.PlayUnreachable,
        PaywallMessageUiModel.SomethingWentWrong ->
            PaywallMessageBehaviourUiModel(SnackbarTone.Error, READING_MILLIS)
    }

    private companion object {
        const val READING_MILLIS = 8_000L
    }
}

/** Carries the paywall's outcomes to the screen, which takes them only while it is shown. */
class PaywallEventPresenter {
    private val _events = MutableSharedFlow<PaywallEventUiModel>(extraBufferCapacity = EVENT_BUFFER)
    val events: SharedFlow<PaywallEventUiModel> = _events.asSharedFlow()

    fun show(event: PaywallEventUiModel) {
        _events.tryEmit(event)
    }

    private companion object {
        const val EVENT_BUFFER = 8
    }
}

class PaywallNotificationUiMapper(
    private val presenter: PaywallEventPresenter,
    private val premiumStatusUiMapper: PremiumStatusUiMapper
) : NotificationUiMapper<PaywallPresentationNotification> {
    override fun toUi(notification: PaywallPresentationNotification): UiNotification {
        val event = when (notification) {
            is PaywallPresentationNotification.Unlocked ->
                PaywallEventUiModel.Unlocked(premiumStatusUiMapper.toUi(notification.status))
            PaywallPresentationNotification.NothingToRestore ->
                PaywallEventUiModel.Message(PaywallMessageUiModel.NothingToRestore)
            PaywallPresentationNotification.PlayUnreachable ->
                PaywallEventUiModel.Message(PaywallMessageUiModel.PlayUnreachable)
            PaywallPresentationNotification.SomethingWentWrong ->
                PaywallEventUiModel.Message(PaywallMessageUiModel.SomethingWentWrong)
        }
        return UiNotification { presenter.show(event) }
    }
}

class PremiumSectionViewStateUiMapper(private val premiumStatusUiMapper: PremiumStatusUiMapper) {
    fun toUi(viewState: PremiumSectionViewState): PremiumSectionViewStateUiModel =
        when (viewState) {
            PremiumSectionViewState.Loading -> PremiumSectionViewStateUiModel.Loading
            is PremiumSectionViewState.Ready -> PremiumSectionViewStateUiModel.Ready(
                PremiumSectionUiModel(
                    premiumStatusUiMapper.toUi(viewState.status),
                    viewState.note?.let(::note)
                )
            )
            PremiumSectionViewState.Checking -> PremiumSectionViewStateUiModel.Checking
        }

    private fun note(note: RestoreNotePresentationModel): RestoreNoteUiModel = when (note) {
        RestoreNotePresentationModel.Restored -> RestoreNoteUiModel.Restored
        RestoreNotePresentationModel.NothingFound -> RestoreNoteUiModel.NothingFound
        RestoreNotePresentationModel.Pending -> RestoreNoteUiModel.Pending
        RestoreNotePresentationModel.Unreachable -> RestoreNoteUiModel.Unreachable
        RestoreNotePresentationModel.Unavailable -> RestoreNoteUiModel.Unavailable
    }
}

/** The Premium section has no notifications, so this is never asked. */
class PremiumSectionNotificationUiMapper :
    NotificationUiMapper<PremiumSectionPresentationNotification> {
    override fun toUi(notification: PremiumSectionPresentationNotification): UiNotification =
        UiNotification { }
}

class TrialBannerViewStateUiMapper {
    fun toUi(viewState: TrialBannerViewState): TrialBannerViewStateUiModel = when (viewState) {
        TrialBannerViewState.Hidden -> TrialBannerViewStateUiModel.Hidden
        is TrialBannerViewState.Shown -> TrialBannerViewStateUiModel.Shown(
            trialEndMillis = viewState.trialEnd.toEpochMilli(),
            endsToday = viewState.endsToday,
            renews = viewState.renews
        )
    }
}
