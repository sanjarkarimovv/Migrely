package com.migrely.app.subscription.ui.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.migrely.app.architecture.ui.view.ScreenEnterObserver
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.motion.rememberReduceMotion
import com.migrely.app.designsystem.snackbar.MigrelySnackbar
import com.migrely.app.designsystem.snackbar.MigrelySnackbarHost
import com.migrely.app.designsystem.snackbar.messageTimeoutMillis
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.subscription.presentation.model.PaywallViewState
import com.migrely.app.subscription.ui.R
import com.migrely.app.subscription.ui.di.PaywallDependencies
import com.migrely.app.subscription.ui.model.PaywallContextUiModel
import com.migrely.app.subscription.ui.model.PaywallEventUiModel
import com.migrely.app.subscription.ui.model.PaywallMessageUiModel
import com.migrely.app.subscription.ui.model.PaywallOffersUiModel
import com.migrely.app.subscription.ui.model.PaywallProductUiModel
import com.migrely.app.subscription.ui.model.PaywallViewStateUiModel
import com.migrely.app.subscription.ui.model.PremiumPlanUiModel
import com.migrely.app.subscription.ui.model.PremiumStatusUiModel
import com.migrely.app.subscription.ui.model.TrialLengthUiModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onSubscription

/** Only for a trial Google Play gives in months and days at once, which it does not today. */
private const val DAYS_PER_MONTH = 30
private val CONTENT_MAX_WIDTH = 600.dp
private val CARD_SHAPE = RoundedCornerShape(16.dp)

/** Callbacks from the paywall. */
data class PaywallActions(
    val onClose: () -> Unit,
    val onChoose: (PremiumPlanUiModel) -> Unit,
    val onBuy: (PaywallProductUiModel) -> Unit,
    val onRestore: () -> Unit,
    val onRetry: () -> Unit,
    val onManage: () -> Unit,
    val onContinue: () -> Unit
)

/** A message as shown, with its place in the order shown, so the same one twice shows again. */
private data class ShownMessage(val message: PaywallMessageUiModel, val number: Int)

/**
 * The paywall (BRD §7.3, SUB-F1, D109): a headline for [context], what Premium adds, the three
 * products at Google Play's prices with yearly chosen first, a button that names the action, the
 * terms, "Restore purchases", and a close button from the first frame. Back closes it as the close
 * button does. Its outcomes arrive only while it is shown, and one it missed is asked for again.
 */
