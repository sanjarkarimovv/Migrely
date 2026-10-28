package com.migrely.app.designsystem.picker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerDefaults
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.paneTitle
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
 * picker on a phone (BRD D127). Done is on once the range has an end, as in Material's own full-screen picker;
 * picking the same day twice gives a range of one day.
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
    val selectableDates = remember(lastDate) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) =
                utcTimeMillis <= lastDate.utcMidnight()

            override fun isSelectableYear(year: Int) = year <= lastDate.year
        }
    }
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStart?.utcMidnight(),
        initialSelectedEndDateMillis = initialEnd?.utcMidnight(),
        yearRange = yearRange,
        selectableDates = selectableDates
    )
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        DateRangePickerScreen(state, labels, onConfirm, onDismiss)
    }
}

/**
 * The picker's full-screen content: Cancel, the title and Done in a row above Material's range picker. The window
 * is announced by the title, as a dialog's would be.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateRangePickerScreen(
    state: DateRangePickerState,
    labels: DateRangePickerLabels,
    onConfirm: (start: LocalDate, end: LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val start = state.selectedStartDateMillis
    val end = state.selectedEndDateMillis
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .semantics { paneTitle = labels.title },
        color = DatePickerDefaults.colors().containerColor
    ) {
        // Clear of the system bars, but not of the keyboard: in text input mode the fields sit at the top, and
        // making room for the keyboard squeezed them to thin lines in landscape at 200% font.
        Column(
            Modifier.windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout))
        ) {
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
                        if (start != null && end != null) onConfirm(start.utcDate(), end.utcDate())
                    },
                    // Not on a start alone: in text input mode, a changed start drops the end while its field
                    // still shows it, and an end that is not allowed is dropped too.
                    enabled = start != null && end != null
                ) { Text(labels.confirm) }
            }
            // The picker's header does not scroll, and the months get only the height left under it (#102). So the
            // picker fills the screen, its title is in the row above instead of a header line, and the headline
            // starts 24 dp in rather than Material's 64 dp, so the two dates wrap less.
            val dateFormatter = remember { DatePickerDefaults.dateFormatter() }
            DateRangePicker(
                state = state,
                modifier = Modifier.weight(1f),
                dateFormatter = dateFormatter,
                title = null,
                headline = {
                    DateRangePickerDefaults.DateRangePickerHeadline(
                        selectedStartDateMillis = state.selectedStartDateMillis,
                        selectedEndDateMillis = state.selectedEndDateMillis,
                        displayMode = state.displayMode,
                        dateFormatter = dateFormatter,
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, bottom = 12.dp)
                    )
                }
            )
        }
    }
}

private fun Long.utcDate(): LocalDate =
    Instant.ofEpochMilli(this).atOffset(ZoneOffset.UTC).toLocalDate()
