package com.migrely.app.history.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import com.migrely.app.architecture.ui.view.ScreenEnterObserver
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.motion.rememberReduceMotion
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.history.presentation.model.HistoryViewState
import com.migrely.app.history.ui.R
import com.migrely.app.history.ui.di.HistoryDependencies
import com.migrely.app.history.ui.mapper.HistoryCalendarMappers
import com.migrely.app.history.ui.mapper.HistoryTimeUiFormatter
import com.migrely.app.history.ui.model.HistoryAttackUiModel
import com.migrely.app.history.ui.model.HistoryDayUiModel
import com.migrely.app.history.ui.model.HistoryMonthUiModel
import com.migrely.app.history.ui.model.HistoryViewStateUiModel
import java.time.LocalDate
import java.time.YearMonth

private const val COMPACT_HEIGHT_DP = 480
private val CONTENT_MAX_WIDTH = 600.dp

/**
 * History (spec: attack-history). The month asked for and the chosen day are saveable, so rotation and process death
 * keep them (design D4). [initialDay] comes from home and seeds both. The request is kept as the user made it, never
 * replaced by the month the domain clamped it to, so an undo that brings back an earlier month shows it again; the
 * arrows step from the month shown. A slower answer to a request the user has since replaced is ignored.
 */
@Composable
fun HistoryDependencies.History(
    navController: NavController,
    initialDay: LocalDate?,
    modifier: Modifier = Modifier
) {
    var requestedMonth by rememberSaveable {
        mutableStateOf(initialDay?.let { YearMonth.from(it).toString() })
    }
    var selectedEpochDay by rememberSaveable { mutableStateOf(initialDay?.toEpochDay()) }
    val month = requestedMonth?.let(YearMonth::parse)
    val currentMonth by rememberUpdatedState(month)
    ScreenEnterObserver { historyViewModel.onEnter(currentMonth) }
    LifecycleResumeEffect(historyViewModel) {
        historyViewModel.onResume(currentMonth)
        onPauseOrDispose { }
    }
    ViewModelObserver(navController)

    val viewState by historyViewModel.viewState.collectAsState(HistoryViewState.Loading)
    var uiState by rememberSaveable {
        mutableStateOf(historyViewStateUiMapper.toUi(HistoryViewState.Loading))
    }
    val currentRequest by rememberUpdatedState(requestedMonth)
    LaunchedEffect(viewState) {
        val answer = historyViewStateUiMapper.toUi(viewState)
        if (answer.month != null && answer.requestedMonth == currentRequest) uiState = answer
    }
    val shown = uiState.month
    val reduceMotion = rememberReduceMotion()

    fun showMonth(target: YearMonth) {
        requestedMonth = target.toString()
        selectedEpochDay = null
        historyViewModel.onMonthAction(target)
    }

    HistoryContents(
        viewState = uiState,
        selectedEpochDay = selectedEpochDay,
        mappers = historyCalendarMappers,
        actions = HistoryActions(
            onBack = historyViewModel::onLeaveAction,
            onPreviousMonth = { shown?.let { showMonth(it.yearMonth().minusMonths(1)) } },
            onNextMonth = { shown?.let { showMonth(it.yearMonth().plusMonths(1)) } },
            onDay = { epochDay ->
                selectedEpochDay = if (selectedEpochDay == epochDay) null else epochDay
            },
            onShowAllMonth = { selectedEpochDay = null },
            onAttack = historyViewModel::onAttackAction,
            onAddPastAttack = { epochDay ->
                historyViewModel.onAddPastAttackAction(epochDay?.let(LocalDate::ofEpochDay))
            }
        ),
        messages = {
            HistoryMessages(
                presenter = historyMessagePresenter,
                behaviourMapper = historyMessageBehaviourUiMapper,
                reduceMotion = reduceMotion,
                onUndoDelete = { attackId ->
                    historyViewModel.onUndoDeleteAction(attackId, currentMonth)
                }
            )
        },
        modifier = modifier
    )
}

/** Callbacks from History to its ViewModel and its saveable state. */
data class HistoryActions(
    val onBack: () -> Unit,
    val onPreviousMonth: () -> Unit,
    val onNextMonth: () -> Unit,
    val onDay: (epochDay: Long) -> Unit,
    val onShowAllMonth: () -> Unit,
    val onAttack: (attackId: String) -> Unit,
    val onAddPastAttack: (epochDay: Long?) -> Unit
)

