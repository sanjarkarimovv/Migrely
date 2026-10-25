package com.migrely.app.attack.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.migrely.app.attack.ui.R
import com.migrely.app.attack.ui.mapper.AttackTimeUiFormatter
import com.migrely.app.attack.ui.model.AttackDetailsUiModel
import com.migrely.app.attack.ui.model.IntensityPointUiModel
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.picker.MigrelyDateTimePicker
import kotlin.math.roundToInt

private const val MINIMUM_LEVEL = 1f
private const val MAXIMUM_LEVEL = 10f
private const val SLIDER_STEPS = 8
private const val DEFAULT_LEVEL = 5f
private const val MILD_UP_TO = 3
private const val MODERATE_UP_TO = 6

/** Pain over time (BRD ATK-F4): the peak, every entry in time order, and adding or removing an entry. */
@Composable
internal fun PainSection(
    details: AttackDetailsUiModel,
    timeUiFormatter: AttackTimeUiFormatter,
    actions: AttackDetailsActions
) {
    var adding by rememberSaveable(details.id) { mutableStateOf(false) }
    Column {
        SectionTitle(stringResource(R.string.section_pain))
        val peak = details.peakIntensity
        Text(
            text = if (peak == null) {
                stringResource(R.string.pain_not_recorded)
            } else {
                stringResource(R.string.pain_peak, peak)
            },
            style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        details.intensityPoints.forEach { point ->
            PainEntryRow(point, details, timeUiFormatter) { actions.onRemovePoint(point) }
        }
        OutlinedButton(
            onClick = { adding = true },
            modifier = Modifier
                .padding(top = 8.dp)
                .heightIn(min = 48.dp)
        ) {
            Icon(MigrelyIcons.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(
                text = stringResource(R.string.pain_add),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
    if (adding) {
        AddPainDialog(
            details = details,
            timeUiFormatter = timeUiFormatter,
            onAdd = { point ->
                adding = false
                actions.onAddPoint(point)
            },
            onDismiss = { adding = false }
        )
    }
}

@Composable
private fun PainEntryRow(
    point: IntensityPointUiModel,
    details: AttackDetailsUiModel,
    timeUiFormatter: AttackTimeUiFormatter,
    onRemove: () -> Unit
) {
    val time = timeUiFormatter.formatWithinAttack(
        point.atEpochMillis,
        details.startEpochMillis,
        details.startZoneId
    )
    val removeDescription = stringResource(R.string.pain_remove, point.value, time)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(start = 4.dp)
    ) {
        Text(
            text = stringResource(R.string.pain_entry, time, point.value),
            style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = MigrelyIcons.Close,
                contentDescription = removeDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * A pain level from 1 to 10 at a time inside the attack. The slider has the platform's accessibility actions (BRD
 * EC-UX-04); the time picker only offers times inside the attack (BRD EC-ATK-09).
 */
@Composable
private fun AddPainDialog(
    details: AttackDetailsUiModel,
    timeUiFormatter: AttackTimeUiFormatter,
    onAdd: (IntensityPointUiModel) -> Unit,
    onDismiss: () -> Unit
) {
    // "Now" is read when the dialog opens, so a screen left open for a while still offers the current time.
    val openedAt = rememberSaveable { System.currentTimeMillis() }
    val latest = details.endEpochMillis ?: (openedAt + CLOCK_TOLERANCE_MILLIS)
    var level by rememberSaveable { mutableFloatStateOf(DEFAULT_LEVEL) }
    var at by rememberSaveable {
        mutableLongStateOf(
            if (details.endEpochMillis ==
                null
            ) {
                openedAt
            } else {
                details.startEpochMillis
            }
        )
    }
    var pickingTime by rememberSaveable { mutableStateOf(false) }
    val value = level.roundToInt()
    val levelName = stringResource(
        when {
            value <= MILD_UP_TO -> R.string.pain_level_mild
            value <= MODERATE_UP_TO -> R.string.pain_level_moderate
            else -> R.string.pain_level_severe
        }
    )
    val timeText = timeUiFormatter.formatWithinAttack(
        at,
        details.startEpochMillis,
        details.startZoneId
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pain_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "${stringResource(R.string.pain_dialog_value, value)} · $levelName",
                    style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                )
                Slider(
                    value = level,
                    onValueChange = { level = it },
                    valueRange = MINIMUM_LEVEL..MAXIMUM_LEVEL,
                    steps = SLIDER_STEPS,
                    modifier = Modifier.semantics { stateDescription = "$value, $levelName" }
                )
                val changeTime = stringResource(R.string.pain_dialog_change_time, timeText)
                TextButton(
                    onClick = { pickingTime = true },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .semantics { contentDescription = changeTime }
                ) {
                    Text(stringResource(R.string.pain_dialog_time, timeText))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onAdd(IntensityPointUiModel(at, value)) }) {
                Text(stringResource(R.string.pain_dialog_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.attack_picker_cancel)) }
        }
    )
    if (pickingTime) {
        MigrelyDateTimePicker(
            initialEpochMillis = at,
            minEpochMillis = details.startEpochMillis,
            maxEpochMillis = latest,
            zoneId = zoneOf(details.startZoneId),
            labels = pickerLabels(R.string.picker_point_title, R.string.picker_point_out_of_range),
            onConfirm = { picked ->
                at = picked
                pickingTime = false
            },
            onDismiss = { pickingTime = false }
        )
    }
}

/**
 * Shown the first time the user records a 10 (BRD SAF-F2). It never blocks the screen: the rest stays usable and the
 * card can be dismissed.
 */
@Composable
internal fun SafetyCard(onDismiss: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .semantics { liveRegion = LiveRegionMode.Assertive }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = MigrelyIcons.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.safety_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
            Text(
                text = stringResource(R.string.safety_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(top = 8.dp)
            )
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(
                    stringResource(R.string.safety_dismiss),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}
