package com.migrely.app.reminders.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.format.MigrelyDateFormat
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.reminders.ui.R
import java.time.LocalDate

/** From this font size the row's value goes under its label, so it is never cut (design D9). */
private const val STACKED_FONT_SCALE = 1.3f

/**
 * The "Doctor visit" section (BRD D168, design D9): its heading, summary, the one "Next visit" row, "Remove" when the
 * domain says the visit is [removable] (after today), and why the reminder may show only on Home. With no date, nothing
 * is added under the row. It reads no clock: [today] comes with the state, so a screen kept past midnight follows it.
 */
@Suppress("LongParameterList")
@Composable
internal fun DoctorVisitSection(
    today: LocalDate,
    nextVisit: LocalDate?,
    removable: Boolean,
    notificationsAllowed: Boolean,
    channelOff: Boolean,
    rowFocus: FocusRequester,
    onPick: () -> Unit,
    onRemove: () -> Unit,
    onOpenChannelSettings: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            stringResource(R.string.reminders_visit_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .semantics { heading() }
        )
        Text(
            stringResource(R.string.reminders_visit_summary),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        NextVisitRow(today, nextVisit, rowFocus, onPick)
        // On its day a visit is not removed: it stays as the last visit a report starts from (BRD EC-REP-04).
        if (nextVisit != null && removable) {
            val removeDescription = stringResource(R.string.reminders_visit_remove_description)
            TextButton(
                onClick = {
                    // The focused control goes, so keyboard focus moves to the row rather than the top (design D9).
                    rowFocus.requestFocus()
                    onRemove()
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .heightIn(min = 48.dp)
                    .semantics { contentDescription = removeDescription }
            ) {
                Text(stringResource(R.string.reminders_visit_remove))
            }
        }
        if (nextVisit != null) {
            when {
                // The screen's own notice above already has "Open settings" (design D9).
                !notificationsAllowed -> HomeOnlyLine()
                channelOff -> NotificationsOffNotice(
                    text = stringResource(R.string.reminders_visit_channel_off),
                    openSettingsDescription = stringResource(
                        R.string.reminders_open_settings_description
                    ),
                    onOpenSettings = onOpenChannelSettings
                )
            }
        }
    }
}

/** One Button node, like the daily "Time" row (EC-UX-19): "Next visit, Friday, November 20, 2026. Change it." */
@Composable
private fun NextVisitRow(
    today: LocalDate,
    nextVisit: LocalDate?,
    rowFocus: FocusRequester,
    onPick: () -> Unit
) {
    val locale = LocalConfiguration.current.locales[0]
    val value = nextVisit?.let { MigrelyDateFormat.short(it, today, locale) }
        ?: stringResource(R.string.reminders_visit_not_set)
    val description = nextVisit?.let {
        stringResource(R.string.reminders_visit_description, MigrelyDateFormat.spoken(it, locale))
    } ?: stringResource(R.string.reminders_visit_description_not_set)
    val modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 56.dp)
        .focusRequester(rowFocus)
        .semantics { contentDescription = description }
        .clickable(role = Role.Button, onClick = onPick)
        .padding(horizontal = 16.dp, vertical = 8.dp)
    val label: @Composable () -> Unit = {
        Text(
            text = stringResource(R.string.reminders_visit_next),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.clearAndSetSemantics { }
        )
    }
    val shown: @Composable () -> Unit = {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clearAndSetSemantics { }
        )
    }
    if (LocalDensity.current.fontScale >= STACKED_FONT_SCALE) {
        Column(modifier) {
            label()
            shown()
        }
    } else {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
            Column(Modifier.weight(1f)) { label() }
            shown()
        }
    }
}

@Composable
private fun HomeOnlyLine() {
    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
        Icon(
            MigrelyIcons.Information,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            text = stringResource(R.string.reminders_visit_home_only),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