@Composable
fun PaywallDependencies.Paywall(
    navController: NavController,
    context: PaywallContextUiModel,
    modifier: Modifier = Modifier
) {
    val presentationContext = paywallContextPresentationMapper.toPresentation(context)
    ScreenEnterObserver { paywallViewModel.onEnter(presentationContext) }
    ViewModelObserver(navController)
    val viewState by paywallViewModel.viewState.collectAsState(PaywallViewState.Loading)
    var chosen by rememberSaveable { mutableStateOf<PremiumPlanUiModel?>(null) }
    // Premium is on: the success shows from here on, a rotation included.
    var unlocked by rememberSaveable { mutableStateOf<PremiumStatusUiModel?>(null) }
    var message by remember { mutableStateOf<ShownMessage?>(null) }
    var shownCount by remember { mutableIntStateOf(0) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(paywallEventPresenter, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            paywallEventPresenter.events
                .onSubscription { paywallViewModel.onResume(presentationContext) }
                .collect { event ->
                    paywallViewModel.onOutcomeShownAction()
                    when (event) {
                        is PaywallEventUiModel.Unlocked -> unlocked = event.status
                        is PaywallEventUiModel.Message -> {
                            shownCount++
                            message = ShownMessage(event.message, shownCount)
                        }
                    }
                }
        }
    }
    // A pending payment that completes while the paywall shows unlocks it here (review I1).
    LaunchedEffect(purchaseUpdates) {
        purchaseUpdates.collect { paywallViewModel.onPurchaseUpdatedAction(presentationContext) }
    }
    // Stretched to the accessibility timeout (BRD EC-UX-16; QA #56).
    val messageTimeout = messageTimeoutMillis(
        message?.let { paywallMessageBehaviourUiMapper.toUi(it.message).baseDurationMillis }
    )
    LaunchedEffect(message, messageTimeout) {
        if (message != null && messageTimeout != null) {
            delay(messageTimeout)
            message = null
        }
    }
    val onClose = { paywallViewModel.onCloseAction(presentationContext) }
    BackHandler(onBack = onClose)
    PaywallContents(
        context = context,
        viewState = paywallViewStateUiMapper.toUi(viewState),
        chosen = chosen,
        unlocked = unlocked,
        actions = PaywallActions(
            onClose = onClose,
            onChoose = { chosen = it },
            onBuy = { product ->
                paywallViewModel.onBuyAction(
                    presentationContext,
                    paywallProductPresentationMapper.toPresentation(product)
                )
            },
            onRestore = { paywallViewModel.onRestoreAction(presentationContext) },
            onRetry = { paywallViewModel.onRetryAction(presentationContext) },
            onManage = paywallViewModel::onManageAction,
            onContinue = { paywallViewModel.onContinueAction(presentationContext) }
        ),
        messages = {
            MigrelySnackbarHost(message = message, reduceMotion = rememberReduceMotion()) { shown ->
                MigrelySnackbar(
                    text = stringResource(shown.message.text()),
                    tone = paywallMessageBehaviourUiMapper.toUi(shown.message).tone,
                    action = null,
                    dismissLabel = stringResource(R.string.subscription_message_dismiss),
                    onDismiss = { message = null }
                )
            }
        },
        modifier = modifier
    )
}

/** The paywall itself, without its view model. */
@Suppress("LongParameterList")
@Composable
fun PaywallContents(
    context: PaywallContextUiModel,
    viewState: PaywallViewStateUiModel,
    chosen: PremiumPlanUiModel?,
    unlocked: PremiumStatusUiModel?,
    actions: PaywallActions,
    messages: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize()) {
            // The close button is there from the first frame, whatever is loading (BRD §7.3).
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                        )
                    )
                    .padding(4.dp)
            ) {
                IconButton(onClick = actions.onClose) {
                    Icon(
                        MigrelyIcons.Close,
                        contentDescription = stringResource(R.string.subscription_close)
                    )
                }
            }
            if (viewState == PaywallViewStateUiModel.Working && unlocked == null) {
                val description = stringResource(R.string.subscription_working)
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().semantics {
                        contentDescription = description
                        liveRegion = LiveRegionMode.Polite
                    }
                )
            }
            Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .widthIn(max = CONTENT_MAX_WIDTH)
                        .fillMaxWidth()
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                            )
                        )
                        .verticalScroll(rememberScrollState())
                        .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
                ) {
                    if (unlocked != null) {
                        PaywallSuccess(unlocked, actions)
                    } else {
                        PaywallOffer(context, viewState, chosen, actions)
                    }
                }
            }
        }
        messages()
    }
}

@Composable
private fun PaywallOffer(
    context: PaywallContextUiModel,
    viewState: PaywallViewStateUiModel,
    chosen: PremiumPlanUiModel?,
    actions: PaywallActions
) {
    Headline(context)
    // The cards stay while Google Play's sheet is open, so the screen does not jump.
    var shownOffers by rememberSaveable { mutableStateOf<PaywallOffersUiModel?>(null) }
    when (viewState) {
        is PaywallViewStateUiModel.Ready -> shownOffers = viewState.offers
        PaywallViewStateUiModel.Unavailable, PaywallViewStateUiModel.Unreachable ->
            shownOffers =
                null
        PaywallViewStateUiModel.Loading, PaywallViewStateUiModel.Working -> Unit
    }
    val working = viewState == PaywallViewStateUiModel.Working
    val offers = shownOffers
    when {
        viewState == PaywallViewStateUiModel.Unavailable -> Notice(
            title = stringResource(R.string.subscription_unavailable),
            text = stringResource(R.string.subscription_unavailable_text)
        )

        viewState == PaywallViewStateUiModel.Unreachable -> {
            Notice(
                title = stringResource(R.string.subscription_unreachable),
                text = stringResource(R.string.subscription_unreachable_text)
            )
            OutlinedButton(
                onClick = actions.onRetry,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) {
                Text(stringResource(R.string.subscription_try_again))
            }
        }

        offers == null -> Text(
            stringResource(R.string.subscription_loading),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
        )

        else -> Plans(offers, chosen, working, actions)
    }
    Benefits()
    TextButton(
        onClick = actions.onRestore,
        enabled = !working && viewState != PaywallViewStateUiModel.Unavailable,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
    ) {
        Text(stringResource(R.string.subscription_restore))
    }
}

