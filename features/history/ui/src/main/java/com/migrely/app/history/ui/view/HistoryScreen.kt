package com.migrely.app.history.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalLayoutDirection
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
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import com.migrely.app.architecture.ui.view.ScreenEnterObserver
import com.migrely.app.designsystem.button.MigrelyIconButton
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.layout.MigrelyInsets
import com.migrely.app.designsystem.layout.PageScroll
import com.migrely.app.designsystem.layout.ShownTextLayouts
import com.migrely.app.designsystem.layout.isCompactHeight
import com.migrely.app.designsystem.layout.ownWords
import com.migrely.app.designsystem.layout.shownTextLayouts
import com.migrely.app.designsystem.layout.wordsShownOnly
import com.migrely.app.designsystem.motion.rememberReduceMotion
import com.migrely.app.designsystem.snackbar.MessageLayout
import com.migrely.app.designsystem.text.rememberWidestWidth
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

private val CONTENT_MAX_WIDTH = 600.dp
private val PAGE_PADDING = 16.dp
private val PANE_GAP = 24.dp
private val MIN_PANE_WIDTH = 200.dp
private val PEAK_GAP = 12.dp
private val PEAK_UNDER_GAP = 4.dp

/**
 * History (spec: attack-history). The month asked for and the chosen day are saveable, so rotation and process death
 * keep them (design D4). [initialDay] comes from home and seeds both. The request is kept as the user made it, never
 * replaced by the month the domain clamped it to, so an undo that brings back an earlier month shows it again; the
 * arrows step from the month shown. A slower answer to a request the user has since replaced is ignored.
 *
 * [overuseIndicator] is the medication feature's standing overuse indicator, filled by `:app` and given History's load
 * count as its refresh key (BRD D59).
 */
@Composable
fun HistoryDependencies.History(
    navController: NavController,
    initialDay: LocalDate?,
    overuseIndicator: @Composable (refreshKey: Int) -> Unit,
    modifier: Modifier = Modifier,
    openReport: Boolean = false
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
    OpenReportOnce(openReport, onOpen = historyViewModel::onReportAction)

    val viewState by historyViewModel.viewState.collectAsState(HistoryViewState.Loading)
    // Counts History's loads, so the overuse indicator loads with each of them, an undo or a resume included (D59).
    val historyLoads by produceState(initialValue = 0, historyViewModel) {
        historyViewModel.viewState.collect { value += 1 }
    }
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
            onReport = historyViewModel::onReportAction,
            onInsights = historyViewModel::onInsightsAction,
            onPreviousMonth = { shown?.let { showMonth(it.yearMonth().minusMonths(1)) } },
            onNextMonth = { shown?.let { showMonth(it.yearMonth().plusMonths(1)) } },
            onDay = { epochDay ->
                selectedEpochDay = if (selectedEpochDay == epochDay) null else epochDay
            },
            onShowAllMonth = { selectedEpochDay = null },
            onAttack = historyViewModel::onAttackAction,
            onAddPastAttack = { epochDay ->
                historyViewModel.onAddPastAttackAction(epochDay?.let(LocalDate::ofEpochDay))
            },
            onPeriod = { epochDay, marked ->
                historyViewModel.onPeriodAction(
                    currentMonth,
                    LocalDate.ofEpochDay(epochDay),
                    marked
                )
            },
            onPainFree = { epochDay, marked ->
                historyViewModel.onPainFreeAction(
                    currentMonth,
                    LocalDate.ofEpochDay(epochDay),
                    marked
                )
            }
        ),
        messages = {
            HistoryMessages(
                presenter = historyMessagePresenter,
                navController = navController,
                behaviourMapper = historyMessageBehaviourUiMapper,
                reduceMotion = reduceMotion,
                onUndoDelete = { attackId ->
                    historyViewModel.onUndoDeleteAction(attackId, currentMonth)
                }
            )
        },
        overuseIndicator = { overuseIndicator(historyLoads) },
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
    val onAddPastAttack: (epochDay: Long?) -> Unit,
    val onPeriod: (epochDay: Long, marked: Boolean) -> Unit = { _, _ -> },
    val onPainFree: (epochDay: Long, marked: Boolean) -> Unit = { _, _ -> },
    val onReport: () -> Unit = {},
    val onInsights: () -> Unit = {}
)

