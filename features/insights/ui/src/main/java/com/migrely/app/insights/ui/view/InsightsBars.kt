package com.migrely.app.insights.ui.view

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.migrely.app.insights.ui.R
import com.migrely.app.insights.ui.model.InsightsTimeOfDayUiModel
import java.time.DayOfWeek

private val BAR_HEIGHT = 10.dp

/**
 * One row of a chart: its label and value, the bar beneath, and an optional line of details. A screen reader reads the
 * row as one item, [description], which holds every number in it (BRD D90, D92).
 */
@Composable
internal fun BarRow(
    label: String,
    value: String,
    fraction: Float,
    description: String,
    details: String? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clearAndSetSemantics { contentDescription = description }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
        Bar(fraction, Modifier.fillMaxWidth().height(BAR_HEIGHT))
        if (details != null) {
            Text(
                text = details,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** A rounded track with its filled share, growing from the start side in either direction. */
@Composable
private fun Bar(fraction: Float, modifier: Modifier = Modifier) {
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val fill = MaterialTheme.colorScheme.primary
    Canvas(modifier) {
        val radius = CornerRadius(size.height / 2)
        drawRoundRect(color = track, cornerRadius = radius)
        val width = size.width * fraction.coerceIn(0f, 1f)
        if (width > 0f) {
            val left = if (layoutDirection == LayoutDirection.Rtl) size.width - width else 0f
            drawRoundRect(
                color = fill,
                topLeft = Offset(left, 0f),
                size = Size(width, size.height),
                cornerRadius = radius
            )
        }
    }
}

@StringRes
internal fun DayOfWeek.label(): Int = when (this) {
    DayOfWeek.MONDAY -> R.string.insights_monday
    DayOfWeek.TUESDAY -> R.string.insights_tuesday
    DayOfWeek.WEDNESDAY -> R.string.insights_wednesday
    DayOfWeek.THURSDAY -> R.string.insights_thursday
    DayOfWeek.FRIDAY -> R.string.insights_friday
    DayOfWeek.SATURDAY -> R.string.insights_saturday
    DayOfWeek.SUNDAY -> R.string.insights_sunday
}

/** "Most often on Mondays: %1$d of %2$d" (BRD D90). */
@StringRes
internal fun DayOfWeek.busiest(): Int = when (this) {
    DayOfWeek.MONDAY -> R.string.insights_busiest_monday
    DayOfWeek.TUESDAY -> R.string.insights_busiest_tuesday
    DayOfWeek.WEDNESDAY -> R.string.insights_busiest_wednesday
    DayOfWeek.THURSDAY -> R.string.insights_busiest_thursday
    DayOfWeek.FRIDAY -> R.string.insights_busiest_friday
    DayOfWeek.SATURDAY -> R.string.insights_busiest_saturday
    DayOfWeek.SUNDAY -> R.string.insights_busiest_sunday
}

@StringRes
internal fun InsightsTimeOfDayUiModel.label(): Int = when (this) {
    InsightsTimeOfDayUiModel.Night -> R.string.insights_night
    InsightsTimeOfDayUiModel.Morning -> R.string.insights_morning
    InsightsTimeOfDayUiModel.Afternoon -> R.string.insights_afternoon
    InsightsTimeOfDayUiModel.Evening -> R.string.insights_evening
}

@StringRes
internal fun InsightsTimeOfDayUiModel.busiest(): Int = when (this) {
    InsightsTimeOfDayUiModel.Night -> R.string.insights_busiest_night
    InsightsTimeOfDayUiModel.Morning -> R.string.insights_busiest_morning
    InsightsTimeOfDayUiModel.Afternoon -> R.string.insights_busiest_afternoon
    InsightsTimeOfDayUiModel.Evening -> R.string.insights_busiest_evening
}