@Composable
private fun Headline(context: PaywallContextUiModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(context.headline()),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            stringResource(context.lead()),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Benefits() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.subscription_benefits_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() }
        )
        listOf(
            R.string.subscription_benefit_report,
            R.string.subscription_benefit_triggers,
            R.string.subscription_benefit_timing,
            R.string.subscription_benefit_trends
        ).forEach { benefit ->
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    MigrelyIcons.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 12.dp).size(20.dp)
                )
                Text(stringResource(benefit), style = MaterialTheme.typography.bodyLarge)
            }
        }
        Text(
            stringResource(R.string.subscription_free_stays),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** The cards, the button that names the action, and the terms under it (design D7). */
@Composable
private fun Plans(
    offers: PaywallOffersUiModel,
    chosen: PremiumPlanUiModel?,
    working: Boolean,
    actions: PaywallActions
) {
    if (offers.status == PremiumStatusUiModel.Pending) {
        Notice(
            title = stringResource(R.string.subscription_pending_title),
            text = stringResource(R.string.subscription_pending_text)
        )
    }
    // A subscriber sees the plan they have and what they can move to.
    val shown = offers.products.filter { it.current || it.choosable }
    val choice = shown.chosen(chosen)
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.selectableGroup()
    ) {
        shown.forEach { product ->
            ProductCard(
                product = product,
                selected = product == choice,
                enabled = !working,
                onSelect = { actions.onChoose(product.plan) }
            )
        }
    }
    if (choice != null) {
        Button(
            onClick = { actions.onBuy(choice) },
            enabled = !working,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
        ) {
            Text(stringResource(choice.buyLabel()))
        }
        Text(
            terms(choice),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    // A subscription is changed or cancelled in Google Play, and is the only way when its plan is
    // unknown on this phone (review M9).
    if (offers.status is PremiumStatusUiModel.Subscribed) {
        OutlinedButton(
            onClick = actions.onManage,
            enabled = !working,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        ) {
            Text(stringResource(R.string.subscription_manage))
        }
    }
}

/** The card chosen: the one tapped, else yearly, else the first that can be bought (BRD D109). */
private fun List<PaywallProductUiModel>.chosen(plan: PremiumPlanUiModel?) =
    firstOrNull { it.plan == plan && it.choosable }
        ?: firstOrNull { it.plan == PremiumPlanUiModel.Yearly && it.choosable }
        ?: firstOrNull { it.choosable }

/** One node per card, read in full, with its selected state (BRD EC-UX-04). */
@Composable
private fun ProductCard(
    product: PaywallProductUiModel,
    selected: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit
) {
    val name = stringResource(product.plan.label())
    val price = price(product)
    val description = stringResource(
        if (product.current) {
            R.string.subscription_card_description_current
        } else {
            R.string.subscription_card_description
        },
        name,
        price
    )
    val outline = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    val canSelect = enabled && product.choosable
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(CARD_SHAPE)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(if (selected) 2.dp else 1.dp, outline, CARD_SHAPE)
            .selectable(
                selected = selected,
                enabled = canSelect,
                role = Role.RadioButton,
                onClick = onSelect
            )
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.RadioButton
                this.selected = selected
                if (canSelect) {
                    onClick {
                        onSelect()
                        true
                    }
                }
            }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleMedium)
            Text(
                price,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (product.current) {
                Text(
                    stringResource(R.string.subscription_your_plan),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        if (product.choosable) {
            RadioButton(
                selected = selected,
                onClick = null,
                enabled = enabled,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

/** Premium is on; for lifetime over a subscription, how to stop the subscription (EC-BIL-09). */
@Composable
private fun PaywallSuccess(status: PremiumStatusUiModel, actions: PaywallActions) {
    val trialEnd = (status as? PremiumStatusUiModel.Subscribed)?.trialEndMillis
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.semantics(mergeDescendants = true) {
            liveRegion = LiveRegionMode.Polite
        }
    ) {
        Icon(
            MigrelyIcons.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(40.dp)
        )
        Text(
            stringResource(
                if (trialEnd != null) {
                    R.string.subscription_success_trial_title
                } else {
                    R.string.subscription_success_title
                }
            ),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            when {
                trialEnd != null ->
                    stringResource(R.string.subscription_success_trial_text, longDate(trialEnd))
                status is PremiumStatusUiModel.Lifetime ->
                    stringResource(R.string.subscription_success_lifetime_text)
                else -> stringResource(R.string.subscription_success_text)
            },
            style = MaterialTheme.typography.bodyLarge
        )
    }
    if ((status as? PremiumStatusUiModel.Lifetime)?.subscriptionRenewsToo == true) {
        Notice(title = null, text = stringResource(R.string.subscription_cancel_subscription))
        OutlinedButton(
            onClick = actions.onManage,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        ) {
            Text(stringResource(R.string.subscription_manage))
        }
    }
    Button(
        onClick = actions.onContinue,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
    ) {
        Text(stringResource(R.string.subscription_continue))
    }
}

/** A calm note in a card, read as one node. */
@Composable
internal fun Notice(title: String?, text: String, modifier: Modifier = Modifier) {
    Surface(
        shape = CARD_SHAPE,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            if (title != null) Text(title, style = MaterialTheme.typography.titleMedium)
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun price(product: PaywallProductUiModel): String = when (product.plan) {
    PremiumPlanUiModel.Yearly -> product.trial?.let { trial ->
        // As long as Google Play's trial lasts, never a length of the app's own (review M8).
        if (trial.months > 0 && trial.days == 0) {
            pluralStringResource(
                R.plurals.subscription_price_yearly_trial_months,
                trial.months,
                trial.months,
                product.price
            )
        } else {
            val days = trial.days + trial.months * DAYS_PER_MONTH
            pluralStringResource(
                R.plurals.subscription_price_yearly_trial_days,
                days,
                days,
                product.price
            )
        }
    } ?: stringResource(R.string.subscription_price_yearly, product.price)
    PremiumPlanUiModel.Monthly -> stringResource(R.string.subscription_price_monthly, product.price)
    PremiumPlanUiModel.Lifetime -> stringResource(
        R.string.subscription_price_lifetime,
        product.price
    )
}

@Composable
private fun terms(product: PaywallProductUiModel): String = when (product.plan) {
    PremiumPlanUiModel.Yearly, PremiumPlanUiModel.Monthly -> {
        val renews = stringResource(R.string.subscription_terms_renews)
        if (product.trial != null) {
            renews + " " + stringResource(R.string.subscription_terms_trial)
        } else {
            renews
        }
    }
    PremiumPlanUiModel.Lifetime -> stringResource(R.string.subscription_terms_once)
}

private fun PaywallProductUiModel.buyLabel() = when {
    trial != null -> R.string.subscription_start_trial
    plan == PremiumPlanUiModel.Lifetime -> R.string.subscription_buy_once
    else -> R.string.subscription_subscribe
}

internal fun PremiumPlanUiModel.label() = when (this) {
    PremiumPlanUiModel.Yearly -> R.string.subscription_plan_yearly
    PremiumPlanUiModel.Monthly -> R.string.subscription_plan_monthly
    PremiumPlanUiModel.Lifetime -> R.string.subscription_plan_lifetime
}

private fun PaywallContextUiModel.headline() = when (this) {
    PaywallContextUiModel.Onboarding -> R.string.subscription_headline_onboarding
    PaywallContextUiModel.Patterns -> R.string.subscription_headline_patterns
    PaywallContextUiModel.Report -> R.string.subscription_headline_report
    PaywallContextUiModel.Insights -> R.string.subscription_headline_insights
    PaywallContextUiModel.Settings -> R.string.subscription_headline_settings
}

private fun PaywallContextUiModel.lead() = when (this) {
    PaywallContextUiModel.Onboarding -> R.string.subscription_lead_onboarding
    PaywallContextUiModel.Patterns -> R.string.subscription_lead_patterns
    PaywallContextUiModel.Report -> R.string.subscription_lead_report
    PaywallContextUiModel.Insights -> R.string.subscription_lead_insights
    PaywallContextUiModel.Settings -> R.string.subscription_lead_settings
}

private fun PaywallMessageUiModel.text() = when (this) {
    PaywallMessageUiModel.NothingToRestore -> R.string.subscription_message_nothing_to_restore
    PaywallMessageUiModel.PlayUnreachable -> R.string.subscription_message_unreachable
    PaywallMessageUiModel.SomethingWentWrong -> R.string.subscription_message_failed
}

private val PREVIEW_OFFERS = PaywallOffersUiModel(
    status = PremiumStatusUiModel.Free,
    products = listOf(
        PaywallProductUiModel(
            PremiumPlanUiModel.Yearly,
            "$29.99",
            TrialLengthUiModel(months = 0, days = 7),
            null,
            false,
            true
        ),
        PaywallProductUiModel(PremiumPlanUiModel.Monthly, "$4.99", null, null, false, true),
        PaywallProductUiModel(PremiumPlanUiModel.Lifetime, "$59.99", null, null, false, true)
    )
)
private val PREVIEW_ACTIONS = PaywallActions({}, {}, {}, {}, {}, {}, {})

@Preview(name = "Paywall, doctor report")
@Composable
private fun PaywallPreview() {
    MigrelyTheme {
        PaywallContents(
            context = PaywallContextUiModel.Report,
            viewState = PaywallViewStateUiModel.Ready(PREVIEW_OFFERS),
            chosen = null,
            unlocked = null,
            actions = PREVIEW_ACTIONS,
            messages = {}
        )
    }
}

@Preview(name = "Paywall, light, pending")
@Composable
private fun PaywallPendingLightPreview() {
    MigrelyTheme(dark = false) {
        PaywallContents(
            context = PaywallContextUiModel.Patterns,
            viewState = PaywallViewStateUiModel.Ready(
                PREVIEW_OFFERS.copy(status = PremiumStatusUiModel.Pending)
            ),
            chosen = PremiumPlanUiModel.Monthly,
            unlocked = null,
            actions = PREVIEW_ACTIONS,
            messages = {}
        )
    }
}

@Preview(name = "Paywall, unavailable")
@Composable
private fun PaywallUnavailablePreview() {
    MigrelyTheme {
        PaywallContents(
            context = PaywallContextUiModel.Insights,
            viewState = PaywallViewStateUiModel.Unavailable,
            chosen = null,
            unlocked = null,
            actions = PREVIEW_ACTIONS,
            messages = {}
        )
    }
}

@Preview(name = "Paywall, lifetime over a subscription")
@Composable
private fun PaywallLifetimePreview() {
    MigrelyTheme {
        PaywallContents(
            context = PaywallContextUiModel.Settings,
            viewState = PaywallViewStateUiModel.Loading,
            chosen = null,
            unlocked = PremiumStatusUiModel.Lifetime(subscriptionRenewsToo = true),
            actions = PREVIEW_ACTIONS,
            messages = {}
        )
    }
}
