package com.migrely.app.home.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.button.MigrelyIconButton
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.layout.MigrelyInsets
import com.migrely.app.designsystem.sky.SkyAppearance
import com.migrely.app.designsystem.sky.SkyCanvas
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.home.ui.R
import com.migrely.app.home.ui.mapper.SkyAppearanceUiMapper
import com.migrely.app.home.ui.mapper.TimeUiFormatter
import com.migrely.app.home.ui.model.AttackSummaryUiModel
import com.migrely.app.home.ui.model.PeakUiModel
import com.migrely.app.home.ui.model.SkyUiModel
import com.migrely.app.home.ui.model.TodayUiModel

private val SkyCardTitle = Color(0xFFC8CCD2)
private val SkyCardSubtitle = Color(0xFF9AA0A9)

@Composable
fun TodayContents(
    today: TodayUiModel,
    dayTitle: String,
    nowMillis: Long,
    skyAppearanceUiMapper: SkyAppearanceUiMapper,
    timeUiFormatter: TimeUiFormatter,
    onStartAttack: () -> Unit,
    onAttackDetails: (attackId: String) -> Unit,
    onAddPastAttack: () -> Unit,
    modifier: Modifier = Modifier,
    onLogMedication: () -> Unit = {},
    monthCalendar: @Composable () -> Unit = {},
    medicationPrompt: @Composable () -> Unit = {},
    onPeriodToday: (marked: Boolean) -> Unit = {},
    onSettings: () -> Unit = {},
    trialBanner: @Composable () -> Unit = {},
    visitCard: @Composable () -> Unit = {},
    onSettingsRowHeight: (Dp) -> Unit = {},
    settingsModifier: Modifier = Modifier,
    lastModifier: Modifier = Modifier
) {
    val screenModifier = modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .windowInsetsPadding(MigrelyInsets.bars)
        .padding(horizontal = 16.dp)
    val appearance = skyAppearanceUiMapper.toAppearance(today.sky)
    if (isCompactHeight()) {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = screenModifier) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                DayHeader(dayTitle, onSettings, onSettingsRowHeight, settingsModifier)
                Column(Modifier.fillMaxWidth()) {
                    trialBanner()
                    TodaySkyCard(today = today, appearance = appearance)
                    if (today.periodOffered) PeriodToday(today.periodToday, onPeriodToday)
                    medicationPrompt()
                    visitCard()
                    TodayAttackList(today.attacks, nowMillis, timeUiFormatter, onAttackDetails)
                    monthCalendar()
                }
                Spacer(Modifier.height(24.dp))
            }
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
            ) {
                StartAttackButton(onStartAttack)
                Spacer(Modifier.height(8.dp))
                QuietLinks(onAddPastAttack, onLogMedication, lastModifier)
            }
        }
    } else {
        Column(modifier = screenModifier) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                DayHeader(dayTitle, onSettings, onSettingsRowHeight, settingsModifier)
                Column(Modifier.fillMaxWidth()) {
                    trialBanner()
                    TodaySkyCard(today = today, appearance = appearance)
                    if (today.periodOffered) PeriodToday(today.periodToday, onPeriodToday)
                    medicationPrompt()
                    visitCard()
                    TodayAttackList(today.attacks, nowMillis, timeUiFormatter, onAttackDetails)
                    monthCalendar()
                }
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(16.dp))
            StartAttackButton(onStartAttack)
            Spacer(Modifier.height(4.dp))
            QuietLinks(onAddPastAttack, onLogMedication, lastModifier)
            Spacer(Modifier.height(12.dp))
        }
    }
}

/** Today's ended attacks under the sky, oldest first; each opens its details (BRD 18.3 A). */
@Composable
private fun TodayAttackList(
    attacks: List<AttackSummaryUiModel>,
    nowMillis: Long,
    timeUiFormatter: TimeUiFormatter,
    onAttackDetails: (attackId: String) -> Unit
) {
    if (attacks.isEmpty()) return
    Text(
        text = stringResource(R.string.today_list_title),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 8.dp, top = 20.dp, bottom = 8.dp)
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        attacks.forEach { attack ->
            AttackSummaryRow(attack, nowMillis, timeUiFormatter) { onAttackDetails(attack.id) }
        }
    }
}

