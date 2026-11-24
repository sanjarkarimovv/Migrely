package com.migrely.app.attack.ui.view

import android.content.res.Configuration
import android.view.ViewTreeObserver
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.migrely.app.attack.ui.R
import com.migrely.app.attack.ui.model.AttackDetailsUiModel
import com.migrely.app.attack.ui.model.AttackTagUiModel
import com.migrely.app.attack.ui.model.HeadLocationUiModel
import com.migrely.app.attack.ui.model.SymptomUiModel
import com.migrely.app.attack.ui.model.TagKindUiModel
import com.migrely.app.attack.ui.model.TriggerUiModel
import com.migrely.app.designsystem.dialog.MigrelyDropdownMenu
import com.migrely.app.designsystem.dialog.MigrelyScrollingAlertDialog
import com.migrely.app.designsystem.input.limitedEdit
import com.migrely.app.designsystem.layout.isCompactHeight
import kotlinx.coroutines.delay

private const val MAXIMUM_TAG_NAME_LENGTH = 40

/** How long an intent to type waits for an on-screen keyboard, with room for a slow one under load (design D10 item 6). */
private const val KEYBOARD_WAIT_MILLIS = 3_000L

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
    // TalkBack says "Double-tap and hold to" before the long press's label (T23);
    // the actions menu shows removeLabel alone.
    val removeAction = stringResource(R.string.custom_remove_from_list_action)
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
                    onLongClickLabel = if (onArchive != null) removeAction else null,
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
                    // On the chip's own node, so a chip cut by the page's edge still says it (#255, BRD D193).
                    contentDescription = spokenLabel ?: label
                }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // One spoken name (BRD D181 (2)): the chip says it, so the shown text, or "+ Your own" for
                // "Add your own symptom", is left out of the semantics.
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = content,
                    modifier = Modifier.clearAndSetSemantics { }
                )
            }
        }
        if (onArchive != null) {
            MigrelyDropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
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

/**
 * The custom tag dialog's name field, under the title in one area that scrolls (T23 8.1 row R03,
 * [MigrelyScrollingAlertDialog]). At 200% in landscape, German's buttons stack and leave the dialog little height: the
 * title then scrolls away before the field is cut, so the field and its counter can always be brought into view whole.
 * On opening, on focus, while the keyboard moves and at each new [visibleHeight] of the area, the whole field, its
 * floating label included, is brought into view, so a short dialog never opens on a title alone.
 */
@Composable
internal fun CustomTagNameField(
    name: TextFieldValue,
    visibleHeight: Int,
    onTypingIntent: () -> Unit = {},
    onKeyboardChange: (Boolean) -> Unit = {},
    onNameChange: (TextFieldValue) -> Unit
) {
    val fieldInView = remember { BringIntoViewRequester() }
    val fieldWithRoomInView = remember { BringIntoViewRequester() }
    var focused by remember { mutableStateOf(false) }
    // A completed tap on the field is an intent to type: after Back closed the keyboard the field keeps focus, and the
    // next tap raises the keyboard again. A drag to scroll and a long press to paste end the press as Cancel, not
    // Release, so they are not (design D10 item 6).
    val fieldInteractions = remember { MutableInteractionSource() }
    val currentOnTypingIntent by rememberUpdatedState(onTypingIntent)
    LaunchedEffect(fieldInteractions) {
        fieldInteractions.interactions.collect {
            if (it is PressInteraction.Release) currentOnTypingIntent()
        }
    }
    // From the root insets: a dialog window that fits its system bars shows Compose no keyboard inset.
    val view = LocalView.current
    var keyboard by remember { mutableStateOf(false) }
    val currentOnKeyboardChange by rememberUpdatedState(onKeyboardChange)
    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val insets = ViewCompat.getRootWindowInsets(view)
            val up = insets?.isVisible(WindowInsetsCompat.Type.ime()) == true
            if (up != keyboard) {
                keyboard = up
                currentOnKeyboardChange(up)
            }
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose { view.viewTreeObserver.removeOnGlobalLayoutListener(listener) }
    }
    // The dialog settles over a few frames and the keyboard resizes it, so each new visible height asks again.
    LaunchedEffect(focused, keyboard, visibleHeight) {
        // After a frame, so the field has been laid out and its place is known.
        withFrameNanos { }
        // Typing: the room around the field too (the floating label's overhang, the counter's glyphs). At rest: the
        // field alone, so a title that fits is not pushed off by a few pixels.
        val typing = focused || keyboard
        if (typing) fieldWithRoomInView.bringIntoView() else fieldInView.bringIntoView()
    }
    // The space around the field is brought into view with it: above, the floating label's overhang; below,
    // the counter's glyphs, taller than their line at large fonts.
    Box(
        Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(fieldWithRoomInView)
            .padding(vertical = 4.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.custom_dialog_label)) },
            supportingText = {
                Text(stringResource(R.string.custom_dialog_counter, name.text.length))
            },
            singleLine = true,
            interactionSource = fieldInteractions,
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(fieldInView)
                .onFocusChanged {
                    if (it.isFocused && !focused) currentOnTypingIntent()
                    focused = it.isFocused
                }
        )
    }
}

