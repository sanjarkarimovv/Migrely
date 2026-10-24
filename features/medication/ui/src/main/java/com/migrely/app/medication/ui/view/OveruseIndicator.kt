package com.migrely.app.medication.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.medication.presentation.model.OveruseIndicatorViewState
import com.migrely.app.medication.ui.R
import com.migrely.app.medication.ui.di.OveruseIndicatorDependencies
import com.migrely.app.medication.ui.model.OveruseCategoryUiModel
import com.migrely.app.medication.ui.model.OveruseIndicatorRowUiModel
import com.migrely.app.medication.ui.model.OveruseIndicatorUiModel

/**
 * The standing overuse indicator in History's month summary (BRD EC-MED-06, D59): each category's attack-medication
 * days in the last 30 days against its limit. `:app` places it, and it loads whenever [refreshKey] changes, which
 * History does every time it loads.
 */
@Composable
fun OveruseIndicatorDependencies.OveruseIndicator(refreshKey: Int, modifier: Modifier = Modifier) {
    LaunchedEffect(refreshKey) { overuseIndicatorViewModel.onEnter() }
    val viewState by overuseIndicatorViewModel.viewState
        .collectAsState<OveruseIndicatorViewState, OveruseIndicatorViewState?>(null)
    viewState?.let { OveruseIndicatorContents(overuseIndicatorUiMapper.toUi(it), modifier) }
}

/**
 * Rows at or past their limit read in amber, the colour kept for overuse, and say "at the limit" or "over the limit";
 * one calm line follows them. The others read neutral. Nothing shows without a row.
 */
@Composable
internal fun OveruseIndicatorContents(
    indicator: OveruseIndicatorUiModel,
    modifier: Modifier = Modifier
) {
    if (indicator.rows.isEmpty()) return
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.indicator_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.semantics { heading() }
        )
        indicator.rows.forEach { row ->
            val counted = stringResource(row.category.indicatorRow(), row.days, row.limit)
            // Said in words too, not by colour alone (WCAG 1.4.1).
            Text(
                text = when {
                    !row.reached -> counted
                    row.days == row.limit -> stringResource(R.string.indicator_at_limit, counted)
                    else -> stringResource(R.string.indicator_over_limit, counted)
                },
                style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                color = if (row.reached) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        if (indicator.rows.any(OveruseIndicatorRowUiModel::reached)) {
            Text(
                text = stringResource(R.string.indicator_reached),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun OveruseCategoryUiModel.indicatorRow() = when (this) {
    OveruseCategoryUiModel.SimplePainkiller -> R.string.indicator_row_simple
    OveruseCategoryUiModel.Triptan -> R.string.indicator_row_triptan
    OveruseCategoryUiModel.CombinationOrOther -> R.string.indicator_row_other
    OveruseCategoryUiModel.Multiple -> R.string.indicator_row_multiple
}

@Preview(name = "Overuse indicator")
@Composable
private fun OveruseIndicatorPreview() {
    MigrelyTheme {
        OveruseIndicatorContents(
            OveruseIndicatorUiModel(
                listOf(
                    OveruseIndicatorRowUiModel(
                        OveruseCategoryUiModel.SimplePainkiller,
                        7,
                        15,
                        false
                    ),
                    OveruseIndicatorRowUiModel(OveruseCategoryUiModel.Triptan, 5, 10, false),
                    OveruseIndicatorRowUiModel(OveruseCategoryUiModel.Multiple, 12, 10, true)
                )
            ),
            Modifier.padding(16.dp)
        )
    }
}