@Composable
private fun AttackSummaryRow(
    attack: AttackSummaryUiModel,
    nowMillis: Long,
    timeUiFormatter: TimeUiFormatter,
    onClick: () -> Unit
) {
    // An attack that began on an earlier day shows that day before its start (spec: today-sky).
    val (start, end) = timeUiFormatter.formatRange(
        attack.startEpochMillis,
        attack.endEpochMillis,
        attack.startZoneId,
        nowMillis
    )
    val peak = attack.peakIntensity
    val peakText = if (peak == null) {
        stringResource(R.string.attack_summary_peak_not_recorded)
    } else {
        stringResource(R.string.attack_summary_peak, peak)
    }
    val spokenPeak = if (peak == null) {
        stringResource(R.string.attack_summary_peak_not_recorded_spoken)
    } else {
        stringResource(R.string.attack_summary_peak_spoken, peak)
    }
    val description = stringResource(R.string.attack_summary_description, start, end, spokenPeak)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            // Merged into the clickable, not replacing it, so the row stays focusable for keyboards and switch access
            // (QA #14, BRD EC-UX-19); only the texts inside are cleared so the description is read once.
            .semantics {
                contentDescription = description
                role = Role.Button
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clearAndSetSemantics { }
        ) {
            Text(
                text = stringResource(R.string.attack_summary_range, start, end),
                style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = peakText,
                style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

/** The date, with "Settings" at the end; attack mode has its own screen, so the button is normal mode only (D70). */
@Composable
private fun DayHeader(
    dayTitle: String,
    onSettings: () -> Unit,
    onHeight: (Dp) -> Unit,
    settingsModifier: Modifier
) {
    val density = LocalDensity.current
    // Its height, not its place, so a message placed from it does not follow scrolling (BRD D178 (6)).
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { onHeight(with(density) { it.height.toDp() }) }
    ) {
        Text(
            text = dayTitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, top = 16.dp, bottom = 16.dp)
        )
        MigrelyIconButton(onClick = onSettings, modifier = settingsModifier) {
            Icon(
                MigrelyIcons.Settings,
                contentDescription = stringResource(R.string.home_settings),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StartAttackButton(onStartAttack: () -> Unit) {
    Button(
        onClick = onStartAttack,
        shape = RoundedCornerShape(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
    ) {
        Text(
            text = stringResource(R.string.start_attack),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * The quiet ways to log what has already happened, under the big button: a past attack (BRD 18.3 A, D26) and a
 * medication taken (D34). Side by side while each label's longest word, with its button's padding, fits half the row;
 * otherwise stacked, each a full-width row, so no word breaks and the type keeps its size (D181 (5), EC-UX-48). A
 * label may still wrap at a space. "Add a past migraine" comes first in both, so the Tab order and [lastModifier]'s
 * place do not change.
 */
@Composable
private fun QuietLinks(
    onAddPastAttack: () -> Unit,
    onLogMedication: () -> Unit,
    lastModifier: Modifier
) {
    Layout(
        content = {
            QuietLink(R.string.home_add_past_attack, onAddPastAttack, Modifier)
            QuietLink(R.string.log_medication, onLogMedication, lastModifier)
        },
        modifier = Modifier.fillMaxWidth()
    ) { links, constraints ->
        val rowWidth = constraints.maxWidth
        val half = rowWidth / 2
        // A button's min intrinsic width is its longest word plus its padding.
        val stacked = links.any { it.minIntrinsicWidth(constraints.maxHeight) > half }
        val linkWidth = if (stacked) rowWidth else half
        val placeables = links.map {
            it.measure(constraints.copy(minWidth = linkWidth, maxWidth = linkWidth, minHeight = 0))
        }
        val height = if (stacked) placeables.sumOf { it.height } else placeables.maxOf { it.height }
        layout(rowWidth, height) {
            var x = 0
            var y = 0
            placeables.forEach { link ->
                link.placeRelative(x, y)
                if (stacked) y += link.height else x += link.width
            }
        }
    }
}

@Composable
private fun QuietLink(label: Int, onClick: () -> Unit, modifier: Modifier) {
    TextButton(onClick = onClick, modifier = modifier.heightIn(min = 48.dp)) {
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Home's one-tap period mark for today (BRD DAY-F1, D48): a filter chip with a check when on, read as "Period today" with
 * its state.
 */
@Composable
private fun PeriodToday(marked: Boolean, onPeriodToday: (Boolean) -> Unit) {
    val description = stringResource(R.string.period_today_description)
    FilterChip(
        selected = marked,
        onClick = { onPeriodToday(!marked) },
        // One spoken name (BRD D181 (2)): the chip says "Period today" with its state; the word it
        // shows is left out of its semantics, so TalkBack does not add "Period".
        label = {
            Text(
                text = stringResource(R.string.period_today),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clearAndSetSemantics { }
            )
        },
        leadingIcon = if (marked) {
            { Icon(MigrelyIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
        } else {
            null
        },
        modifier = Modifier
            .padding(top = 12.dp)
            .heightIn(min = 48.dp)
            .semantics { contentDescription = description }
    )
}

@Composable
private fun TodaySkyCard(today: TodayUiModel, appearance: SkyAppearance) {
    val painFree = today.sky == SkyUiModel.Clear
    val title = when {
        painFree -> stringResource(R.string.today_pain_free_title)
        today.attackCount == 0 -> stringResource(R.string.today_calm_title)
        else -> pluralStringResource(
            R.plurals.today_attack_count,
            today.attackCount,
            today.attackCount
        )
    }
    val subtitle = when (today.peak) {
        null -> if (painFree) {
            stringResource(R.string.today_pain_free_subtitle)
        } else {
            stringResource(R.string.today_calm_subtitle)
        }
        PeakUiModel.Mild -> stringResource(R.string.today_peak_mild)
        PeakUiModel.Moderate -> stringResource(R.string.today_peak_moderate)
        PeakUiModel.Severe -> stringResource(R.string.today_peak_severe)
        PeakUiModel.NotRecorded -> stringResource(R.string.today_peak_not_recorded)
    }
    val skyName = when (today.sky) {
        SkyUiModel.Calm -> stringResource(R.string.sky_calm)
        SkyUiModel.Clear -> stringResource(R.string.sky_clear)
        SkyUiModel.Hazy -> stringResource(R.string.home_sky_hazy)
        SkyUiModel.Cloudy -> stringResource(R.string.home_sky_cloudy)
        SkyUiModel.Storm -> stringResource(R.string.home_sky_storm)
    }
    val description = stringResource(R.string.today_card_description, skyName, title, subtitle)
    Box(
        contentAlignment = Alignment.BottomStart,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 208.dp)
            .clip(RoundedCornerShape(28.dp))
            .clearAndSetSemantics { contentDescription = description }
    ) {
        SkyCanvas(
            appearance = appearance,
            modifier = Modifier.matchParentSize(),
            decorationHeight = 92.dp
        )
        Column(
            modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 96.dp, bottom = 20.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, color = SkyCardTitle)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = SkyCardSubtitle
            )
        }
    }
}

// 23 October 2026, 17:00 in Tashkent.
private const val PREVIEW_NOW = 1_792_756_800_000L

@Preview(name = "Today: calm")
@Composable
private fun TodayCalmPreview() {
    MigrelyTheme {
        TodayContents(
            today = TodayUiModel(SkyUiModel.Calm, attackCount = 0, peak = null),
            dayTitle = "Thursday, 23 October",
            nowMillis = PREVIEW_NOW,
            skyAppearanceUiMapper = SkyAppearanceUiMapper(),
            timeUiFormatter = previewFormatter,
            onStartAttack = {},
            onAttackDetails = {},
            onAddPastAttack = {}
        )
    }
}

@Preview(name = "Today: storm")
@Composable
private fun TodayStormPreview() {
    MigrelyTheme {
        TodayContents(
            today = TodayUiModel(
                sky = SkyUiModel.Storm,
                attackCount = 2,
                peak = PeakUiModel.Severe,
                attacks = listOf(
                    AttackSummaryUiModel(
                        "a",
                        PREVIEW_NOW - 30_600_000L,
                        "Asia/Tashkent",
                        PREVIEW_NOW - 27_600_000L,
                        4
                    ),
                    AttackSummaryUiModel(
                        "b",
                        PREVIEW_NOW - 9_000_000L,
                        "Asia/Tashkent",
                        PREVIEW_NOW - 600_000L,
                        9
                    )
                )
            ),
            dayTitle = "Thursday, 23 October",
            nowMillis = PREVIEW_NOW,
            skyAppearanceUiMapper = SkyAppearanceUiMapper(),
            timeUiFormatter = previewFormatter,
            onStartAttack = {},
            onAttackDetails = {},
            onAddPastAttack = {}
        )
    }
}