private val HistoryViewStateUiModel.month: HistoryMonthUiModel?
    get() = when (this) {
        HistoryViewStateUiModel.Loading -> null
        is HistoryViewStateUiModel.NoAttacks -> month
        is HistoryViewStateUiModel.Ready -> month
    }

private val HistoryViewStateUiModel.requestedMonth: String?
    get() = when (this) {
        HistoryViewStateUiModel.Loading -> null
        is HistoryViewStateUiModel.NoAttacks -> requestedMonth
        is HistoryViewStateUiModel.Ready -> requestedMonth
    }

private fun HistoryMonthUiModel.yearMonth(): YearMonth = YearMonth.of(year, month)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryContents(
    viewState: HistoryViewStateUiModel,
    selectedEpochDay: Long?,
    mappers: HistoryCalendarMappers,
    actions: HistoryActions,
    messages: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.history_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(
                            MigrelyIcons.Back,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                )
            )
            val month = viewState.month
            if (month == null) {
                Box(Modifier.fillMaxSize())
            } else {
                MonthContents(
                    month = month,
                    noAttacks = viewState is HistoryViewStateUiModel.NoAttacks,
                    selectedEpochDay = selectedEpochDay,
                    mappers = mappers,
                    actions = actions
                )
            }
        }
        messages()
    }
}

@Composable
private fun MonthContents(
    month: HistoryMonthUiModel,
    noAttacks: Boolean,
    selectedEpochDay: Long?,
    mappers: HistoryCalendarMappers,
    actions: HistoryActions
) {
    val contentModifier = Modifier
        .windowInsetsPadding(
            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
        )
        .padding(horizontal = 16.dp)
    val calendar: @Composable () -> Unit = {
        MonthHeader(month, mappers.historyTimeUiFormatter, actions)
        HistoryCalendar(
            month = month,
            selectedEpochDay = selectedEpochDay,
            mappers = mappers,
            onDay = actions.onDay
        )
    }
    val list: @Composable () -> Unit = {
        ListSection(month, noAttacks, selectedEpochDay, mappers.historyTimeUiFormatter, actions)
    }
    // A phone in landscape puts the calendar and the list side by side (BRD EC-UX-03).
    if (LocalConfiguration.current.screenHeightDp < COMPACT_HEIGHT_DP) {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = contentModifier) {
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                calendar()
                Spacer(Modifier.height(16.dp))
            }
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                list()
                Spacer(Modifier.height(16.dp))
            }
        }
    } else {
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = contentModifier
                    .widthIn(max = CONTENT_MAX_WIDTH)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                calendar()
                Spacer(Modifier.height(24.dp))
                list()
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun MonthHeader(
    month: HistoryMonthUiModel,
    timeUiFormatter: HistoryTimeUiFormatter,
    actions: HistoryActions
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        IconButton(onClick = actions.onPreviousMonth, enabled = month.hasPreviousMonth) {
            Icon(
                MigrelyIcons.ChevronStart,
                contentDescription = stringResource(R.string.previous_month)
            )
        }
        Text(
            text = timeUiFormatter.formatMonthTitle(month.year, month.month),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() }
        )
        IconButton(onClick = actions.onNextMonth, enabled = month.hasNextMonth) {
            Icon(MigrelyIcons.ChevronEnd, contentDescription = stringResource(R.string.next_month))
        }
    }
}

@Composable
private fun ListSection(
    month: HistoryMonthUiModel,
    noAttacks: Boolean,
    selectedEpochDay: Long?,
    timeUiFormatter: HistoryTimeUiFormatter,
    actions: HistoryActions
) {
    val monthName = timeUiFormatter.formatMonthName(month.year, month.month)
    val selectedDay = month.days.firstOrNull { it.epochDay == selectedEpochDay }
    when {
        selectedDay != null -> DaySection(selectedDay, monthName, timeUiFormatter, actions)
        noAttacks -> EmptyHistory(actions)
        else -> {
            SectionTitle(
                if (month.attacks.isEmpty()) {
                    stringResource(R.string.month_no_attacks, monthName)
                } else {
                    pluralStringResource(
                        R.plurals.month_attack_count,
                        month.attacks.size,
                        month.attacks.size,
                        monthName
                    )
                }
            )
            AttackRows(month.attacks, timeUiFormatter, actions.onAttack)
        }
    }
}

