package com.migrely.app.subscription.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.subscription.presentation.model.PremiumSectionViewState
import com.migrely.app.subscription.ui.R
import com.migrely.app.subscription.ui.di.PremiumSectionDependencies
import com.migrely.app.subscription.ui.model.PremiumPlanUiModel
import com.migrely.app.subscription.ui.model.PremiumSectionUiModel
import com.migrely.app.subscription.ui.model.PremiumSectionViewStateUiModel
import com.migrely.app.subscription.ui.model.PremiumStatusUiModel
import com.migrely.app.subscription.ui.model.RestoreNoteUiModel

private val ROW_MIN_HEIGHT = 56.dp

/** Callbacks from the Premium section. */
data class PremiumSectionActions(
    val onStatus: () -> Unit,
    val onRestore: () -> Unit,
    val onManage: () -> Unit
)

/**
 * Settings' Premium section (BRD SUB-F2, D112), which `:app` puts at the top of Settings: the
 * status with the trial's end when known, "Restore purchases", and "Manage subscription" for a
 * subscription. A free user's status row opens the paywall; a subscriber's opens it to change
 * plans. It reads the status again every time Settings resumes.
 */
@Composable
fun PremiumSectionDependencies.PremiumSection(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    ViewModelObserver(navController)
    LifecycleResumeEffect(premiumSectionViewModel) {
        premiumSectionViewModel.onEnter()
        onPauseOrDispose { }
    }
    // A purchase change Google Play reported while Settings shows is shown at once (review I1).
    LaunchedEffect(purchaseUpdates) {
        purchaseUpdates.collect { premiumSectionViewModel.onPurchaseUpdatedAction() }
    }
    val viewState by premiumSectionViewModel.viewState
        .collectAsState(PremiumSectionViewState.Loading)
    val uiState = premiumSectionViewStateUiMapper.toUi(viewState)
    // The status stays while Google Play is asked again, so the section does not jump.
    var shown by rememberSaveable { mutableStateOf<PremiumSectionUiModel?>(null) }
    (uiState as? PremiumSectionViewStateUiModel.Ready)?.let { shown = it.section }
    PremiumSectionContents(
        section = shown,
        checking = uiState == PremiumSectionViewStateUiModel.Checking,
        actions = PremiumSectionActions(
            onStatus = premiumSectionViewModel::onStatusAction,
            onRestore = premiumSectionViewModel::onRestoreAction,
            onManage = premiumSectionViewModel::onManageAction
        ),
        modifier = modifier
    )
}

