package com.migrely.app.medication.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.input.TapGuard
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.medication.presentation.model.MedicationPromptViewState
import com.migrely.app.medication.ui.R
import com.migrely.app.medication.ui.di.MedicationPromptDependencies
import com.migrely.app.medication.ui.mapper.MedicationTimeUiFormatter
import com.migrely.app.medication.ui.model.IntakeEffectUiModel
import com.migrely.app.medication.ui.model.MedicationPromptUiModel
import com.migrely.app.medication.ui.model.OveruseCategoryUiModel
import com.migrely.app.medication.ui.model.OveruseLevelUiModel
import java.time.ZoneId
import java.util.Locale

/** A new card ignores taps this long, so the second tap of a double tap never answers the next question. */
private const val NEW_CARD_TAP_GUARD_MILLIS = 1_000L
private const val ONE_ANSWER_A_ROW_FONT_SCALE = 1.5f
private const val NO_BREAK_SPACE = '\u00A0'

/**
 * Home's one medication card under today's card (BRD D46): an overuse warning (MED-F5, D45) or "Did it help?" (MED-F4,
 * D39). It loads whenever [refreshKey] changes, which home does every time it loads.
 */
@Composable
fun MedicationPromptDependencies.MedicationPrompt(refreshKey: Int, modifier: Modifier = Modifier) {
    LaunchedEffect(refreshKey) { medicationPromptViewModel.onEnter() }
    val viewState by medicationPromptViewModel.viewState.collectAsState(
        MedicationPromptViewState.Hidden
    )
    val prompt by rememberSaveable(viewState) {
        mutableStateOf(medicationPromptUiMapper.toUi(viewState))
    }
    val toPresentation = medicationPromptUiToPresentationMapper
    MedicationPromptContents(
        prompt = prompt,
        timeUiFormatter = medicationTimeUiFormatter,
        actions = MedicationPromptActions(
            onEffect = { intakeId, effect ->
                medicationPromptViewModel.onEffectAction(
                    intakeId,
                    toPresentation.toPresentation(effect)
                )
            },
            onSkip = medicationPromptViewModel::onSkipEffectAction,
            onSeen = { category, level ->
                medicationPromptViewModel.onOveruseSeenAction(
                    toPresentation.toPresentation(category),
                    toPresentation.toPresentation(level)
                )
            }
        ),
        modifier = modifier
    )
}

data class MedicationPromptActions(
    val onEffect: (intakeId: String, effect: IntakeEffectUiModel) -> Unit,
    val onSkip: (intakeId: String) -> Unit,
    val onSeen: (category: OveruseCategoryUiModel, level: OveruseLevelUiModel) -> Unit
)

/** Holds the card shown last without being snapshot state, so reading it never recomposes. */
private class ShownCard(var key: Any?)

@Composable
fun MedicationPromptContents(
    prompt: MedicationPromptUiModel,
    timeUiFormatter: MedicationTimeUiFormatter,
    actions: MedicationPromptActions,
    modifier: Modifier = Modifier
) {
    val key = prompt.cardKey() ?: return
    val shown = remember { ShownCard(null) }
    val guardTaps = remember(key) { shown.key != null && shown.key != key }
    SideEffect { shown.key = key }
    Box(modifier = modifier.padding(top = 12.dp)) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            when (prompt) {
                is MedicationPromptUiModel.EffectQuestion -> EffectQuestion(
                    prompt,
                    timeUiFormatter,
                    actions
                )
                is MedicationPromptUiModel.OveruseWarning -> OveruseWarning(prompt, actions)
                MedicationPromptUiModel.Hidden -> Unit
            }
        }
        if (guardTaps) {
            TapGuard(
                key = key,
                durationMillis = NEW_CARD_TAP_GUARD_MILLIS,
                Modifier.matchParentSize()
            )
        }
    }
}

