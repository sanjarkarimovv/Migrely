package com.migrely.app.home.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.input.TapGuard
import com.migrely.app.designsystem.layout.MigrelyInsets
import com.migrely.app.designsystem.layout.PageScroll
import com.migrely.app.designsystem.layout.ownWords
import com.migrely.app.designsystem.layout.wordsShownOnly
import com.migrely.app.designsystem.picker.DateTimePickerLabels
import com.migrely.app.designsystem.picker.MigrelyDateTimePicker
import com.migrely.app.designsystem.sky.SkyAppearance
import com.migrely.app.designsystem.sky.SkyCanvas
import com.migrely.app.designsystem.snackbar.clearOfMessage
import com.migrely.app.designsystem.theme.MigrelyColors
import com.migrely.app.designsystem.theme.MigrelyDarkRoom
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.home.ui.R
import com.migrely.app.home.ui.mapper.TimeUiFormatter
import com.migrely.app.home.ui.model.AttackInProgressUiModel
import com.migrely.app.home.ui.model.IntensityLevelUiModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import kotlin.math.roundToInt

private const val MINUTES_PER_HOUR = 60L
private const val MINUTES_PER_DAY = 24 * 60L
private const val MILLIS_PER_MINUTE = 60_000L
private const val STACKED_FONT_SCALE = 1.3f
private const val GUIDANCE_MOVE_GUARD_MILLIS = 500L
private val MOVE_START_OPTIONS = listOf(
    15 to R.string.move_start_15,
    30 to R.string.move_start_30,
    60 to R.string.move_start_60,
    120 to R.string.move_start_120
)

// Fewer title lines than this left for the guidance above the controls make the whole page scroll as one (#274).
private const val GUIDANCE_MIN_LINES = 3

/**
 * The "dark room" (BRD 18.3 B): one line of text, the elapsed time and large controls in the lower half. It is dark in
 * every theme, its time picker included (BRD D94).
 */
@Composable
fun AttackModeContents(
    attack: AttackInProgressUiModel,
    nowMillis: Long,
    reduceMotion: Boolean,
    messageBottom: () -> Dp,
    timeUiFormatter: TimeUiFormatter,
    actions: HomeActions,
    modifier: Modifier = Modifier
) = MigrelyDarkRoom {
    val startedAt = stringResource(
        R.string.attack_started_at,
        timeUiFormatter.formatStartTime(attack.startEpochMillis, attack.startZoneId, nowMillis),
        elapsedText(nowMillis - attack.startEpochMillis)
    )
    val screenModifier = modifier
        .fillMaxSize()
        .background(MigrelyColors.AttackModeBackground)
        .windowInsetsPadding(MigrelyInsets.bars)
        .padding(horizontal = 16.dp)
    // In a short window (landscape) the controls alone fill the height and hide the guidance,
    // so the two sit side by side.
    if (isCompactHeight()) {
        SideBySideAttackLayout(startedAt, attack, nowMillis, messageBottom, actions, screenModifier)
    } else {
        StackedAttackLayout(startedAt, attack, nowMillis, reduceMotion, actions, screenModifier)
    }
}

/**
 * The guidance (the calming line, the start time, Details and the 72-hour card) above the controls. At the largest
 * fonts on a short phone the controls alone can fill the height and leave the guidance no room at all (#274), so when
 * less than [GUIDANCE_MIN_LINES] title lines would be left for it, the whole page scrolls as one instead: guidance,
 * then the start options, then the controls. Type never shrinks, and the order stays the same (BRD D171).
 */
@Suppress("LongParameterList")
@Composable
private fun StackedAttackLayout(
    startedAt: String,
    attack: AttackInProgressUiModel,
    nowMillis: Long,
    reduceMotion: Boolean,
    actions: HomeActions,
    modifier: Modifier
) {
    val density = LocalDensity.current
    // Saved, so after a language change or process death the page opens in the right layout at its saved scroll.
    var controlsHeightDp by rememberSaveable { mutableFloatStateOf(0f) }
    val guidanceMinHeight = with(density) {
        MaterialTheme.typography.titleLarge.lineHeight.toDp() * GUIDANCE_MIN_LINES
    }
    val guidance: @Composable () -> Unit = {
        Spacer(Modifier.height(16.dp))
        RainCard(startedAt = startedAt, reduceMotion = reduceMotion) {
            actions.onAttackDetails(attack.id)
        }
        if (attack.longAttackCheckDue) {
            Spacer(Modifier.height(16.dp))
            LongAttackCheckCard(attack = attack, nowMillis = nowMillis, actions = actions)
        }
    }
    val controls: @Composable () -> Unit = {
        // Measured where it is drawn; its height does not depend on which layout holds it.
        Column(
            Modifier.onSizeChanged { controlsHeightDp = with(density) { it.height.toDp().value } }
        ) {
            MoveStartOptions(onMoveStartEarlier = actions.onMoveStartEarlier)
            Spacer(Modifier.height(12.dp))
            AttackControls(attack = attack, actions = actions)
        }
    }
    BoxWithConstraints(modifier = modifier) {
        if (maxHeight - controlsHeightDp.dp >= guidanceMinHeight) {
            Column(Modifier.fillMaxSize()) {
                PageScroll {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .pageScroll(rememberScrollState())
                    ) { guidance() }
                }
                controls()
            }
        } else {
            PageScroll {
                Column(Modifier.fillMaxSize().pageScroll(rememberScrollState())) {
                    guidance()
                    // The same gap as between the two cards, so the start options read as their own group.
                    Spacer(Modifier.height(16.dp))
                    controls()
                }
            }
        }
    }
}