/** The section itself, without its view model. Each row is one labelled node (EC-UX-04). */
@Composable
fun PremiumSectionContents(
    section: PremiumSectionUiModel?,
    checking: Boolean,
    actions: PremiumSectionActions,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.subscription_section_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 4.dp)
                .semantics { heading() }
        )
        if (section != null) {
            val status = section.status
            SectionRow(
                title = stringResource(status.title()),
                summary = summary(status),
                onClick = actions.onStatus.takeIf { status.opensPaywall() }
            )
            if (status.managed()) {
                SectionRow(
                    title = stringResource(R.string.subscription_manage),
                    summary = stringResource(R.string.subscription_manage_summary),
                    onClick = actions.onManage
                )
            }
        }
        SectionRow(
            title = stringResource(R.string.subscription_restore),
            summary = stringResource(
                if (checking) {
                    R.string.subscription_checking
                } else {
                    R.string.subscription_restore_summary
                }
            ),
            onClick = actions.onRestore.takeUnless { checking },
            opensScreen = false
        )
        section?.note?.takeUnless { checking }?.let { note ->
            Text(
                text = stringResource(note.text()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
    }
}

/** A row that acts, with a chevron when it leaves the screen, or a fact when [onClick] is null. */
@Composable
private fun SectionRow(
    title: String,
    summary: String,
    onClick: (() -> Unit)?,
    opensScreen: Boolean = true
) {
    val action = if (onClick != null) {
        Modifier.clickable(role = Role.Button, onClick = onClick)
    } else {
        Modifier.semantics(mergeDescendants = true) { }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ROW_MIN_HEIGHT)
            .then(action)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (onClick != null && opensScreen) {
            Icon(
                MigrelyIcons.ChevronEnd,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun PremiumStatusUiModel.title() = when (this) {
    PremiumStatusUiModel.Free -> R.string.subscription_status_free
    PremiumStatusUiModel.Pending -> R.string.subscription_status_pending
    is PremiumStatusUiModel.Subscribed -> when {
        trialEndMillis != null -> R.string.subscription_status_trial
        plan == PremiumPlanUiModel.Yearly -> R.string.subscription_status_yearly
        plan == PremiumPlanUiModel.Monthly -> R.string.subscription_status_monthly
        else -> R.string.subscription_status_premium
    }
    is PremiumStatusUiModel.Lifetime -> R.string.subscription_status_lifetime
}

@Composable
private fun summary(status: PremiumStatusUiModel): String = when (status) {
    PremiumStatusUiModel.Free -> stringResource(R.string.subscription_status_free_summary)
    PremiumStatusUiModel.Pending -> stringResource(R.string.subscription_pending_text)
    is PremiumStatusUiModel.Subscribed ->
        status.trialEndMillis
            ?.let { stringResource(R.string.subscription_status_trial_ends, longDate(it)) }
            ?: stringResource(R.string.subscription_status_change_plan)
    is PremiumStatusUiModel.Lifetime -> stringResource(
        if (status.subscriptionRenewsToo) {
            R.string.subscription_status_still_renews
        } else {
            R.string.subscription_status_lifetime_summary
        }
    )
}

/** Free, pending and subscribed users can buy or change plans; lifetime has nothing to buy. */
private fun PremiumStatusUiModel.opensPaywall() = when (this) {
    PremiumStatusUiModel.Free, PremiumStatusUiModel.Pending, is PremiumStatusUiModel.Subscribed ->
        true
    is PremiumStatusUiModel.Lifetime -> false
}

/** A subscription, or one that still renews under lifetime, is managed in Google Play. */
private fun PremiumStatusUiModel.managed() = when (this) {
    PremiumStatusUiModel.Free, PremiumStatusUiModel.Pending -> false
    is PremiumStatusUiModel.Subscribed -> true
    is PremiumStatusUiModel.Lifetime -> subscriptionRenewsToo
}

private fun RestoreNoteUiModel.text() = when (this) {
    RestoreNoteUiModel.Restored -> R.string.subscription_note_restored
    RestoreNoteUiModel.NothingFound -> R.string.subscription_note_nothing
    RestoreNoteUiModel.Pending -> R.string.subscription_note_pending
    RestoreNoteUiModel.Unreachable -> R.string.subscription_note_unreachable
    RestoreNoteUiModel.Unavailable -> R.string.subscription_note_unavailable
}

private val PREVIEW_ACTIONS = PremiumSectionActions({}, {}, {})

@Preview(name = "Premium section, yearly")
@Composable
private fun PremiumSectionYearlyPreview() {
    MigrelyTheme {
        PremiumSectionContents(
            section = PremiumSectionUiModel(
                PremiumStatusUiModel.Subscribed(PremiumPlanUiModel.Yearly, null),
                note = null
            ),
            checking = false,
            actions = PREVIEW_ACTIONS
        )
    }
}

@Preview(name = "Premium section, free, light")
@Composable
private fun PremiumSectionFreeLightPreview() {
    MigrelyTheme(dark = false) {
        PremiumSectionContents(
            section = PremiumSectionUiModel(
                PremiumStatusUiModel.Free,
                RestoreNoteUiModel.NothingFound
            ),
            checking = false,
            actions = PREVIEW_ACTIONS
        )
    }
}
