package com.migrely.app.reminders.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.migrely.app.designsystem.format.MigrelyDateFormat
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.reminders.presentation.model.VisitCardViewState
import com.migrely.app.reminders.ui.R
import com.migrely.app.reminders.ui.di.VisitCardDependencies
import java.time.LocalDate

/**
 * Home's visit card (BRD D168, design D8), in the slot under the medication prompt. It loads whenever [refreshKey]
 * changes, which home does every time it loads. It comes and goes with no animation, and is a reminder, not a
 * warning: never the safety colour and no warning icon.
 */
@Composable
fun VisitCardDependencies.VisitCard(
    navController: NavController,
    refreshKey: Int,
    modifier: Modifier = Modifier
) {
    ViewModelObserver(navController)
    var visit by remember { mutableStateOf<LocalDate?>(null) }
    LaunchedEffect(visitCardViewModel) {
        visitCardViewModel.viewState.collect { state ->
            visit = when (state) {
                VisitCardViewState.Hidden -> null
                is VisitCardViewState.Shown -> state.visit
            }
        }
    }
    LaunchedEffect(refreshKey) { visitCardViewModel.onEnter() }
    val shown = visit ?: return
    VisitCardContents(
        visit = shown,
        onReport = { visitCardViewModel.onReportAction(shown) },
        onClose = { visitCardViewModel.onCloseAction(shown) },
        modifier = modifier
    )
}

/** The card: its line names the date, never "today" or "tomorrow", so it stays true across midnight. */
@Composable
fun VisitCardContents(
    visit: LocalDate,
    onReport: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = LocalConfiguration.current.locales[0]
    val text = stringResource(
        R.string.reminders_visit_card_text,
        MigrelyDateFormat.short(visit, LocalDate.now(), locale)
    )
    val spoken =
        stringResource(R.string.reminders_visit_card_text, MigrelyDateFormat.spoken(visit, locale))
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(start = 16.dp)) {
            Icon(
                MigrelyIcons.Report,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(top = 16.dp, end = 12.dp)
                    .size(24.dp)
            )
            Column(Modifier.weight(1f).padding(top = 16.dp)) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clearAndSetSemantics { contentDescription = spoken }
                )
                TextButton(
                    onClick = onReport,
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(stringResource(R.string.reminders_visit_card_report))
                }
            }
            IconButton(onClick = onClose) {
                Icon(
                    MigrelyIcons.Close,
                    contentDescription = stringResource(
                        R.string.reminders_visit_card_close_description
                    ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(name = "Visit card")
@Composable
private fun VisitCardPreview() {
    MigrelyTheme {
        VisitCardContents(LocalDate.of(2026, 11, 20), onReport = {}, onClose = {})
    }
}