/** The chosen day's attacks, oldest first, with adding one for that day (BRD HIS-F3, D30). */
@Composable
private fun DaySection(
    day: HistoryDayUiModel,
    monthName: String,
    timeUiFormatter: HistoryTimeUiFormatter,
    actions: HistoryActions
) {
    SectionTitle(timeUiFormatter.formatDayTitle(day.epochDay))
    if (day.attacks.isEmpty()) {
        Text(
            text = stringResource(R.string.day_no_attacks),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    } else {
        AttackRows(day.attacks, timeUiFormatter, actions.onAttack)
    }
    Spacer(Modifier.height(16.dp))
    OutlinedButton(
        onClick = { actions.onAddPastAttack(day.epochDay) },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
    ) {
        Text(stringResource(R.string.add_past_attack), style = MaterialTheme.typography.labelLarge)
    }
    TextButton(
        onClick = actions.onShowAllMonth,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
    ) {
        Text(
            stringResource(R.string.show_all_month, monthName),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/** First use (BRD EC-HIS-01). */
@Composable
private fun EmptyHistory(actions: HistoryActions) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )
            Text(
                text = stringResource(R.string.empty_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = { actions.onAddPastAttack(null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text(
                    stringResource(R.string.log_first_attack),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .padding(start = 8.dp, bottom = 8.dp)
            .semantics { heading() }
    )
}

@Composable
private fun AttackRows(
    attacks: List<HistoryAttackUiModel>,
    timeUiFormatter: HistoryTimeUiFormatter,
    onAttack: (attackId: String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        attacks.forEach { attack ->
            AttackRow(attack, timeUiFormatter) { onAttack(attack.id) }
        }
    }
}

@Composable
private fun AttackRow(
    attack: HistoryAttackUiModel,
    timeUiFormatter: HistoryTimeUiFormatter,
    onClick: () -> Unit
) {
    val start = timeUiFormatter.formatStart(attack.startEpochMillis, attack.startZoneId)
    val spokenStart =
        timeUiFormatter.formatStart(attack.startEpochMillis, attack.startZoneId, spoken = true)
    val end = attack.endEpochMillis?.let {
        timeUiFormatter.formatEnd(it, attack.startEpochMillis, attack.startZoneId)
    }
    val peak = attack.peakIntensity
    val peakText = if (peak == null) {
        stringResource(R.string.attack_peak_not_recorded)
    } else {
        stringResource(R.string.attack_peak, peak)
    }
    val spokenPeak = if (peak == null) {
        stringResource(R.string.attack_peak_not_recorded_spoken)
    } else {
        stringResource(R.string.attack_peak_spoken, peak)
    }
    val description = if (end == null) {
        stringResource(R.string.attack_in_progress_description, spokenStart, spokenPeak)
    } else {
        stringResource(R.string.attack_description, spokenStart, end, spokenPeak)
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            // Merged into the clickable, so the row stays focusable for keyboards and switch access (BRD EC-UX-19).
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
                text = stringResource(
                    R.string.attack_range,
                    start,
                    end ?: stringResource(R.string.attack_in_progress)
                ),
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

private val previewActions = HistoryActions({}, {}, {}, {}, {}, {}, {})

@Preview(name = "History: October")
@Composable
private fun HistoryReadyPreview() {
    MigrelyTheme {
        HistoryContents(
            viewState = HistoryViewStateUiModel.Ready(previewMonth(), null),
            selectedEpochDay = null,
            mappers = previewMappers,
            actions = previewActions,
            messages = {}
        )
    }
}

@Preview(name = "History: a day chosen")
@Composable
private fun HistoryDayPreview() {
    MigrelyTheme {
        HistoryContents(
            viewState = HistoryViewStateUiModel.Ready(previewMonth(), null),
            selectedEpochDay = LocalDate.parse("2026-10-06").toEpochDay(),
            mappers = previewMappers,
            actions = previewActions,
            messages = {}
        )
    }
}

@Preview(name = "History: first use")
@Composable
private fun HistoryEmptyPreview() {
    MigrelyTheme {
        HistoryContents(
            viewState = HistoryViewStateUiModel.NoAttacks(previewMonth(), null),
            selectedEpochDay = null,
            mappers = previewMappers,
            actions = previewActions,
            messages = {}
        )
    }
}