private fun MedicationPromptUiModel.cardKey(): Any? = when (this) {
    MedicationPromptUiModel.Hidden -> null
    is MedicationPromptUiModel.EffectQuestion -> intakeId
    is MedicationPromptUiModel.OveruseWarning -> category to level
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EffectQuestion(
    question: MedicationPromptUiModel.EffectQuestion,
    timeUiFormatter: MedicationTimeUiFormatter,
    actions: MedicationPromptActions
) {
    val time = timeUiFormatter.formatTime(
        question.takenAtEpochMillis,
        question.zoneId,
        System.currentTimeMillis()
    )
    // "50 mg" never breaks between the number and its unit at large font sizes.
    val taken = question.dose
        ?.let {
            stringResource(R.string.prompt_effect_taken_dose, time, it.replace(' ', NO_BREAK_SPACE))
        }
        ?: stringResource(R.string.prompt_effect_taken, time)
    val spokenTaken = question.dose
        ?.let { stringResource(R.string.prompt_effect_taken_dose_spoken, time, it) }
        ?: taken
    Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 4.dp, bottom = 16.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.prompt_effect_title, question.medicationName),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = taken,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .semantics { contentDescription = spokenTaken }
                )
            }
            IconButton(onClick = { actions.onSkip(question.intakeId) }) {
                Icon(
                    MigrelyIcons.Close,
                    contentDescription = stringResource(R.string.prompt_effect_skip)
                )
            }
        }
        // Two equal answers a row, or one a row at large font sizes, so no label is cut off (BRD EC-UX-03).
        val largeFont = LocalDensity.current.fontScale >= ONE_ANSWER_A_ROW_FONT_SCALE
        val answersPerRow = if (largeFont) 1 else 2
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = answersPerRow,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, end = 12.dp)
        ) {
            IntakeEffectUiModel.entries.forEach { effect ->
                OutlinedButton(
                    onClick = { actions.onEffect(question.intakeId, effect) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                ) {
                    Text(
                        stringResource(effect.label()),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun OveruseWarning(
    warning: MedicationPromptUiModel.OveruseWarning,
    actions: MedicationPromptActions
) {
    Column(modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 8.dp, bottom = 8.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                MigrelyIcons.Information,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(20.dp)
            )
            Text(
                text = pluralStringResource(warning.category.title(), warning.days, warning.days),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(start = 12.dp, end = 8.dp)
                    .semantics { heading() }
            )
        }
        // Informative, never a diagnosis (BRD MED-F5, section 11).
        Text(
            text = when (warning.level) {
                OveruseLevelUiModel.Soft ->
                    stringResource(R.string.overuse_body_soft, warning.thresholdDays)
                OveruseLevelUiModel.Doctor -> stringResource(R.string.overuse_body_doctor)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 32.dp, top = 4.dp, end = 8.dp)
        )
        TextButton(
            onClick = { actions.onSeen(warning.category, warning.level) },
            modifier = Modifier
                .align(Alignment.End)
                .heightIn(min = 48.dp)
        ) {
            Text(
                stringResource(R.string.overuse_got_it),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

private fun IntakeEffectUiModel.label() = when (this) {
    IntakeEffectUiModel.NotAtAll -> R.string.effect_not_at_all
    IntakeEffectUiModel.ALittle -> R.string.effect_a_little
    IntakeEffectUiModel.ALot -> R.string.effect_a_lot
    IntakeEffectUiModel.Completely -> R.string.effect_completely
}

private fun OveruseCategoryUiModel.title() = when (this) {
    OveruseCategoryUiModel.SimplePainkiller -> R.plurals.overuse_title_simple
    OveruseCategoryUiModel.Triptan -> R.plurals.overuse_title_triptan
    OveruseCategoryUiModel.CombinationOrOther -> R.plurals.overuse_title_other
    OveruseCategoryUiModel.Multiple -> R.plurals.overuse_title_multiple
}

private val previewFormatter = MedicationTimeUiFormatter(
    locale = { Locale.US },
    is24HourFormat = { false },
    deviceZone = { ZoneId.of("Asia/Tashkent") },
    bestDateTimePattern = { _, skeleton -> if (skeleton == "hm") "h:mm a" else "EEE, MMM d" }
)

private val previewActions = MedicationPromptActions({ _, _ -> }, {}, { _, _ -> })

@Preview(name = "Did it help?")
@Composable
private fun EffectQuestionPreview() {
    MigrelyTheme {
        MedicationPromptContents(
            prompt = MedicationPromptUiModel.EffectQuestion(
                "i",
                "Ibuprofen",
                "400 mg",
                System.currentTimeMillis() - 3 * 3_600_000L,
                "Asia/Tashkent"
            ),
            timeUiFormatter = previewFormatter,
            actions = previewActions
        )
    }
}

@Preview(name = "Overuse, doctor")
@Composable
private fun OveruseDoctorPreview() {
    MigrelyTheme {
        MedicationPromptContents(
            prompt = MedicationPromptUiModel.OveruseWarning(
                OveruseCategoryUiModel.Triptan,
                OveruseLevelUiModel.Doctor,
                days = 10,
                thresholdDays = 10
            ),
            timeUiFormatter = previewFormatter,
            actions = previewActions
        )
    }
}