/**
 * When the custom tag dialog keeps the platform's narrow width (T23 8.1 row R03, design D10 item 6): from an intent to
 * type (a completed tap on the field, or the field gaining focus), before the keyboard rises, so Android pans the dialog
 * and the typed line stays above the keyboard, and while the keyboard is up. Once the keyboard closes the dialog is wide
 * again, even with the field still focused. When no keyboard is up when [KeyboardWait] ends after the latest intent, it
 * is wide again too: narrowing is only ever for an on-screen keyboard. While a hardware keyboard is attached
 * ([HardwareKeyboardWatch]) an intent to type does not narrow it, as no on-screen keyboard is expected; one that rises
 * anyway still narrows it, and its closing widens it (design D10 item 6, cr-de finding 2).
 */
internal class KeyboardWidthRule {
    var narrow by mutableStateOf(false)
        private set

    var typingIntents by mutableIntStateOf(0)
        private set

    private var keyboardUp = false
    private var hardwareKeyboard = false

    fun onTypingIntent() {
        if (hardwareKeyboard) return
        narrow = true
        typingIntents++
    }

    fun onHardwareKeyboard(attached: Boolean) {
        hardwareKeyboard = attached
        if (attached && !keyboardUp) narrow = false
    }

    fun onKeyboard(up: Boolean) {
        keyboardUp = up
        narrow = up
    }

    fun onKeyboardWaitOver() {
        if (!keyboardUp) narrow = false
    }
}

/** Each intent to type starts the wait again; a keyboard that has not risen by its end is not coming. */
@Composable
internal fun KeyboardWait(widthRule: KeyboardWidthRule) {
    LaunchedEffect(widthRule.typingIntents) {
        if (widthRule.typingIntents > 0) {
            delay(KEYBOARD_WAIT_MILLIS)
            widthRule.onKeyboardWaitOver()
        }
    }
}

/**
 * Whether the custom tag dialog takes the wide width: in a short window, where the default width stacks the buttons and
 * hides the field (T23 8.1 row R03), except while it is narrow for an on-screen keyboard ([KeyboardWidthRule]).
 */
@Composable
internal fun customTagDialogWide(widthRule: KeyboardWidthRule): Boolean =
    isCompactHeight() && !widthRule.narrow

/** Tells [widthRule] whether a hardware keyboard is attached, as the configuration says now. */
@Composable
internal fun HardwareKeyboardWatch(widthRule: KeyboardWidthRule) {
    val attached =
        LocalConfiguration.current.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO
    LaunchedEffect(attached) { widthRule.onHardwareKeyboard(attached) }
}

/** The user's own symptom or trigger: 1 to 40 characters, input stops at 40 (BRD EC-ATK-12). */
@Composable
internal fun CustomTagDialog(kind: TagKindUiModel, onAdd: (String) -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }
    val title = stringResource(
        when (kind) {
            TagKindUiModel.Symptom -> R.string.custom_dialog_symptom_title
            TagKindUiModel.Trigger -> R.string.custom_dialog_trigger_title
        }
    )
    val widthRule = remember { KeyboardWidthRule() }
    HardwareKeyboardWatch(widthRule)
    KeyboardWait(widthRule)
    // The area's height, so the field is brought into view again as the dialog settles and the keyboard resizes it.
    var visibleHeight by remember { mutableIntStateOf(0) }
    MigrelyScrollingAlertDialog(
        title = title,
        onDismissRequest = onDismiss,
        areaModifier = Modifier.onSizeChanged { visibleHeight = it.height },
        content = {
            CustomTagNameField(
                name = name,
                visibleHeight = visibleHeight,
                onTypingIntent = widthRule::onTypingIntent,
                onKeyboardChange = widthRule::onKeyboard
            ) { name = limitedEdit(name, it, MAXIMUM_TAG_NAME_LENGTH) }
        },
        wide = customTagDialogWide(widthRule),
        confirmButton = {
            TextButton(onClick = { onAdd(name.text.trim()) }, enabled = name.text.isNotBlank()) {
                Text(stringResource(R.string.custom_dialog_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.attack_picker_cancel)) }
        }
    )
}
