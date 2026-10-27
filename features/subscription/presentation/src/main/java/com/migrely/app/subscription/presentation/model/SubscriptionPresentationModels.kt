package com.migrely.app.subscription.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification
import java.time.Instant
import java.time.Period

/** Where the paywall was opened from, which picks its headline and where it leads (BRD D109). */
enum class PaywallContextPresentationModel {
    /** Shown itself after the last onboarding step (D110). */
    Onboarding,

    /** Shown itself on home after the 5th migraine: "see your patterns" (D110). */
    Patterns,
    Report,
    Insights,
    Settings
}

enum class PremiumPlanPresentationModel {
    Yearly,
    Monthly,
    Lifetime
}

sealed interface PremiumStatusPresentationModel {
    data object Free : PremiumStatusPresentationModel

    data object Pending : PremiumStatusPresentationModel

    /** [plan] is null when unknown on this phone; [trialEnd] while a known trial runs. */
    data class Subscribed(val plan: PremiumPlanPresentationModel?, val trialEnd: Instant?) :
        PremiumStatusPresentationModel

    data class Lifetime(val subscriptionRenewsToo: Boolean) : PremiumStatusPresentationModel
}

/** A product card: Play's [price], and whether it is the user's [current] plan or [choosable]. */
data class PaywallProductPresentationModel(
    val plan: PremiumPlanPresentationModel,
    val price: String,
    val freeTrial: Period?,
    val offerToken: String?,
    val current: Boolean,
    val choosable: Boolean
)

sealed interface PaywallViewState {
    data object Loading : PaywallViewState

    data class Ready(
        val status: PremiumStatusPresentationModel,
        val products: List<PaywallProductPresentationModel>
    ) : PaywallViewState

    /** Google Play's sheet is open, or purchases are being restored. */
    data object Working : PaywallViewState

    /** Google Play has no billing here (BRD EC-BIL-11). */
    data object Unavailable : PaywallViewState

    /** Google Play did not answer; it can be asked again. */
    data object Unreachable : PaywallViewState
}

sealed interface PaywallPresentationNotification : PresentationNotification {
    /**
     * Premium is on: the paywall shows its success, and how to cancel a subscription that renews.
     */
    data class Unlocked(val status: PremiumStatusPresentationModel) :
        PaywallPresentationNotification

    data object NothingToRestore : PaywallPresentationNotification

    data object PlayUnreachable : PaywallPresentationNotification

    data object SomethingWentWrong : PaywallPresentationNotification
}

/** What "Restore purchases" in Settings found (BRD SUB-F2). */
enum class RestoreNotePresentationModel {
    Restored,
    NothingFound,
    Pending,
    Unreachable,
    Unavailable
}

sealed interface PremiumSectionViewState {
    data object Loading : PremiumSectionViewState

    data class Ready(
        val status: PremiumStatusPresentationModel,
        val note: RestoreNotePresentationModel?
    ) : PremiumSectionViewState

    /** Google Play is being asked again. */
    data object Checking : PremiumSectionViewState
}

/** The Premium section shows no messages of its own: what a restore found is in its state. */
sealed interface PremiumSectionPresentationNotification : PresentationNotification

sealed interface TrialBannerViewState {
    data object Hidden : TrialBannerViewState

    /** [renews] is false once the subscription is cancelled: Premium then ends with the trial. */
    data class Shown(val trialEnd: Instant, val endsToday: Boolean, val renews: Boolean) :
        TrialBannerViewState
}

/** The trial banner shows no messages of its own. */
sealed interface TrialBannerPresentationNotification : PresentationNotification

/** The home trigger and the onboarding gate show nothing but where they lead. */
sealed interface PaywallTriggerPresentationNotification : PresentationNotification
