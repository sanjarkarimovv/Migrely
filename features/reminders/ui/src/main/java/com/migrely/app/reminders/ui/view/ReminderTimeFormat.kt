package com.migrely.app.reminders.ui.view

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private const val MINUTES_PER_HOUR = 60

/**
 * The phone's own 12- or 24-hour choice, as the time picker uses, in the activity's formatting locale, English with the
 * phone's region (BRD D117).
 */
@Composable
internal fun reminderTimePattern(): DateTimeFormatter =
    if (DateFormat.is24HourFormat(LocalContext.current)) {
        DateTimeFormatter.ofPattern("HH:mm")
    } else {
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(LocalConfiguration.current.locales[0])
    }

internal fun Int.toReminderTime(): LocalTime = LocalTime.of(
    this / MINUTES_PER_HOUR,
    this % MINUTES_PER_HOUR
)
