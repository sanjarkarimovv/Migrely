package com.migrely.app.designsystem.picker

import android.view.WindowManager
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import com.migrely.app.designsystem.dialog.MigrelyDialog
import com.migrely.app.designsystem.format.MigrelyDateFormat
import java.time.LocalDate

/** The words the day picker shows; the calling feature owns them as string resources. */
@Immutable
data class DatePickerLabels(
    val title: String,
    val confirm: String,
    val cancel: String,
    /** The headline before a day is picked. */
    val none: String
)

/**
 * Picks one day from [firstDate] to [lastDate], full screen like [MigrelyDateRangePicker] and by the same rules (BRD
 * D127): Cancel, the title and Done in a row that never scrolls, Done on only once a day is picked, the window resized
 * for the keyboard. Other days show disabled. Under the row, one headline says "Pick a day" or the day picked, politely
 * spoken in full. [initial] is picked and shown at first, so Done is on at once.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongParameterList")
@Composable
fun MigrelyDatePicker(
    initial: LocalDate?,
    firstDate: LocalDate,
    lastDate: LocalDate,
    labels: DatePickerLabels,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    // The Material picker works in UTC days, so dates are converted at their UTC midnight.
    val selectableDates = remember(firstDate, lastDate) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) =
                utcTimeMillis in firstDate.utcMidnight()..lastDate.utcMidnight()

            override fun isSelectableYear(year: Int) = year in firstDate.year..lastDate.year
        }
    }
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial?.utcMidnight(),
        initialDisplayedMonthMillis = (initial ?: firstDate).withDayOfMonth(1).utcMidnight(),
        yearRange = firstDate.year..lastDate.year,
        selectableDates = selectableDates
    )
    MigrelyDialog(onDismissRequest = onDismiss, fullScreen = true) {
        // As in the range picker: the screen makes room for the keyboard itself, so Cancel and Done stay in view.
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            @Suppress("DEPRECATION")
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
        DatePickerScreen(
            state,
            labels,
            today = firstDate,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }
}

/**
 * The picker's full-screen content. Everything under the top row scrolls when it does not fit, so in landscape at a
 * large font the weekday row and two whole weeks still show with Done in view. The window is announced by the title.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DatePickerScreen(
    state: DatePickerState,
    labels: DatePickerLabels,
    today: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val picked = state.selectedDateMillis
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .semantics { paneTitle = labels.title },
        color = DatePickerDefaults.colors().containerColor
    ) {
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
                    onClick = { picked?.let { onConfirm(it.utcDate()) } },
                    enabled = picked != null
                ) { Text(labels.confirm) }
            }
            BoxWithConstraints(Modifier.weight(1f)) {
                // Typing a date over the keyboard leaves little room: the date field shows the date then (D127).
                val headerShown =
                    state.displayMode != DisplayMode.Input ||
                        maxHeight >= ROOM_FOR_HEADER_WHILE_TYPING
                DatePicker(
                    state = state,
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    title = null,
                    headline = if (headerShown) {
                        { Headline(picked?.utcDate(), labels.none, today) }
                    } else {
                        null
                    },
                    showModeToggle = headerShown
                )
            }
        }
    }
}

@Composable
private fun Headline(picked: LocalDate?, none: String, today: LocalDate) {
    val locale = LocalConfiguration.current.locales[0]
    val shown = picked?.let { MigrelyDateFormat.short(it, today, locale) } ?: none
    val spoken = picked?.let { MigrelyDateFormat.spoken(it, locale) } ?: none
    Text(
        text = shown,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .padding(start = 24.dp, end = 12.dp, bottom = 12.dp)
            .semantics {
                contentDescription = spoken
                liveRegion = LiveRegionMode.Polite
            }
    )
}
