package com.migrely.app.attack.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.migrely.app.attack.ui.R
import com.migrely.app.attack.ui.model.AttackDetailsUiModel
import com.migrely.app.attack.ui.model.AttackTagUiModel
import com.migrely.app.attack.ui.model.HeadLocationUiModel
import com.migrely.app.attack.ui.model.SymptomUiModel
import com.migrely.app.attack.ui.model.TagKindUiModel
import com.migrely.app.attack.ui.model.TriggerUiModel

private const val MAXIMUM_TAG_NAME_LENGTH = 40

/** One choice as the chips show it: what to send, how to label it, and the custom tag id when it is the user's own. */
private data class Choice(val tag: AttackTagUiModel, val label: String, val customTagId: String?)

/**
 * Where it hurts, symptoms and triggers (BRD ATK-F5). A tap flips the chip at once and saves it; the shown state
 * follows the tap until the saved state catches up, so quick double taps never lose a change.
 */
@Composable
internal fun ChoiceSections(
    details: AttackDetailsUiModel,
    labels: AttackLabels,
    actions: AttackDetailsActions
) {
    val pending = remember(details.id) { mutableStateMapOf<AttackTagUiModel, Boolean>() }
    val selected = buildSet {
        details.selectedLocations.forEach { add(AttackTagUiModel.Location(it)) }
        details.selectedSymptoms.forEach { add(AttackTagUiModel.Symptom(it)) }
        details.selectedTriggers.forEach { add(AttackTagUiModel.Trigger(it)) }
    }
    LaunchedEffect(selected) { pending.keys.removeAll { tag -> pending[tag] == (tag in selected) } }
    fun isSelected(tag: AttackTagUiModel) = pending[tag] ?: (tag in selected)
    val onToggle: (AttackTagUiModel) -> Unit = { tag ->
        val newValue = !isSelected(tag)
        pending[tag] = newValue
        actions.onTag(tag, newValue)
    }
    var addingKind by rememberSaveable(details.id) { mutableStateOf<TagKindUiModel?>(null) }

    ChoiceGroup(
        title = stringResource(R.string.section_location),
        choices = HeadLocationUiModel.entries.map { location ->
            Choice(
                AttackTagUiModel.Location(location),
                stringResource(
                    labels.location
                        .toUi(location)
                ),
                null
            )
        },
        isSelected = ::isSelected,
        onToggle = onToggle,
        onArchive = actions.onArchiveCustomTag,
        addLabel = null,
        onAdd = {}
    )
    ChoiceGroup(
        title = stringResource(R.string.section_symptoms),
        choices = details.symptomChoices.map { symptom ->
            when (symptom) {
                is SymptomUiModel.BuiltIn ->
                    Choice(
                        AttackTagUiModel.Symptom(symptom),
                        stringResource(
                            labels.symptom
                                .toUi(symptom.symptom)
                        ),
                        null
                    )

                is SymptomUiModel.Custom ->
                    Choice(AttackTagUiModel.Symptom(symptom), symptom.tag.name, symptom.tag.id)
            }
        },
        isSelected = ::isSelected,
        onToggle = onToggle,
        onArchive = actions.onArchiveCustomTag,
        addLabel = stringResource(R.string.custom_add_symptom),
        onAdd = { addingKind = TagKindUiModel.Symptom }
    )
    ChoiceGroup(
        title = stringResource(R.string.section_triggers),
        choices = details.triggerChoices.map { trigger ->
            when (trigger) {
                is TriggerUiModel.BuiltIn ->
                    Choice(
                        AttackTagUiModel.Trigger(trigger),
                        stringResource(
                            labels.trigger
                                .toUi(trigger.trigger)
                        ),
                        null
                    )

                is TriggerUiModel.Custom ->
                    Choice(AttackTagUiModel.Trigger(trigger), trigger.tag.name, trigger.tag.id)
            }
        },
        isSelected = ::isSelected,
        onToggle = onToggle,
        onArchive = actions.onArchiveCustomTag,
        addLabel = stringResource(R.string.custom_add_trigger),
        onAdd = { addingKind = TagKindUiModel.Trigger }
    )
    addingKind?.let { kind ->
        CustomTagDialog(
            kind = kind,
            onAdd = { name ->
                addingKind = null
                actions.onAddCustomTag(kind, name)
            },
            onDismiss = { addingKind = null }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Suppress("LongParameterList")
@Composable
private fun ChoiceGroup(
    title: String,
    choices: List<Choice>,
    isSelected: (AttackTagUiModel) -> Boolean,
    onToggle: (AttackTagUiModel) -> Unit,
    onArchive: (tagId: String) -> Unit,
    addLabel: String?,
    onAdd: () -> Unit
) {
    Column {
        SectionTitle(title)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            choices.forEach { choice ->
                ChoiceChip(
                    label = choice.label,
                    selected = isSelected(choice.tag),
                    onClick = { onToggle(choice.tag) },
                    onArchive = choice.customTagId?.let { id -> { onArchive(id) } }
                )
            }
            if (addLabel != null) {
                ChoiceChip(
                    label = stringResource(R.string.custom_add_chip),
                    selected = false,
                    onClick = onAdd,
                    onArchive = null,
                    spokenLabel = addLabel,
                    isAction = true
                )
            }
        }
    }
}

/**
 * A choice chip. The user's own tags also offer "Remove from my list" through a long press and as an accessibility
 * action (BRD EC-ATK-11). Selection is announced as "selected" (BRD EC-UX-04).
 */
@Suppress("LongParameterList")
@Composable
private fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    onArchive: (() -> Unit)?,
    spokenLabel: String? = null,
    isAction: Boolean = false
) {
    var menuOpen by remember { mutableStateOf(false) }
    val removeLabel = stringResource(R.string.custom_remove_from_list)
    val colors = MaterialTheme.colorScheme
    val container = if (selected) colors.secondaryContainer else colors.surface
    val border = if (selected) colors.secondary else colors.outline
    val content = when {
        selected -> MaterialTheme.colorScheme.onSecondaryContainer
        isAction -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = container,
            border = BorderStroke(if (selected) 1.5.dp else 1.dp, border),
            modifier = Modifier
                .heightIn(min = 48.dp)
                .combinedClickable(
                    role = if (isAction) Role.Button else null,
                    onLongClickLabel = if (onArchive != null) removeLabel else null,
                    onLongClick = onArchive?.let { { menuOpen = true } },
                    onClick = onClick
                )
                .semantics {
                    // Announced as "Aura, selected" (spec: attack-details, accessible details screen).
                    if (!isAction) this.selected = selected
                    if (onArchive != null) {
                        customActions = listOf(
                            CustomAccessibilityAction(removeLabel) { onArchive().let { true } }
                        )
                    }
                    if (spokenLabel != null) contentDescription = spokenLabel
                }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(text = label, style = MaterialTheme.typography.bodyMedium, color = content)
            }
        }
        if (onArchive != null) {
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text(removeLabel) },
                    onClick = {
                        menuOpen = false
                        onArchive()
                    }
                )
            }
        }
    }
}

/** The user's own symptom or trigger: 1 to 40 characters, input stops at 40 (BRD EC-ATK-12). */
@Composable
private fun CustomTagDialog(kind: TagKindUiModel, onAdd: (String) -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    when (kind) {
                        TagKindUiModel.Symptom -> R.string.custom_dialog_symptom_title
                        TagKindUiModel.Trigger -> R.string.custom_dialog_trigger_title
                    }
                )
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(MAXIMUM_TAG_NAME_LENGTH) },
                label = { Text(stringResource(R.string.custom_dialog_label)) },
                supportingText = {
                    Text(stringResource(R.string.custom_dialog_counter, name.length))
                },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onAdd(name.trim()) }, enabled = name.isNotBlank()) {
                Text(stringResource(R.string.custom_dialog_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.attack_picker_cancel)) }
        }
    )
}
