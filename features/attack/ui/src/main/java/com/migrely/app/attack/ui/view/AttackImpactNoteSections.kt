package com.migrely.app.attack.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.migrely.app.attack.ui.R
import com.migrely.app.attack.ui.model.FunctionalImpactUiModel
import kotlinx.coroutines.delay

private const val MAXIMUM_NOTE_LENGTH = 1_000
private const val NOTE_SAVE_DELAY_MILLIS = 600L

/** At most one impact; tapping the chosen one clears it (BRD ATK-F5, P3). */
@Composable
internal fun ImpactSection(
    impact: FunctionalImpactUiModel?,
    labels: AttackLabels,
    onImpact: (FunctionalImpactUiModel?) -> Unit
) {
    Column(modifier = Modifier.selectableGroup()) {
        SectionTitle(stringResource(R.string.section_impact))
        FunctionalImpactUiModel.entries.forEachIndexed { level, option ->
            val chosen = option == impact
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .selectable(
                        selected = chosen,
                        role = Role.RadioButton,
                        onClick = { onImpact(if (chosen) null else option) }
                    )
            ) {
                RadioButton(selected = chosen, onClick = null)
                Text(
                    text = stringResource(
                        R.string.impact_level,
                        level,
                        stringResource(labels.impact.toUi(option))
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }
    }
}

/**
 * A note of up to 1,000 characters with a counter (BRD EC-ATK-13). Once edited, the text on screen wins over reloads.
 * It is saved shortly after typing stops, when the app goes to the background and when the screen is left, so it
 * survives a back press or the app being killed (BRD ATK-F6, EC-ATK-10).
 */
@Composable
internal fun NoteSection(attackId: String, savedNote: String, onNote: (String) -> Unit) {
    var draft by rememberSaveable(attackId) { mutableStateOf<String?>(null) }
    val currentDraft by rememberUpdatedState(draft)
    val currentSaved by rememberUpdatedState(savedNote)
    val currentOnNote by rememberUpdatedState(onNote)
    fun flush() {
        val text = currentDraft ?: return
        if (text != currentSaved) currentOnNote(text)
    }
    LaunchedEffect(draft) {
        if (draft == null) return@LaunchedEffect
        delay(NOTE_SAVE_DELAY_MILLIS)
        flush()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { flush() }
    DisposableEffect(attackId) { onDispose { flush() } }

    val text = draft ?: savedNote
    Column {
        SectionTitle(stringResource(R.string.section_note))
        OutlinedTextField(
            value = text,
            onValueChange = { draft = it.take(MAXIMUM_NOTE_LENGTH) },
            placeholder = { Text(stringResource(R.string.note_hint)) },
            supportingText = {
                Text(
                    text = stringResource(R.string.note_counter, text.length),
                    style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum")
                )
            },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