// Two columns of equal weight: HomeContents keeps messages to the start half because the controls column begins just
// past it (design D4 of fix-today-zone-and-landscape-message). Change the split and the message area together.
@Composable
private fun SideBySideAttackLayout(
    startedAt: String,
    attack: AttackInProgressUiModel,
    nowMillis: Long,
    messageBottom: () -> Dp,
    actions: HomeActions,
    modifier: Modifier
) {
    // Whole dp, so it changes only while the message moves (BRD D52).
    val messageMove by remember { derivedStateOf { messageBottom().value.roundToInt() } }
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = modifier) {
        Box(modifier = Modifier.weight(1f)) {
            // While a message shows over this column, the column's visible part starts below it, so no line is
            // covered, scrolled or not (BRD D52). The column's own top space then separates its first line from the
            // message.
            PageScroll {
                Column(
                    modifier = Modifier
                        .clearOfMessage(messageBottom)
                        .pageScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(16.dp))
                    AttackGuidance(startedAt = startedAt, textAlign = TextAlign.Start) {
                        actions.onAttackDetails(attack.id)
                    }
                    if (attack.longAttackCheckDue) {
                        Spacer(Modifier.height(16.dp))
                        LongAttackCheckCard(
                            attack = attack,
                            nowMillis = nowMillis,
                            actions = actions
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    MoveStartOptions(onMoveStartEarlier = actions.onMoveStartEarlier)
                }
            }
            // The column's controls move with it, so a tap while it moves could land on a different one: a quick
            // second "2 h earlier" after a rejected move, say (review of D52).
            TapGuard(
                key = messageMove,
                durationMillis = GUIDANCE_MOVE_GUARD_MILLIS,
                modifier = Modifier.matchParentSize()
            )
        }
        PageScroll {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .pageScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(16.dp))
                AttackControls(attack = attack, actions = actions)
            }
        }
    }
}

@Composable
private fun AttackControls(attack: AttackInProgressUiModel, actions: HomeActions) {
    // Each control says its name from its own node, so a page edge that cuts it never leaves it nameless (#255, #274).
    val medicationTaken = stringResource(R.string.medication_taken)
    val endAttack = stringResource(R.string.end_attack)
    IntensityChoices(selected = attack.currentLevel, onIntensity = actions.onIntensity)
    Spacer(Modifier.height(12.dp))
    // A large, dim control for the dark room (BRD 18.3 B, D34, EC-UX-02).
    OutlinedButton(
        onClick = { actions.onLogMedication(attack.id) },
        shape = RoundedCornerShape(32.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MigrelyColors.AttackModeText),
        border = BorderStroke(1.dp, MigrelyColors.AttackModeOutline),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .ownWords(medicationTaken)
    ) {
        Icon(MigrelyIcons.Add, contentDescription = null, modifier = Modifier.size(20.dp))
        Text(
            text = medicationTaken,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .padding(start = 8.dp)
                .wordsShownOnly()
        )
    }
    Spacer(Modifier.height(16.dp))
    Button(
        onClick = actions.onEndAttack,
        shape = RoundedCornerShape(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MigrelyColors.AttackModeEndContainer,
            contentColor = MigrelyColors.AttackModeEndText
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .ownWords(endAttack)
    ) {
        Text(
            text = endAttack,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.wordsShownOnly()
        )
    }
    Spacer(Modifier.height(24.dp))
}

@Composable
private fun RainCard(startedAt: String, reduceMotion: Boolean, onDetails: () -> Unit) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 208.dp)
            .clip(RoundedCornerShape(28.dp))
    ) {
        SkyCanvas(
            appearance = SkyAppearance.Rain,
            modifier = Modifier.matchParentSize(),
            animate = !reduceMotion,
            decorationHeight = 100.dp
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 104.dp, bottom = 22.dp)
        ) {
            AttackGuidance(
                startedAt = startedAt,
                textAlign = TextAlign.Center,
                onDetails = onDetails
            )
        }
    }
}

