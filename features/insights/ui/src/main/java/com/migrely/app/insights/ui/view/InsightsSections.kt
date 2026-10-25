package com.migrely.app.insights.ui.view

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.migrely.app.insights.ui.R
import com.migrely.app.insights.ui.model.InsightsCycleUiModel
import com.migrely.app.insights.ui.model.InsightsMedicationUiModel
import com.migrely.app.insights.ui.model.InsightsMonthUiModel
import com.migrely.app.insights.ui.model.InsightsPatternsUiModel
import com.migrely.app.insights.ui.model.InsightsPercentUiModel
import com.migrely.app.insights.ui.model.InsightsTriggerUiModel
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/** A titled card; the title is a heading, so a screen reader can move from section to section. */
@Composable
internal fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )
            content()
        }
    }
}

@Composable
private fun BodyText(text: String, muted: Boolean = false) {
    Text(
        text = text,
        style = if (muted) {
            MaterialTheme.typography.bodyMedium
        } else {
            MaterialTheme.typography.bodyLarge
        },
        color = if (muted) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    )
}

/** What is still missing before the sections show (BRD EC-INS-01, D88). */
@Composable
internal fun ProgressCard(patterns: InsightsPatternsUiModel.NotEnough) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            if (patterns.missingMigraines > 0) {
                BodyText(
                    pluralStringResource(
                        R.plurals.insights_more_migraines,
                        patterns.missingMigraines,
                        patterns.missingMigraines
                    )
                )
            }
            if (patterns.missingDays > 0) {
                BodyText(
                    pluralStringResource(
                        R.plurals.insights_more_days,
                        patterns.missingDays,
                        patterns.missingDays
                    )
                )
            }
        }
    }
}

/** The triggers logged with the most migraines, as counts and shares, never as causes (BRD INS-F6, D89). */
@Composable
internal fun TriggersSection(patterns: InsightsPatternsUiModel.Enough) {
    Section(stringResource(R.string.insights_triggers_title)) {
        if (patterns.triggers.isEmpty()) {
            BodyText(stringResource(R.string.insights_no_triggers), muted = true)
        }
        patterns.triggers.forEach {
            val name = when (val trigger = it.trigger) {
                is InsightsTriggerUiModel.BuiltIn -> stringResource(trigger.trigger.label())
                is InsightsTriggerUiModel.Custom -> trigger.name
            }
            BodyText(
                pluralStringResource(
                    R.plurals.insights_trigger_share,
                    patterns.migraines,
                    name,
                    it.migraines,
                    patterns.migraines,
                    percentText(it.percent)
                )
            )
        }
    }
}

/** Weekdays and times of day, each a bar chart with the busiest named when it is the only one (BRD D90). */
@Composable
internal fun WhenSection(patterns: InsightsPatternsUiModel.Enough) {
    val total = patterns.migraines
    Section(stringResource(R.string.insights_when_title)) {
        SubTitle(stringResource(R.string.insights_weekdays_title))
        val day = patterns.busiestWeekday
        val dayCount = patterns.weekdays.firstOrNull { it.day == day }?.migraines ?: 0
        BodyText(
            if (day == null) {
                stringResource(R.string.insights_no_busiest_day)
            } else {
                stringResource(day.busiest(), dayCount, total)
            }
        )
        patterns.weekdays.forEach {
            CountBar(stringResource(it.day.label()), it.migraines, total, it.fraction)
        }
        SubTitle(stringResource(R.string.insights_times_title))
        val time = patterns.busiestTimeOfDay
        val timeCount = patterns.timesOfDay.firstOrNull { it.time == time }?.migraines ?: 0
        BodyText(
            if (time == null) {
                stringResource(R.string.insights_no_busiest_time)
            } else {
                stringResource(time.busiest(), timeCount, total)
            }
        )
        patterns.timesOfDay.forEach {
            CountBar(stringResource(it.time.label()), it.migraines, total, it.fraction)
        }
        BodyText(stringResource(R.string.insights_zone_note), muted = true)
    }
}

@Composable
private fun SubTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .padding(top = 8.dp)
            .semantics { heading() }
    )
}

