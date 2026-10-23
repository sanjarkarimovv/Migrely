package com.migrely.app.attack.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.migrely.app.attack.ui.R
import com.migrely.app.attack.ui.mapper.AttackTimeUiFormatter
import com.migrely.app.attack.ui.model.AttackDetailsUiModel
import com.migrely.app.attack.ui.model.TimeErrorReasonUiModel
import com.migrely.app.attack.ui.model.TimeErrorUiModel
import com.migrely.app.attack.ui.model.TimeFieldUiModel
import com.migrely.app.designsystem.picker.DateTimePickerLabels
import com.migrely.app.designsystem.picker.MigrelyDateTimePicker
import java.time.LocalDate
import java.time.ZoneId

private const val MILLIS_PER_MINUTE = 60_000L
private const val MINUTES_PER_HOUR = 60L
private const val MINUTES_PER_DAY = 24 * 60L

/** The clock tolerance of the domain (BRD EC-ATK-03), so the pickers allow what the domain accepts. */
private const val CLOCK_TOLERANCE_MILLIS = 5 * MILLIS_PER_MINUTE

/** The earliest start a picker offers; attacks are never logged this far back. */
private val EARLIEST_START_MILLIS = LocalDate.of(
    2000,
    1,
    1
).atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()

/**
 * Start, end and duration (BRD EC-ATK-03, EC-ATK-04). The pickers keep each time inside what the domain accepts; a
 * rejection the pickers cannot foresee, such as an overlap, shows as an inline error under the time.
 */
@Composable
internal fun TimeSection(
    details: AttackDetailsUiModel,
    timeUiFormatter: AttackTimeUiFormatter,
    timeError: TimeErrorUiModel?,
    actions: AttackDetailsActions
) {
    var picking by rememberSaveable(details.id) { mutableStateOf<TimeFieldUiModel?>(null) }
    // Read when a picker opens, so a screen left open for a while still allows the current time.
    val latestAllowed = remember(picking) { System.currentTimeMillis() } + CLOCK_TOLERANCE_MILLIS
    val end = details.endEpochMillis
    Column {
        SectionTitle(stringResource(R.string.section_time))
        val startText = timeUiFormatter.formatDateTime(
            details.startEpochMillis,
            details.startZoneId
        )
        TimeRow(
            label = stringResource(R.string.time_started),
            value = startText,
            description = stringResource(R.string.time_change_start, startText),
            error = timeError?.takeIf { it.field == TimeFieldUiModel.Start },
            zoneId = details.startZoneId,
            timeUiFormatter = timeUiFormatter,
            onClick = { picking = TimeFieldUiModel.Start }
        )
        if (end == null) {
            TimeRow(
                label = stringResource(R.string.time_ended),
                value = stringResource(R.string.time_in_progress),
                description = null,
                error = null,
                zoneId = details.startZoneId,
                timeUiFormatter = timeUiFormatter,
                onClick = null
            )
        } else {
            val endText = timeUiFormatter.formatDateTime(end, details.startZoneId)
            TimeRow(
                label = stringResource(R.string.time_ended),
                value = endText,
                description = stringResource(R.string.time_change_end, endText),
                error = timeError?.takeIf { it.field == TimeFieldUiModel.End },
                zoneId = details.startZoneId,
                timeUiFormatter = timeUiFormatter,
                onClick = { picking = TimeFieldUiModel.End }
            )
        }
        val duration = durationText((end ?: details.nowEpochMillis) - details.startEpochMillis)
        Text(
            text = stringResource(
                if (end == null) R.string.time_duration_so_far else R.string.time_duration,
                duration
            ),
            style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
        )
    }
    val zone = zoneOf(details.startZoneId)
    when (picking) {
        TimeFieldUiModel.Start -> MigrelyDateTimePicker(
            initialEpochMillis = details.startEpochMillis,
            minEpochMillis = EARLIEST_START_MILLIS,
            // Strictly before the end: the picker works in whole minutes, the stored end may have seconds.
            maxEpochMillis = end?.minus(1) ?: latestAllowed,
            zoneId = zone,
            labels = pickerLabels(R.string.picker_start_title, R.string.picker_start_out_of_range),
            onConfirm = { start ->
                picking = null
                // Confirming the minute already shown keeps the stored time and its seconds.
                if (!sameMinute(start, details.startEpochMillis)) actions.onStartPicked(start)
            },
            onDismiss = { picking = null }
        )

        TimeFieldUiModel.End -> MigrelyDateTimePicker(
            initialEpochMillis = end ?: (latestAllowed - CLOCK_TOLERANCE_MILLIS),
            minEpochMillis = details.startEpochMillis + 1,
            maxEpochMillis = latestAllowed,
            zoneId = zone,
            labels = pickerLabels(R.string.picker_end_title, R.string.picker_end_out_of_range),
            onConfirm = { newEnd ->
                picking = null
                if (end == null || !sameMinute(newEnd, end)) actions.onEndPicked(newEnd)
            },
            onDismiss = { picking = null }
        )

        null -> Unit
    }
}

