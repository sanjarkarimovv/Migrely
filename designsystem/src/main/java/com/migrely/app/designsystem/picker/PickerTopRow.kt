package com.migrely.app.designsystem.picker

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp

/**
 * A full-screen picker's top row: Cancel, the title and Done (BRD D127). When the title's longest word does not fit
 * between the two buttons, as German "Nächster Termin" at 200% on a 360 dp phone, the title moves to its own line under
 * them instead of breaking inside a word (T23 8.1 row R02, D181). Type never shrinks.
 */
@Composable
internal fun PickerTopRow(
    title: String,
    cancel: @Composable () -> Unit,
    confirm: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val titleText: @Composable () -> Unit = {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .semantics { heading() }
        )
    }
    Layout(contents = listOf(cancel, titleText, confirm), modifier = modifier) {
            measurables,
            constraints
        ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val cancelPlaceable = measurables[0].first().measure(loose)
        val confirmPlaceable = measurables[2].first().measure(loose)
        val titleMeasurable = measurables[1].first()
        val width = constraints.maxWidth
        val room = (width - cancelPlaceable.width - confirmPlaceable.width).coerceAtLeast(0)
        val fitsBetween = titleMeasurable.minIntrinsicWidth(Constraints.Infinity) <= room
        if (fitsBetween) {
            val titlePlaceable = titleMeasurable.measure(Constraints(maxWidth = room))
            val height =
                maxOf(cancelPlaceable.height, titlePlaceable.height, confirmPlaceable.height)
            layout(width, height) {
                cancelPlaceable.place(0, (height - cancelPlaceable.height) / 2)
                titlePlaceable.place(cancelPlaceable.width, (height - titlePlaceable.height) / 2)
                confirmPlaceable.place(
                    width - confirmPlaceable.width,
                    (height - confirmPlaceable.height) / 2
                )
            }
        } else {
            val titlePlaceable = titleMeasurable.measure(Constraints(maxWidth = width))
            val rowHeight = maxOf(cancelPlaceable.height, confirmPlaceable.height)
            layout(width, rowHeight + titlePlaceable.height) {
                cancelPlaceable.place(0, (rowHeight - cancelPlaceable.height) / 2)
                confirmPlaceable.place(
                    width - confirmPlaceable.width,
                    (rowHeight - confirmPlaceable.height) / 2
                )
                titlePlaceable.place(0, rowHeight)
            }
        }
    }
}