@Composable
private fun CountBar(label: String, migraines: Int, total: Int, fraction: Float) {
    BarRow(
        label = label,
        value = stringResource(R.string.insights_count_of, migraines, total),
        fraction = fraction,
        description = pluralStringResource(
            R.plurals.insights_bar_description,
            total,
            label,
            migraines,
            total
        )
    )
}

/** The share of migraines around a period, always beside the share of days those are (BRD D91). */
@Composable
internal fun CycleSection(cycle: InsightsCycleUiModel, migraines: Int) {
    Section(stringResource(R.string.insights_cycle_title)) {
        val share = pluralStringResource(
            R.plurals.insights_cycle_share,
            migraines,
            cycle.migraines,
            migraines,
            percentText(cycle.percent)
        )
        val base = pluralStringResource(
            R.plurals.insights_cycle_base,
            cycle.periodDays,
            percentText(cycle.daysPercent),
            cycle.days,
            cycle.periodDays
        )
        BodyText(stringResource(R.string.insights_cycle_text, share, base))
        BodyText(stringResource(R.string.insights_cycle_window), muted = true)
    }
}

/** Each attack medication and how often it helped, once it has 3 answers (BRD INS-F4, D92). */
@Composable
internal fun MedicationsSection(medications: List<InsightsMedicationUiModel>) {
    Section(stringResource(R.string.insights_medications_title)) {
        if (medications.isEmpty()) {
            BodyText(stringResource(R.string.insights_no_medications), muted = true)
        }
        medications.forEach { medication ->
            val result = when (medication) {
                is InsightsMedicationUiModel.Rated -> pluralStringResource(
                    R.plurals.insights_helped,
                    medication.answered,
                    medication.helped,
                    medication.answered,
                    percentText(medication.percent)
                )
                is InsightsMedicationUiModel.NeedsAnswers ->
                    stringResource(R.string.insights_needs_answers, medication.answered)
            }
            // The name and its answers are read together.
            Column(Modifier.padding(vertical = 4.dp).semantics(mergeDescendants = true) {}) {
                BodyText(medication.name)
                BodyText(result, muted = true)
            }
        }
    }
}

/** Migraine days as bars, and every month's four numbers as text a screen reader reads (BRD INS-F5, D92). */
@Composable
internal fun TrendsSection(trends: List<InsightsMonthUiModel>) {
    Section(stringResource(R.string.insights_trends_title)) {
        BodyText(stringResource(R.string.insights_trends_note), muted = true)
        trends.forEach { month ->
            val name = monthName(month.year, month.month)
            val days = pluralStringResource(
                R.plurals.insights_migraine_days,
                month.migraineDays,
                month.migraineDays
            )
            val migraines =
                pluralStringResource(R.plurals.insights_migraines, month.migraines, month.migraines)
            val medication = pluralStringResource(
                R.plurals.insights_medication_days,
                month.medicationDays,
                month.medicationDays
            )
            val bedRest = pluralStringResource(
                R.plurals.insights_bed_rest,
                month.bedRestMigraines,
                month.bedRestMigraines
            )
            BarRow(
                label = name,
                value = days,
                fraction = month.fraction,
                description = stringResource(
                    R.string.insights_month_description,
                    name,
                    days,
                    migraines,
                    medication,
                    bedRest
                ),
                details = stringResource(
                    R.string.insights_month_details,
                    migraines,
                    medication,
                    bedRest
                )
            )
        }
    }
}

/** "60%", or "<1%" and ">99%" for a share that is not quite none or all. */
@Composable
private fun percentText(percent: InsightsPercentUiModel): String = when (percent) {
    is InsightsPercentUiModel.Whole -> stringResource(R.string.insights_percent, percent.percent)
    InsightsPercentUiModel.BelowOne -> stringResource(R.string.insights_percent_below, 1)
    InsightsPercentUiModel.AboveNinetyNine -> stringResource(R.string.insights_percent_above, 99)
}

/** "October 2026", in the order of the device's language. */
private fun monthName(year: Int, month: Int): String {
    val locale = Locale.getDefault()
    return DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "MMMMy"), locale)
        .format(YearMonth.of(year, month))
}
