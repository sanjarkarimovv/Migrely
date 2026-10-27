package com.migrely.app.designsystem.picker

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

/** The words the picker shows; the calling feature owns them as string resources. */
@Immutable
data class DateTimePickerLabels(
    val title: String,
    val next: String,
    val cancel: String,
    val confirm: String,
    val outOfRange: String
)

/**
 * Picks a date and then a time in [zoneId], limited to `[minEpochMillis, maxEpochMillis]`. Dates outside the range
 * cannot be chosen, and confirming is disabled with [DateTimePickerLabels.outOfRange] shown while the time is outside
 * it. The time picker follows the device's 12- or 24-hour setting (BRD EC-TIM-05).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongParameterList")
@Composable
fun MigrelyDateTimePicker(
    initialEpochMillis: Long,
    minEpochMillis: Long,
    maxEpochMillis: Long,
    zoneId: ZoneId,
    labels: DateTimePickerLabels,
    onConfirm: (epochMillis: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var pickedDateUtcMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    val pickedDate = pickedDateUtcMillis
    if (pickedDate == null) {
        DateStep(
            initialEpochMillis,
            minEpochMillis,
            maxEpochMillis,
            zoneId,
            labels,
            onDismiss
        ) { utcMillis ->
            pickedDateUtcMillis = utcMillis
        }
    } else {
        TimeStep(
            dateUtcMillis = pickedDate,
            initialEpochMillis = initialEpochMillis,
            range = minEpochMillis..maxEpochMillis,
            zoneId = zoneId,
            labels = labels,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongParameterList")
@Composable
private fun DateStep(
    initialEpochMillis: Long,
    minEpochMillis: Long,
    maxEpochMillis: Long,
    zoneId: ZoneId,
    labels: DateTimePickerLabels,
    onDismiss: () -> Unit,
    onDatePicked: (utcMillis: Long) -> Unit
) {
    val firstDate = Instant.ofEpochMilli(minEpochMillis).atZone(zoneId).toLocalDate()
    val lastDate = Instant.ofEpochMilli(maxEpochMillis).atZone(zoneId).toLocalDate()
    val initialDate = Instant.ofEpochMilli(initialEpochMillis).atZone(zoneId).toLocalDate()
    // The Material date picker works in UTC days, so dates are converted at their UTC midnight.
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.coerceIn(firstDate, lastDate).utcMidnight(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
                return !date.isBefore(firstDate) && !date.isAfter(lastDate)
            }

            override fun isSelectableYear(year: Int) = year in firstDate.year..lastDate.year
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { state.selectedDateMillis?.let(onDatePicked) },
                enabled = state.selectedDateMillis != null
            ) { Text(labels.next) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(labels.cancel) } }
    ) {
        DatePicker(state = state, title = { Text(labels.title, Modifier.padding(16.dp)) })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongParameterList")
@Composable
private fun TimeStep(
    dateUtcMillis: Long,
    initialEpochMillis: Long,
    range: LongRange,
    zoneId: ZoneId,
    labels: DateTimePickerLabels,
    onConfirm: (epochMillis: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val is24Hour = DateFormat.is24HourFormat(LocalContext.current)
    val initialTime = Instant.ofEpochMilli(initialEpochMillis).atZone(zoneId).toLocalTime()
    val state = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = is24Hour
    )
    val date = remember(dateUtcMillis) {
        Instant.ofEpochMilli(dateUtcMillis).atZone(ZoneOffset.UTC).toLocalDate()
    }
    val pickedEpochMillis = date.atTime(LocalTime.of(state.hour, state.minute))
        .atZone(zoneId)
        .toInstant()
        .toEpochMilli()
    // The picker works in whole minutes while the bounds may carry seconds: the minute that holds the lower bound is
    // allowed and the result is clamped to the exact bound, so a boundary can always be picked.
    val inRange = pickedEpochMillis in pickableRange(range)
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = labels.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                // Vertical in every orientation: the horizontal layout Material picks in landscape is wider than a
                // dialog and hid the afternoon hours (QA #79); in landscape the column scrolls instead.
                TimePicker(state = state, layoutType = TimePickerLayoutType.Vertical)
                if (!inRange) {
                    Text(
                        text = labels.outOfRange,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text(labels.cancel) }
                    TextButton(onClick = {
                        onConfirm(pickedEpochMillis.coerceIn(range))
                    }, enabled = inRange) {
                        Text(labels.confirm)
                    }
                }
            }
        }
    }
}

private fun LocalDate.utcMidnight(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun LocalDate.coerceIn(first: LocalDate, last: LocalDate): LocalDate = when {
    isBefore(first) -> first
    isAfter(last) -> last
    else -> this
}

private const val MILLIS_PER_MINUTE = 60_000L

/** [range] widened to the start of the minute that holds its lower bound. */
internal fun pickableRange(range: LongRange): LongRange =
    (range.first - Math.floorMod(range.first, MILLIS_PER_MINUTE))..range.last
