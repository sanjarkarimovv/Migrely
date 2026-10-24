package com.migrely.app.history.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.theme.MigrelyColors
import com.migrely.app.history.ui.R
import com.migrely.app.history.ui.mapper.HistoryTimeUiFormatter
import com.migrely.app.history.ui.model.HistoryMonthUiModel
import com.migrely.app.history.ui.model.PainLevelUiModel
import com.migrely.app.history.ui.model.ProgressAgainstUiModel
import com.migrely.app.history.ui.model.ProgressTrendUiModel
import com.migrely.app.history.ui.model.ProgressUiModel
import java.time.LocalDate

/**
 * The month's numbers under the calendar (BRD D53–D56): "October so far" for the current month. The chronic notice and
 * the progress message follow them (D54, D55). Each number is one node for screen readers, read as "Migraine days: 6".
 */
@Composable
internal fun MonthSummary(
    month: HistoryMonthUiModel,
    timeUiFormatter: HistoryTimeUiFormatter,
    modifier: Modifier = Modifier
) {
    val stats = month.stats
    val today = LocalDate.ofEpochDay(month.todayEpochDay)
    val name = timeUiFormatter.formatMonthName(month.year, month.month)
    val current = today.year == month.year && today.monthValue == month.month
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = if (current) stringResource(R.string.summary_title_so_far, name) else name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .semantics { heading() }
            )
            StatRow(stringResource(R.string.stat_migraines), stats.migraines.toString())
            StatRow(stringResource(R.string.stat_migraine_days), stats.migraineDays.toString())
            StatRow(stringResource(R.string.stat_pain_free_days), stats.painFreeDays.toString())
            val peak = stats.averagePeak?.let { stringResource(it.word()) }
            StatRow(
                label = stringResource(R.string.stat_average_peak),
                value = peak ?: stringResource(R.string.stat_no_peak),
                spokenValue = peak ?: stringResource(R.string.stat_no_peak_description)
            )
            StatRow(
                stringResource(R.string.stat_attack_medication_days),
                stats.attackMedicationDays.toString()
            )
            if (stats.chronic) ChronicNotice(name)
            stats.progress?.let {
                ProgressLine(it, timeUiFormatter, Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, spokenValue: String = value) {
    val description = stringResource(R.string.stat_description, label, spokenValue)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 32.dp)
            .clearAndSetSemantics { contentDescription = description }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

/** A calm notice, not a warning: no warning colour and no blame (BRD STA-F2, G1, D54). */
@Composable
private fun ChronicNotice(monthName: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Icon(
                MigrelyIcons.Information,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(20.dp)
            )
            Text(
                text = stringResource(R.string.chronic_notice, monthName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * The progress message (BRD EC-UX-14, D55): "3 fewer migraine days than August", or "… than this time last month" in
 * the current month. Only fewer days use the positive colour; more or as many read in the neutral one.
 */
@Composable
internal fun ProgressLine(
    progress: ProgressUiModel,
    timeUiFormatter: HistoryTimeUiFormatter,
    modifier: Modifier = Modifier
) {
    val month = when (val against = progress.against) {
        is ProgressAgainstUiModel.WholeMonth -> timeUiFormatter.formatMonthName(
            against.year,
            against.month
        )
        ProgressAgainstUiModel.SameDaysLastMonth -> null
    }
    val days = progress.days
    val text = when (progress.trend) {
        ProgressTrendUiModel.Fewer -> if (month == null) {
            pluralStringResource(R.plurals.progress_fewer_this_time, days, days)
        } else {
            pluralStringResource(R.plurals.progress_fewer_than_month, days, days, month)
        }
        ProgressTrendUiModel.More -> if (month == null) {
            pluralStringResource(R.plurals.progress_more_this_time, days, days)
        } else {
            pluralStringResource(R.plurals.progress_more_than_month, days, days, month)
        }
        ProgressTrendUiModel.Same -> if (month == null) {
            stringResource(R.string.progress_same_this_time)
        } else {
            stringResource(R.string.progress_same_month, month)
        }
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = if (progress.trend == ProgressTrendUiModel.Fewer) {
            MigrelyColors.Positive
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        modifier = modifier
    )
}

private fun PainLevelUiModel.word() = when (this) {
    PainLevelUiModel.Mild -> R.string.legend_mild
    PainLevelUiModel.Moderate -> R.string.legend_moderate
    PainLevelUiModel.Severe -> R.string.legend_severe
}
