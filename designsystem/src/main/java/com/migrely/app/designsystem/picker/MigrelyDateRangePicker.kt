package com.migrely.app.designsystem.picker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerDefaults
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** The words the range picker shows; the calling feature owns them as string resources. */
@Immutable
data class DateRangePickerLabels(val title: String, val confirm: String, val cancel: String)

/**
 * Picks a range of days up to [lastDate] in the years of [yearRange], full screen as Material advises for a range
 * picker on a phone (BRD D127). Done takes the chosen range, or the start day alone when no end is chosen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongParameterList")
@Composable
fun MigrelyDateRangePicker(
    initialStart: LocalDate?,
    initialEnd: LocalDate?,
    lastDate: LocalDate,
    yearRange: IntRange,
    labels: DateRangePickerLabels,
    onConfirm: (start: LocalDate, end: LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    // The Material picker works in UTC days, so dates are converted at their UTC midnight.
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStart?.utcMidnight(),
        initialSelectedEndDateMillis = initialEnd?.utcMidnight(),
        yearRange = yearRange,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) =
                utcTimeMillis <= lastDate.utcMidnight()

            override fun isSelectableYear(year: Int) = year <= lastDate.year
        }
    )
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        DateRangePickerScreen(state, labels, onConfirm, onDismiss)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateRangePickerScreen(
    state: DateRangePickerState,
    labels: DateRangePickerLabels,
    onConfirm: (start: LocalDate, end: LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val start = state.selectedStartDateMillis
    Surface(Modifier.fillMaxSize(), color = DatePickerDefaults.colors().containerColor) {
        Column(Modifier.safeDrawingPadding()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                TextButton(onClick = onDismiss) { Text(labels.cancel) }
                Text(
                    text = labels.title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .semantics { heading() }
                )
                TextButton(
                    onClick = {
                        if (start !=
                            null
                        ) {
                            onConfirm(
                                start.utcDate(),
                                (state.selectedEndDateMillis ?: start).utcDate()
                            )
                        }
                    },
                    enabled = start != null
                ) { Text(labels.confirm) }
            }
            // The picker's header does not scroll and the months get only the height left under it. Material's title
            // keeps the header at least 128 dp high and its large headline wraps the two dates into many lines, so
            // at 200% font no week was left (#102): the title is in the row above, and the headline is smaller.
            val dateFormatter = remember { DatePickerDefaults.dateFormatter() }
            DateRangePicker(
                state = state,
                modifier = Modifier.weight(1f),
                dateFormatter = dateFormatter,
                title = null,
                headline = {
                    ProvideTextStyle(MaterialTheme.typography.titleLarge) {
                        DateRangePickerDefaults.DateRangePickerHeadline(
                            selectedStartDateMillis = state.selectedStartDateMillis,
                            selectedEndDateMillis = state.selectedEndDateMillis,
                            displayMode = state.displayMode,
                            dateFormatter = dateFormatter,
                            modifier = Modifier.padding(start = 24.dp, end = 12.dp, bottom = 12.dp)
                        )
                    }
                }
            )
        }
    }
}

private fun Long.utcDate(): LocalDate =
    Instant.ofEpochMilli(this).atOffset(ZoneOffset.UTC).toLocalDate()