@Composable
private fun AttackGuidance(startedAt: String, textAlign: TextAlign, onDetails: () -> Unit) {
    // The one calming line uses the title face (BRD D20); the time below uses tabular figures so it does not shift
    // sideways as the minutes tick (EC-UX-17).
    Text(
        text = stringResource(R.string.attack_rest),
        style = MaterialTheme.typography.titleLarge,
        color = MigrelyColors.AttackModeText,
        textAlign = textAlign
    )
    Text(
        text = startedAt,
        style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
        // Full strength: 0.8 alpha measured 4.05:1, under WCAG AA (QA #13).
        color = MigrelyColors.AttackModeText,
        textAlign = textAlign,
        modifier = Modifier.padding(top = 4.dp)
    )
    // A dim link, so a long attack can be described without waiting for it to end (BRD D24).
    val detailsDescription = stringResource(R.string.attack_details_description)
    TextButton(
        onClick = onDetails,
        modifier = Modifier
            .heightIn(min = 48.dp)
            .semantics { contentDescription = detailsDescription }
    ) {
        // One spoken name (BRD D181 (2)): "Details of this migraine", not also "Details".
        Text(
            text = stringResource(R.string.attack_details),
            style = MaterialTheme.typography.labelMedium,
            color = MigrelyColors.AttackModeText,
            modifier = Modifier.clearAndSetSemantics { }
        )
    }
}

@Composable
private fun elapsedText(elapsedMillis: Long): String {
    val minutes = (elapsedMillis / MILLIS_PER_MINUTE).coerceAtLeast(0)
    return when {
        minutes < MINUTES_PER_HOUR -> stringResource(R.string.elapsed_minutes, minutes.toInt())
        minutes < MINUTES_PER_DAY -> stringResource(
            R.string.elapsed_hours_minutes,
            (minutes / MINUTES_PER_HOUR).toInt(),
            (minutes % MINUTES_PER_HOUR).toInt()
        )

        else -> stringResource(
            R.string.elapsed_days_hours,
            (minutes / MINUTES_PER_DAY).toInt(),
            ((minutes % MINUTES_PER_DAY) / MINUTES_PER_HOUR).toInt()
        )
    }
}

/** The label sits on its own line and the options wrap, so large fonts never break a word mid-way. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MoveStartOptions(onMoveStartEarlier: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.move_start_label),
            style = MaterialTheme.typography.bodyMedium,
            color = MigrelyColors.AttackModeText
        )
        FlowRow(modifier = Modifier.fillMaxWidth()) {
            MOVE_START_OPTIONS.forEach { (minutes, label) ->
                val text = stringResource(label)
                val description = stringResource(R.string.move_start_description, text)
                TextButton(
                    onClick = { onMoveStartEarlier(minutes) },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .semantics { contentDescription = description }
                ) {
                    // One spoken name (BRD D181 (2)): "Move the start 15 min earlier" only.
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MigrelyColors.AttackModeText,
                        modifier = Modifier.clearAndSetSemantics { }
                    )
                }
            }
        }
    }
}

@Composable
private fun IntensityChoices(
    selected: IntensityLevelUiModel?,
    onIntensity: (IntensityLevelUiModel) -> Unit
) {
    // Three chips side by side stop fitting at large font sizes; stack them full width instead (BRD EC-UX-03).
    if (LocalDensity.current.fontScale >= STACKED_FONT_SCALE) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup()
        ) {
            IntensityLevelUiModel.entries.forEach { level ->
                IntensityChip(level, level == selected, onIntensity, Modifier.fillMaxWidth())
            }
        }
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup()
        ) {
            IntensityLevelUiModel.entries.forEach { level ->
                IntensityChip(level, level == selected, onIntensity, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun IntensityChip(
    level: IntensityLevelUiModel,
    isSelected: Boolean,
    onIntensity: (IntensityLevelUiModel) -> Unit,
    modifier: Modifier
) {
    val container = if (isSelected) {
        MigrelyColors.AttackModeSelectedContainer
    } else {
        MigrelyColors.AttackModeSurface
    }
    val outline = if (isSelected) {
        MigrelyColors.AttackModeSelectedOutline
    } else {
        MigrelyColors.AttackModeOutline
    }
    val textColor = if (isSelected) {
        MigrelyColors.AttackModeSelectedText
    } else {
        MigrelyColors.AttackModeText
    }
    val label = stringResource(level.label())
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = container,
        border = BorderStroke(width = if (isSelected) 1.5.dp else 1.dp, color = outline),
        modifier = modifier
            .heightIn(min = 64.dp)
            .selectable(selected = isSelected, onClick = { onIntensity(level) })
            .ownWords(label)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.wordsShownOnly()
            )
        }
    }
}

private fun IntensityLevelUiModel.label(): Int = when (this) {
    IntensityLevelUiModel.Mild -> R.string.intensity_mild
    IntensityLevelUiModel.Moderate -> R.string.intensity_moderate
    IntensityLevelUiModel.Severe -> R.string.intensity_severe
}

/** "Still going?" after 72 hours (BRD EC-ATK-02, SAF-F3), inline so the dark room stays calm. */
@Composable
private fun LongAttackCheckCard(
    attack: AttackInProgressUiModel,
    nowMillis: Long,
    actions: HomeActions
) {
    var pickingEnd by rememberSaveable(attack.id) { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MigrelyColors.AttackModeSurface,
        border = BorderStroke(1.dp, MigrelyColors.AttackModeOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.long_check_title),
                style = MaterialTheme.typography.titleMedium,
                color = MigrelyColors.AttackModeSelectedText
            )
            Text(
                text = stringResource(R.string.long_check_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MigrelyColors.AttackModeText,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
            LongCheckButton(R.string.long_check_still_going) { actions.onStillGoing(attack.id) }
            LongCheckButton(R.string.long_check_ended) { pickingEnd = true }
            LongCheckButton(R.string.long_check_dont_ask) { actions.onDontAskAgain(attack.id) }
        }
    }
    if (pickingEnd) {
        // Only times between the start and now can be confirmed (spec: attack-logging, "It has ended").
        MigrelyDateTimePicker(
            initialEpochMillis = nowMillis,
            minEpochMillis = attack.startEpochMillis,
            maxEpochMillis = nowMillis,
            zoneId = ZoneId.systemDefault(),
            labels = DateTimePickerLabels(
                title = stringResource(R.string.end_time_title),
                next = stringResource(R.string.home_picker_next),
                cancel = stringResource(R.string.home_picker_cancel),
                confirm = stringResource(R.string.home_picker_confirm),
                outOfRange = stringResource(R.string.message_end_out_of_range)
            ),
            onConfirm = { endEpochMillis ->
                pickingEnd = false
                actions.onEndedAt(endEpochMillis)
            },
            onDismiss = { pickingEnd = false }
        )
    }
}