private val HistoryViewStateUiModel.month: HistoryMonthUiModel?
    get() = when (this) {
        HistoryViewStateUiModel.Loading -> null
        is HistoryViewStateUiModel.NothingLogged -> month
        is HistoryViewStateUiModel.Ready -> month
    }

private val HistoryViewStateUiModel.requestedMonth: String?
    get() = when (this) {
        HistoryViewStateUiModel.Loading -> null
        is HistoryViewStateUiModel.NothingLogged -> requestedMonth
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
    overuseIndicator: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    MessageLayout(
        message = messages,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
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
                            contentDescription = stringResource(R.string.history_navigate_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = actions.onInsights) {
                        Icon(
                            MigrelyIcons.Insights,
                            contentDescription = stringResource(R.string.history_insights)
                        )
                    }
                    IconButton(onClick = actions.onReport) {
                        Icon(
                            MigrelyIcons.Report,
                            contentDescription = stringResource(R.string.history_doctor_report)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = MigrelyInsets.bars.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                )
            )
        }
    ) {
        Column(Modifier.fillMaxSize()) {
            val month = viewState.month
            if (month == null) {
                Box(Modifier.fillMaxSize())
            } else {
                MonthContents(
                    month = month,
                    nothingLogged = viewState is HistoryViewStateUiModel.NothingLogged,
                    selectedEpochDay = selectedEpochDay,
                    mappers = mappers,
                    actions = actions,
                    overuseIndicator = overuseIndicator
                )
            }
        }
    }
}

