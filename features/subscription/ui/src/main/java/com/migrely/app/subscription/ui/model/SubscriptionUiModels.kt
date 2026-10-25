package com.migrely.app.subscription.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Where the paywall was opened from (BRD D109); `:app` passes it with the route. */
enum class PaywallContextUiModel {
    Onboarding,
    Patterns,
    Report,
    Insights,
    Settings
}

enum class PremiumPlanUiModel {
    Yearly,
    Monthly,
    Lifetime
}

/** The status, kept across a rotation while the paywall shows its success. */
sealed interface PremiumStatusUiModel : Parcelable {
    @Parcelize
    data object Free : PremiumStatusUiModel

    @Parcelize
    data object Pending : PremiumStatusUiModel

    @Parcelize
    data class Subscribed(val plan: PremiumPlanUiModel?, val trialEndMillis: Long?) :
        PremiumStatusUiModel

    @Parcelize
    data class Lifetime(val subscriptionRenewsToo: Boolean) : PremiumStatusUiModel
}

/** A product card; kept across a rotation, so the cards stay while Google Play's sheet is open. */
@Parcelize
data class PaywallProductUiModel(
    val plan: PremiumPlanUiModel,
    val price: String,
    val freeTrial: Boolean,
    val offerToken: String?,
    val current: Boolean,
    val choosable: Boolean
) : Parcelable

@Parcelize
data class PaywallOffersUiModel(
    val status: PremiumStatusUiModel,
    val products: List<PaywallProductUiModel>
) : Parcelable

sealed interface PaywallViewStateUiModel {
    data object Loading : PaywallViewStateUiModel

    data class Ready(val offers: PaywallOffersUiModel) : PaywallViewStateUiModel

    data object Working : PaywallViewStateUiModel

    data object Unavailable : PaywallViewStateUiModel

    data object Unreachable : PaywallViewStateUiModel
}

enum class PaywallMessageUiModel {
    NothingToRestore,
    PlayUnreachable,
    SomethingWentWrong
}

sealed interface PaywallEventUiModel {
    data class Unlocked(val status: PremiumStatusUiModel) : PaywallEventUiModel

    data class Message(val message: PaywallMessageUiModel) : PaywallEventUiModel
}

enum class RestoreNoteUiModel {
    Restored,
    NothingFound,
    Pending,
    Unreachable,
    Unavailable
}

/** What the Premium section shows; kept across a rotation, so a check keeps the last status. */
@Parcelize
data class PremiumSectionUiModel(val status: PremiumStatusUiModel, val note: RestoreNoteUiModel?) :
    Parcelable

sealed interface PremiumSectionViewStateUiModel {
    data object Loading : PremiumSectionViewStateUiModel

    data class Ready(val section: PremiumSectionUiModel) : PremiumSectionViewStateUiModel

    data object Checking : PremiumSectionViewStateUiModel
}

sealed interface TrialBannerViewStateUiModel {
    data object Hidden : TrialBannerViewStateUiModel

    data class Shown(val trialEndMillis: Long) : TrialBannerViewStateUiModel
}