@Suppress("LongParameterList")
@Composable
private fun TimeRow(
    label: String,
    value: String,
    description: String?,
    error: TimeErrorUiModel?,
    zoneId: String,
    timeUiFormatter: AttackTimeUiFormatter,
    onClick: (() -> Unit)?
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .then(
                    if (onClick == null) {
                        Modifier
                    } else {
                        Modifier.clickable(role = Role.Button, onClick = onClick)
                    }
                )
                .then(
                    if (description ==
                        null
                    ) {
                        Modifier
                    } else {
                        Modifier.semantics { contentDescription = description }
                    }
                )
                .heightIn(min = 56.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // The label sits above the value, so a long date never squeezes the label at large font sizes.
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
                color = if (onClick == null) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.padding(top = 2.dp)
            )
            if (error != null) {
                Text(
                    text = errorText(error.reason, zoneId, timeUiFormatter),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun errorText(
    reason: TimeErrorReasonUiModel,
    zone: String,
    timeUiFormatter: AttackTimeUiFormatter
): String = when (reason) {
    TimeErrorReasonUiModel.InFuture -> stringResource(R.string.time_error_in_future)
    TimeErrorReasonUiModel.EndBeforeStart ->
        stringResource(R.string.time_error_end_before_start)
    is TimeErrorReasonUiModel.Overlaps -> {
        // In the attack's zone, like every other time on this screen.
        val start = timeUiFormatter.formatDateTime(reason.startEpochMillis, zone)
        val otherEnd = reason.endEpochMillis
        if (otherEnd == null) {
            stringResource(R.string.time_error_overlaps_in_progress, start)
        } else {
            stringResource(
                R.string.time_error_overlaps,
                start,
                timeUiFormatter.formatWithinAttack(otherEnd, reason.startEpochMillis, zone)
            )
        }
    }
}

@Composable
internal fun pickerLabels(title: Int, outOfRange: Int) = DateTimePickerLabels(
    title = stringResource(title),
    next = stringResource(R.string.picker_next),
    cancel = stringResource(R.string.picker_cancel),
    confirm = stringResource(R.string.picker_confirm),
    outOfRange = stringResource(outOfRange)
)

@Composable
internal fun durationText(millis: Long): String {
    val minutes = (millis / MILLIS_PER_MINUTE).coerceAtLeast(0)
    return when {
        minutes < MINUTES_PER_HOUR -> stringResource(R.string.duration_minutes, minutes.toInt())
        minutes < MINUTES_PER_DAY -> stringResource(
            R.string.duration_hours_minutes,
            (minutes / MINUTES_PER_HOUR).toInt(),
            (minutes % MINUTES_PER_HOUR).toInt()
        )

        else -> stringResource(
            R.string.duration_days_hours,
            (minutes / MINUTES_PER_DAY).toInt(),
            ((minutes % MINUTES_PER_DAY) / MINUTES_PER_HOUR).toInt()
        )
    }
}

private fun sameMinute(first: Long, second: Long) =
    first / MILLIS_PER_MINUTE == second / MILLIS_PER_MINUTE

internal fun zoneOf(id: String): ZoneId = try {
    ZoneId.of(id)
} catch (_: java.time.DateTimeException) {
    ZoneId.systemDefault()
}