@Composable
private fun MonthContents(
    month: HistoryMonthUiModel,
    nothingLogged: Boolean,
    selectedEpochDay: Long?,
    mappers: HistoryCalendarMappers,
    actions: HistoryActions,
    overuseIndicator: @Composable () -> Unit
) {
    val contentModifier = Modifier
        .windowInsetsPadding(
            MigrelyInsets.bars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
        )
        .padding(horizontal = PAGE_PADDING)
    val calendar: @Composable () -> Unit = {
        MonthHeader(month, mappers.historyTimeUiFormatter, actions)
        HistoryCalendar(
            month = month,
            selectedEpochDay = selectedEpochDay,
            mappers = mappers,
            onDay = actions.onDay
        )
        // Before anything is logged, the first-use card replaces the numbers (BRD EC-HIS-01, D99).
        if (!nothingLogged) {
            MonthSummary(
                month = month,
                timeUiFormatter = mappers.historyTimeUiFormatter,
                overuseIndicator = overuseIndicator,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
    val list: @Composable () -> Unit = {
        ListSection(month, nothingLogged, selectedEpochDay, mappers.historyTimeUiFormatter, actions)
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        // A phone in landscape puts the calendar and the list side by side (BRD EC-UX-03), when each
        // pane is wide enough.
        if (showsTwoPanes(maxWidth)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PANE_GAP),
                modifier = contentModifier
            ) {
                PageScroll {
                    Column(
                        Modifier
                            .weight(1f)
                            .pageScroll(rememberScrollState())
                    ) {
                        calendar()
                        Spacer(Modifier.height(16.dp))
                    }
                }
                PageScroll {
                    Column(
                        Modifier
                            .weight(1f)
                            .pageScroll(rememberScrollState())
                    ) {
                        list()
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        } else {
            Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
                PageScroll {
                    Column(
                        modifier = contentModifier
                            .widthIn(max = CONTENT_MAX_WIDTH)
                            .fillMaxWidth()
                            .pageScroll(rememberScrollState())
                    ) {
                        calendar()
                        Spacer(Modifier.height(24.dp))
                        list()
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

/**
 * True in a short window where each of the two panes would be at least 200 dp wide, so a day number never needs less
 * than its 100% size (BRD D198 (4), EC-HIS-06); a split screen or a small pop-up window gets the one column instead.
 * [width] is History's own; the panes get it less the side insets and the page padding.
 */
@Composable
private fun showsTwoPanes(width: Dp): Boolean {
    val direction = LocalLayoutDirection.current
    val insets = MigrelyInsets.bars.only(WindowInsetsSides.Horizontal).asPaddingValues()
    val contentWidth = width - insets.calculateStartPadding(direction) -
        insets.calculateEndPadding(direction) - PAGE_PADDING * 2
    return isCompactHeight() && (contentWidth - PANE_GAP) / 2 >= MIN_PANE_WIDTH
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
        MigrelyIconButton(
            contentDescription = stringResource(R.string.previous_month),
            onClick = actions.onPreviousMonth,
            enabled = month.hasPreviousMonth
        ) {
            Icon(
                MigrelyIcons.ChevronStart,
                contentDescription = null
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
        MigrelyIconButton(
            contentDescription = stringResource(R.string.next_month),
            onClick = actions.onNextMonth,
            enabled = month.hasNextMonth
        ) {
            Icon(MigrelyIcons.ChevronEnd, contentDescription = null)
        }
    }
}

@Composable
private fun ListSection(
    month: HistoryMonthUiModel,
    nothingLogged: Boolean,
    selectedEpochDay: Long?,
    timeUiFormatter: HistoryTimeUiFormatter,
    actions: HistoryActions
) {
    val monthName = timeUiFormatter.formatMonthName(month.year, month.month)
    val selectedDay = month.days.firstOrNull { it.epochDay == selectedEpochDay }
    when {
        selectedDay != null -> DaySection(selectedDay, monthName, timeUiFormatter, actions)
        nothingLogged -> EmptyHistory(actions)
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

/** The chosen day's attacks, oldest first, its marks, and adding an attack for that day (BRD HIS-F3, D30, D48). */
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
    Spacer(Modifier.height(8.dp))
    // With "Track periods" off, a marked day still offers the switch, so the mark can be removed, and it stays while
    // the day is chosen, so a mark removed by mistake can be put back (BRD D79). The latch only ever turns on: it is
    // safe because Settings opens only from home, so the setting cannot change while History is open (QA #66).
    var periodShown by rememberSaveable(day.epochDay) { mutableStateOf(false) }
    if (day.periodOffered) periodShown = true
    if (periodShown) {
        MarkSwitch(
            label = stringResource(R.string.mark_period),
            checked = day.period,
            onChange = { actions.onPeriod(day.epochDay, it) }
        )
    }
    MarkSwitch(
        label = stringResource(R.string.mark_pain_free),
        checked = day.painFree,
        onChange = { actions.onPainFree(day.epochDay, it) },
        disabledReason = if (day.painFreeAllowed) {
            null
        } else {
            stringResource(
                R.string.mark_pain_free_blocked
            )
        }
    )
    Spacer(Modifier.height(16.dp))
    OutlinedButton(
        onClick = { actions.onAddPastAttack(day.epochDay) },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .ownWords(stringResource(R.string.history_add_past_attack))
    ) {
        Text(
            stringResource(R.string.history_add_past_attack),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.wordsShownOnly()
        )
    }
    TextButton(
        onClick = actions.onShowAllMonth,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .ownWords(stringResource(R.string.show_all_month, monthName))
    ) {
        Text(
            stringResource(R.string.show_all_month, monthName),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.wordsShownOnly()
        )
    }
}

/**
 * One day mark as a whole row that toggles, with a [Switch] that only shows the state. With [disabledReason] the row is
 * off and cannot be toggled, and the reason is shown and read under the label (BRD EC-DAY-01).
 */
@Composable
private fun MarkSwitch(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    disabledReason: String? = null
) {
    val enabled = disabledReason == null
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onChange
            )
            .ownWords(label)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                modifier = Modifier.wordsShownOnly(),
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            if (disabledReason != null) {
                Text(
                    text = disabledReason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = checked, onCheckedChange = null, enabled = enabled)
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
    val (start, end) = timeUiFormatter.formatRange(
        attack.startEpochMillis,
        attack.endEpochMillis,
        attack.startZoneId
    )
    val spokenStart = timeUiFormatter.formatRange(
        attack.startEpochMillis,
        attack.endEpochMillis,
        attack.startZoneId,
        spoken = true
    ).start
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
    val dateText = stringResource(
        R.string.attack_range,
        start,
        end ?: stringResource(R.string.attack_in_progress)
    )
    val dateStyle = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum")
    // The date breaks only at a plain space; a no-break space keeps "12:27 PM" whole where the locale uses one.
    val dateWidestWord = rememberWidestWidth(dateText.split(' '), dateStyle)
    val dateLayout = remember { ShownTextLayouts() }
    val peakLayout = remember { ShownTextLayouts() }
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
        DateAndPeak(
            dateWidestWord = dateWidestWord,
            date = {
                Text(
                    text = dateText,
                    style = dateStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clearAndSetSemantics { shownTextLayouts = dateLayout },
                    onTextLayout = { dateLayout.record(0, it) }
                )
            },
            peak = {
                Text(
                    text = peakText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clearAndSetSemantics { shownTextLayouts = peakLayout },
                    onTextLayout = { peakLayout.record(0, it) }
                )
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

/**
 * A row's date range with its peak at the end, centred, while the room left beside the peak holds [dateWidestWord],
 * the px width of the date's widest unbreakable piece ("12:27", "Mon,"). Otherwise the date takes the whole width,
 * wrapping only between words, and the peak goes on its own line under it, at the start (BRD D181 (5), EC-HIS-07).
 * No type shrinks.
 */
@Composable
private fun DateAndPeak(
    dateWidestWord: Int,
    date: @Composable () -> Unit,
    peak: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Layout(contents = listOf(date, peak), modifier = modifier) { measurables, constraints ->
        val width = constraints.maxWidth
        val dateMeasurable = measurables[0].single()
        val peakPlaceable = measurables[1].single().measure(Constraints(maxWidth = width))
        val room = width - PEAK_GAP.roundToPx() - peakPlaceable.width
        if (dateWidestWord <= room) {
            val datePlaceable = dateMeasurable.measure(Constraints.fixedWidth(room))
            val height = maxOf(constraints.minHeight, datePlaceable.height, peakPlaceable.height)
            fun centred(placeable: Placeable) =
                Alignment.CenterVertically.align(placeable.height, height)
            layout(width, height) {
                datePlaceable.placeRelative(0, centred(datePlaceable))
                peakPlaceable.placeRelative(width - peakPlaceable.width, centred(peakPlaceable))
            }
        } else {
            val datePlaceable = dateMeasurable.measure(Constraints(maxWidth = width))
            val below = datePlaceable.height + PEAK_UNDER_GAP.roundToPx()
            val contentHeight = below + peakPlaceable.height
            val height = maxOf(constraints.minHeight, contentHeight)
            val top = Alignment.CenterVertically.align(contentHeight, height)
            layout(width, height) {
                datePlaceable.placeRelative(0, top)
                peakPlaceable.placeRelative(0, top + below)
            }
        }
    }
}

internal val previewActions = HistoryActions({}, {}, {}, {}, {}, {}, {})

@Preview(name = "History: October")
@Composable
private fun HistoryReadyPreview() {
    MigrelyTheme {
        HistoryContents(
            viewState = HistoryViewStateUiModel.Ready(previewMonth(), null),
            selectedEpochDay = null,
            mappers = previewMappers,
            actions = previewActions,
            messages = {},
            overuseIndicator = {}
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
            messages = {},
            overuseIndicator = {}
        )
    }
}

@Preview(name = "History: first use")
@Composable
private fun HistoryEmptyPreview() {
    MigrelyTheme {
        HistoryContents(
            viewState = HistoryViewStateUiModel.NothingLogged(previewMonth(), null),
            selectedEpochDay = null,
            mappers = previewMappers,
            actions = previewActions,
            messages = {},
            overuseIndicator = {}
        )
    }
}
