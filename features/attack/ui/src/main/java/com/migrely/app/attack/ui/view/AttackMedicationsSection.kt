package com.migrely.app.attack.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.migrely.app.attack.ui.R
import com.migrely.app.attack.ui.mapper.AttackTimeUiFormatter
import com.migrely.app.attack.ui.model.AttackDetailsUiModel
import com.migrely.app.attack.ui.model.AttackIntakeEffectUiModel
import com.migrely.app.attack.ui.model.AttackIntakeUiModel
import com.migrely.app.designsystem.icon.MigrelyIcons

/**
 * The medications taken during the attack, oldest first. Each row shows how much it helped and opens that choice
 * (BRD ATK-F2, EC-MED-12, D39), and each is removable with undo. "Add medication" opens "Medication taken" for this
 * attack (BRD ATK-F5, D34). Archived medications keep their name (EC-MED-01).
 */
@Composable
internal fun MedicationsSection(
    details: AttackDetailsUiModel,
    timeUiFormatter: AttackTimeUiFormatter,
    actions: AttackDetailsActions
) {
    // The id of the intake whose answer is being chosen, so a rotation keeps the dialog.
    var choosingFor by rememberSaveable { mutableStateOf<String?>(null) }
    Column {
        SectionTitle(stringResource(R.string.section_medications))
        if (details.intakes.isEmpty()) {
            Text(
                text = stringResource(R.string.medication_none),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        details.intakes.forEach { intake ->
            IntakeRow(
                intake = intake,
                time = timeUiFormatter.formatWithinAttack(
                    intake.takenAtEpochMillis,
                    details.startEpochMillis,
                    intake.zoneId
                ),
                onChoose = { choosingFor = intake.id },
                onRemove = { actions.onRemoveIntake(intake) }
            )
        }
        OutlinedButton(
            onClick = actions.onAddMedication,
            modifier = Modifier
                .padding(top = 8.dp)
                .heightIn(min = 48.dp)
        ) {
            Icon(MigrelyIcons.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(
                text = stringResource(R.string.medication_add),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
    details.intakes.firstOrNull { it.id == choosingFor }?.let { intake ->
        EffectDialog(
            intake = intake,
            onChoose = { effect ->
                choosingFor = null
                actions.onIntakeEffect(intake, effect)
            },
            onDismiss = { choosingFor = null }
        )
    }
}

@Composable
private fun IntakeRow(
    intake: AttackIntakeUiModel,
    time: String,
    onChoose: () -> Unit,
    onRemove: () -> Unit
) {
    val what = listOfNotNull(intake.medicationName, intake.dose).joinToString(" ")
    val helped = intake.effect?.let { stringResource(it.helpedLabel()) }
    val description = if (helped == null) {
        stringResource(R.string.medication_row_description, what, time)
    } else {
        stringResource(R.string.medication_row_description_answered, what, time, helped)
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(12.dp))
                // Merged into the clickable, so the row stays focusable for keyboards and switch access (EC-UX-19).
                .semantics { contentDescription = description }
                .clickable(role = Role.Button, onClick = onChoose)
                .padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.medication_row, what, time),
                style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clearAndSetSemantics { }
            )
            if (helped != null) {
                Text(
                    text = helped,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clearAndSetSemantics { }
                )
            }
        }
        IconButton(onClick = onRemove) {
            Icon(
                MigrelyIcons.Close,
                contentDescription = stringResource(R.string.medication_remove, what)
            )
        }
    }
}

@Composable
private fun EffectDialog(
    intake: AttackIntakeUiModel,
    onChoose: (AttackIntakeEffectUiModel) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.medication_effect_title, intake.medicationName)) },
        text = {
            Column(Modifier.selectableGroup()) {
                AttackIntakeEffectUiModel.entries.forEach { effect ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .selectable(
                                selected = intake.effect == effect,
                                role = Role.RadioButton,
                                onClick = { onChoose(effect) }
                            )
                    ) {
                        RadioButton(selected = intake.effect == effect, onClick = null)
                        Text(
                            text = stringResource(effect.answerLabel()),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.picker_cancel)) }
        }
    )
}

private fun AttackIntakeEffectUiModel.answerLabel() = when (this) {
    AttackIntakeEffectUiModel.NotAtAll -> R.string.medication_effect_not_at_all
    AttackIntakeEffectUiModel.ALittle -> R.string.medication_effect_a_little
    AttackIntakeEffectUiModel.ALot -> R.string.medication_effect_a_lot
    AttackIntakeEffectUiModel.Completely -> R.string.medication_effect_completely
}

private fun AttackIntakeEffectUiModel.helpedLabel() = when (this) {
    AttackIntakeEffectUiModel.NotAtAll -> R.string.medication_helped_not_at_all
    AttackIntakeEffectUiModel.ALittle -> R.string.medication_helped_a_little
    AttackIntakeEffectUiModel.ALot -> R.string.medication_helped_a_lot
    AttackIntakeEffectUiModel.Completely -> R.string.medication_helped_completely
}