@Composable
private fun LongCheckButton(label: Int, onClick: () -> Unit) {
    val name = stringResource(label)
    TextButton(onClick = onClick, modifier = Modifier.heightIn(min = 48.dp).ownWords(name)) {
        Text(
            text = name,
            color = MigrelyColors.AttackModeSelectedOutline,
            modifier = Modifier.wordsShownOnly()
        )
    }
}

private val PREVIEW_ZONE: ZoneId = ZoneId.of("Asia/Tashkent")

internal val previewFormatter = TimeUiFormatter(
    locale = { Locale.UK },
    is24HourFormat = { true },
    deviceZone = { PREVIEW_ZONE },
    bestDateTimePattern = { _, skeleton ->
        when (skeleton) {
            "EEE" -> "EEE"
            "dMMM" -> "d MMM"
            "EEEEdMMMM" -> "EEEE, d MMMM"
            else -> "HH:mm"
        }
    }
)

internal val previewActions = HomeActions({}, {}, {}, {}, {}, {}, {}, {}, {}, {})

private fun previewMillis(hour: Int, minute: Int): Long =
    LocalDate.of(2026, 10, 23).atTime(hour, minute).atZone(PREVIEW_ZONE).toInstant().toEpochMilli()

@Preview(name = "Attack mode")
@Composable
private fun AttackModePreview() {
    MigrelyTheme {
        AttackModeContents(
            attack = AttackInProgressUiModel(
                id = "a",
                startEpochMillis = previewMillis(14, 5),
                startZoneId = "Asia/Tashkent",
                currentLevel = IntensityLevelUiModel.Moderate,
                longAttackCheckDue = false
            ),
            nowMillis = previewMillis(14, 47),
            reduceMotion = true,
            messageBottom = { 0.dp },
            timeUiFormatter = previewFormatter,
            actions = previewActions
        )
    }
}

@Preview(name = "Attack mode: 72-hour check")
@Composable
private fun AttackModeLongCheckPreview() {
    MigrelyTheme {
        AttackModeContents(
            attack = AttackInProgressUiModel(
                id = "a",
                startEpochMillis = previewMillis(14, 5) - 73 * 60 * MILLIS_PER_MINUTE,
                startZoneId = "Asia/Tashkent",
                currentLevel = IntensityLevelUiModel.Severe,
                longAttackCheckDue = true
            ),
            nowMillis = previewMillis(14, 5),
            reduceMotion = true,
            messageBottom = { 0.dp },
            timeUiFormatter = previewFormatter,
            actions = previewActions
        )
    }
}
