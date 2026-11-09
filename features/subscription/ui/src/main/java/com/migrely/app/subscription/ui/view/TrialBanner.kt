package com.migrely.app.subscription.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.subscription.ui.R
import com.migrely.app.subscription.ui.di.TrialBannerDependencies
import com.migrely.app.subscription.ui.model.TrialBannerViewStateUiModel
import java.time.Instant

/**
 * Home's reminder a day before the free trial ends (BRD §7.3, EC-BIL-02, D111), in the slot home
 * leaves to `:app`. It loads whenever [refreshKey] changes, which home does every time it loads,
 * and once dismissed it does not come back for that trial.
 */
@Composable
fun TrialBannerDependencies.TrialBanner(refreshKey: Int, modifier: Modifier = Modifier) {
    var banner by remember { mutableStateOf<TrialBannerViewStateUiModel.Shown?>(null) }
    // Counts the offline notes, so a second tap is a new note that TalkBack speaks again (D166).
    var offlineNote by remember { mutableIntStateOf(0) }
    LaunchedEffect(trialBannerViewModel) {
        trialBannerViewModel.viewState.collect { viewState ->
            val shown = trialBannerViewStateUiMapper.toUi(viewState) as?
                TrialBannerViewStateUiModel.Shown
            if (shown?.offlineToManage == true) offlineNote++
            banner = shown
        }
    }
    LaunchedEffect(refreshKey) { trialBannerViewModel.onEnter() }
    val shown = banner ?: return
    TrialBannerContents(
        endsToday = shown.endsToday,
        renews = shown.renews,
        offlineNote = offlineNote.takeIf { shown.offlineToManage },
        onManage = trialBannerViewModel::onManageAction,
        onDismiss = {
            trialBannerViewModel.onDismissAction(Instant.ofEpochMilli(shown.trialEndMillis))
        },
        modifier = modifier
    )
}

/**
 * The banner itself: its words are one node, and "Manage" and dismissing are buttons. With an
 * [offlineNote], "Manage" was tapped offline, and the note under it says so as a polite live region;
 * each new number is a new note (BRD D166).
 */
@Suppress("LongParameterList")
@Composable
fun TrialBannerContents(
    endsToday: Boolean,
    renews: Boolean,
    offlineNote: Int?,
    onManage: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier.fillMaxWidth().padding(top = 12.dp)
    ) {
        Column(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 8.dp)
                        .semantics(mergeDescendants = true) { }
                ) {
                    Text(
                        stringResource(titleOf(endsToday, renews)),
                        style = MaterialTheme.typography.titleMedium
                    )
                    // Once cancelled, nothing is charged and nothing is left to cancel (M15).
                    Text(
                        stringResource(
                            if (renews) {
                                R.string.subscription_trial_banner_text
                            } else {
                                R.string.subscription_trial_banner_cancelled
                            }
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        MigrelyIcons.Close,
                        contentDescription = stringResource(R.string.subscription_trial_dismiss)
                    )
                }
            }
            TextButton(onClick = onManage, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.subscription_trial_manage))
            }
            offlineNote?.let { note ->
                key(note) {
                    Text(
                        text = stringResource(R.string.subscription_offline_manage),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp, bottom = 8.dp)
                            .semantics { liveRegion = LiveRegionMode.Polite }
                    )
                }
            }
        }
    }
}

private fun titleOf(endsToday: Boolean, renews: Boolean) = when {
    renews && endsToday -> R.string.subscription_trial_ends_today
    renews -> R.string.subscription_trial_ends_tomorrow
    endsToday -> R.string.subscription_trial_and_premium_end_today
    else -> R.string.subscription_trial_and_premium_end_tomorrow
}

@Preview(name = "Trial banner")
@Composable
private fun TrialBannerPreview() {
    MigrelyTheme {
        TrialBannerContents(
            endsToday = false,
            renews = true,
            offlineNote = null,
            onManage = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "Trial banner, light")
@Composable
private fun TrialBannerLightPreview() {
    MigrelyTheme(dark = false) {
        TrialBannerContents(
            endsToday = true,
            renews = false,
            offlineNote = 1,
            onManage = {},
            onDismiss = {}
        )
    }
}
