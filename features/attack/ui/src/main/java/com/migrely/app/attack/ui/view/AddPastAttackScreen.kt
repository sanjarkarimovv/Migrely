package com.migrely.app.attack.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.migrely.app.attack.presentation.model.AddPastAttackViewState
import com.migrely.app.attack.ui.R
import com.migrely.app.attack.ui.di.AddPastAttackDependencies
import com.migrely.app.attack.ui.mapper.AttackTimeUiFormatter
import com.migrely.app.attack.ui.model.AddPastAttackEventUiModel
import com.migrely.app.attack.ui.model.AttackRangeUiModel
import com.migrely.app.attack.ui.model.PastAttackDraftUiModel
import com.migrely.app.attack.ui.model.PastAttackProblemUiModel
import com.migrely.app.attack.ui.model.ShownAttackMessageUiModel
import com.migrely.app.attack.ui.model.TimeErrorReasonUiModel
import com.migrely.app.attack.ui.model.TimeErrorUiModel
import com.migrely.app.attack.ui.model.TimeFieldUiModel
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.motion.rememberReduceMotion
import com.migrely.app.designsystem.picker.MigrelyDateTimePicker
import com.migrely.app.designsystem.theme.MigrelyTheme
import java.time.Instant
import java.time.LocalDate

private val CONTENT_MAX_WIDTH = 640.dp

/** Callbacks from the add screen. */
data class AddPastAttackActions(
    val onBack: () -> Unit,
    val onStartPicked: (epochMillis: Long) -> Unit,
    val onEndPicked: (epochMillis: Long) -> Unit,
    val onAdd: () -> Unit,
    val onMerge: () -> Unit,
    val onChangeTimes: () -> Unit
)

/**
 * "Add a past migraine" (BRD ATK-F3, D26). The picked times, the problem shown and the message on screen are saveable,
 * so rotation and process death keep them (EC-ATK-10). Nothing is stored until "Add migraine" or "Merge them", and both
 * wait for their result before they can be tapped again (EC-ATK-17). [day] is the day chosen in History, or null for
 * the usual defaults (BRD D30).
 */
@Composable
fun AddPastAttackDependencies.AddPastAttack(
    navController: NavController,
    day: LocalDate?,
    modifier: Modifier = Modifier
) {
    ViewModelObserver(navController)
    val viewState by addPastAttackViewModel.viewState.collectAsState(AddPastAttackViewState.Loading)
    var draft by rememberSaveable { mutableStateOf<PastAttackDraftUiModel?>(null) }
    // The defaults are asked for until they arrive, also after process death before they did; once picked, the
    // saved times win.
    LaunchedEffect(Unit) { if (draft == null) addPastAttackViewModel.onEnter(day) }
    val defaults = (viewState as? AddPastAttackViewState.Ready)?.draft
    LaunchedEffect(defaults) {
        if (draft == null && defaults != null) draft = pastAttackDraftUiMapper.toUi(defaults)
    }
    var problem by rememberSaveable { mutableStateOf<PastAttackProblemUiModel?>(null) }
    var shownMessage by rememberSaveable { mutableStateOf<ShownAttackMessageUiModel?>(null) }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(addPastAttackEventPresenter) {
        addPastAttackEventPresenter.events.collect { event ->
            saving = false
            when (event) {
                is AddPastAttackEventUiModel.Problem -> problem = event.problem
                is AddPastAttackEventUiModel.Message ->
                    shownMessage = ShownAttackMessageUiModel(
                        event.message,
                        System.currentTimeMillis()
                    )
            }
        }
    }

    val viewModel = addPastAttackViewModel
    fun send(clearProblem: Boolean, action: (start: Instant, end: Instant) -> Unit) {
        val times = draft ?: return
        if (saving) return
        saving = true
        if (clearProblem) problem = null
        action(
            Instant.ofEpochMilli(times.startEpochMillis),
            Instant.ofEpochMilli(times.endEpochMillis)
        )
    }
    val actions = AddPastAttackActions(
        onBack = viewModel::onLeaveAction,
        onStartPicked = { start ->
            problem = null
            draft = draft?.withStart(start, latestEndEpochMillis = currentMinute())
        },
        onEndPicked = { end ->
            problem = null
            draft = draft?.copy(endEpochMillis = end)
        },
        onAdd = { send(clearProblem = true, viewModel::onAddAction) },
        // The card stays, with its buttons waiting, until the merge answers; a failure leaves it there to try again.
        onMerge = { send(clearProblem = false, viewModel::onMergeAction) },
        onChangeTimes = { problem = null }
    )

    Box(modifier = modifier.fillMaxSize()) {
        AddPastAttackContents(
            draft = draft,
            problem = problem,
            saving = saving,
            timeUiFormatter = attackTimeUiFormatter,
            actions = actions
        )
        AttackMessages(
            shown = shownMessage,
            behaviourMapper = attackMessageBehaviourUiMapper,
            reduceMotion = rememberReduceMotion(),
            isShown = { message -> shownMessage == message },
            onClear = { message -> if (shownMessage == message) shownMessage = null },
            onUndoTimes = {},
            onUndoPoint = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPastAttackContents(
    draft: PastAttackDraftUiModel?,
    problem: PastAttackProblemUiModel?,
    saving: Boolean,
    timeUiFormatter: AttackTimeUiFormatter,
    actions: AddPastAttackActions,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.past_title),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            navigationIcon = {
                IconButton(onClick = actions.onBack) {
                    Icon(
                        MigrelyIcons.Back,
                        contentDescription = stringResource(R.string.attack_navigate_back)
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
        if (draft == null) {
            Box(Modifier.fillMaxSize())
        } else {
            DraftContents(draft, problem, saving, timeUiFormatter, actions)
        }
    }
}

@Composable
private fun DraftContents(
    draft: PastAttackDraftUiModel,
    problem: PastAttackProblemUiModel?,
    saving: Boolean,
    timeUiFormatter: AttackTimeUiFormatter,
    actions: AddPastAttackActions
) {
    var picking by rememberSaveable { mutableStateOf<TimeFieldUiModel?>(null) }
    // Read when a picker opens, so a screen left open for a while still allows the current time.
    val now = remember(picking) { System.currentTimeMillis() }
    val timeError = (problem as? PastAttackProblemUiModel.Time)?.error
    Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .widthIn(max = CONTENT_MAX_WIDTH)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.past_intro),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Column {
                DraftTimeRows(draft, timeError, timeUiFormatter) { field -> picking = field }
                Text(
                    text = stringResource(
                        R.string.time_duration,
                        durationText(draft.endEpochMillis - draft.startEpochMillis)
                    ),
                    style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                )
            }
            if (problem is PastAttackProblemUiModel.Overlaps) {
                // The card's two choices replace "Add migraine", which would only show the card again.
                OverlapCard(problem.attacks, draft.zoneId, timeUiFormatter, saving, actions)
            } else {
                Button(
                    onClick = actions.onAdd,
                    enabled = !saving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                ) {
                    Text(
                        text = stringResource(R.string.past_add),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
    DraftPicker(picking, draft, now, actions) { picking = null }
}

@Composable
private fun DraftTimeRows(
    draft: PastAttackDraftUiModel,
    timeError: TimeErrorUiModel?,
    timeUiFormatter: AttackTimeUiFormatter,
    onPick: (TimeFieldUiModel) -> Unit
) {
    val startText = timeUiFormatter.formatDateTime(draft.startEpochMillis, draft.zoneId)
    TimeRow(
        label = stringResource(R.string.time_started),
        value = startText,
        description = stringResource(R.string.time_change_start, startText),
        error = timeError?.takeIf { it.field == TimeFieldUiModel.Start },
        zoneId = draft.zoneId,
        timeUiFormatter = timeUiFormatter,
        onClick = { onPick(TimeFieldUiModel.Start) }
    )
    val endText = timeUiFormatter.formatDateTime(draft.endEpochMillis, draft.zoneId)
    TimeRow(
        label = stringResource(R.string.time_ended),
        value = endText,
        description = stringResource(R.string.time_change_end, endText),
        error = timeError?.takeIf { it.field == TimeFieldUiModel.End },
        zoneId = draft.zoneId,
        timeUiFormatter = timeUiFormatter,
        onClick = { onPick(TimeFieldUiModel.End) }
    )
}

/** The start can be any minute before the current one; the end runs from a minute after the start to the tolerance. */
@Composable
private fun DraftPicker(
    picking: TimeFieldUiModel?,
    draft: PastAttackDraftUiModel,
    now: Long,
    actions: AddPastAttackActions,
    onClose: () -> Unit
) {
    val zone = zoneOf(draft.zoneId)
    when (picking) {
        TimeFieldUiModel.Start -> MigrelyDateTimePicker(
            initialEpochMillis = draft.startEpochMillis,
            minEpochMillis = EARLIEST_START_MILLIS,
            maxEpochMillis = minuteOf(now) - MILLIS_PER_MINUTE,
            zoneId = zone,
            labels = pickerLabels(
                R.string.picker_start_title,
                R.string.picker_past_start_out_of_range
            ),
            onConfirm = { start ->
                onClose()
                actions.onStartPicked(start)
            },
            onDismiss = onClose
        )

        TimeFieldUiModel.End -> MigrelyDateTimePicker(
            initialEpochMillis = draft.endEpochMillis,
            minEpochMillis = draft.startEpochMillis + MILLIS_PER_MINUTE,
            maxEpochMillis = now + CLOCK_TOLERANCE_MILLIS,
            zoneId = zone,
            labels = pickerLabels(R.string.picker_end_title, R.string.picker_end_out_of_range),
            onConfirm = { end ->
                onClose()
                actions.onEndPicked(end)
            },
            onDismiss = onClose
        )

        null -> Unit
    }
}

/** Names the overlapping migraines and offers both ways out (BRD EC-ATK-05); announced when it appears. */
@Composable
private fun OverlapCard(
    attacks: List<AttackRangeUiModel>,
    zoneId: String,
    timeUiFormatter: AttackTimeUiFormatter,
    saving: Boolean,
    actions: AddPastAttackActions
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = overlapText(attacks, zoneId, timeUiFormatter),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
            Text(
                text = stringResource(R.string.past_merge_explanation),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilledTonalButton(
                onClick = actions.onMerge,
                enabled = !saving,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text(
                    text = stringResource(R.string.time_merge),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            TextButton(
                onClick = actions.onChangeTimes,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text(
                    text = stringResource(R.string.past_change_times),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

private fun minuteOf(epochMillis: Long) = epochMillis / MILLIS_PER_MINUTE * MILLIS_PER_MINUTE

private fun currentMinute() = minuteOf(System.currentTimeMillis())

// 23 October 2026, 17:00 in Tashkent.
private const val PREVIEW_NOW = 1_792_756_800_000L

private val previewDraft = PastAttackDraftUiModel(
    startEpochMillis = PREVIEW_NOW - 7_200_000L,
    endEpochMillis = PREVIEW_NOW,
    zoneId = "Asia/Tashkent"
)

private val previewAddActions = AddPastAttackActions({}, {}, {}, {}, {}, {})

private val previewOverlaps = PastAttackProblemUiModel.Overlaps(
    listOf(
        AttackRangeUiModel(PREVIEW_NOW - 10_800_000L, PREVIEW_NOW - 9_000_000L),
        AttackRangeUiModel(PREVIEW_NOW - 1_800_000L, null)
    )
)

@Preview(name = "Add a past migraine")
@Composable
private fun AddPastAttackPreview() {
    MigrelyTheme {
        AddPastAttackContents(previewDraft, null, false, previewTimeFormatter, previewAddActions)
    }
}

@Preview(name = "Overlapping migraines", heightDp = 900)
@Composable
private fun AddPastAttackOverlapPreview() {
    MigrelyTheme {
        AddPastAttackContents(
            previewDraft,
            previewOverlaps,
            false,
            previewTimeFormatter,
            previewAddActions
        )
    }
}

@Preview(name = "End in the future")
@Composable
private fun AddPastAttackErrorPreview() {
    MigrelyTheme {
        AddPastAttackContents(
            previewDraft,
            PastAttackProblemUiModel.Time(
                TimeErrorUiModel(TimeFieldUiModel.End, TimeErrorReasonUiModel.InFuture)
            ),
            false,
            previewTimeFormatter,
            previewAddActions
        )
    }
}

@Preview(name = "Overlap at 200%", heightDp = 1600, fontScale = 2f)
@Composable
private fun AddPastAttackLargeFontPreview() {
    MigrelyTheme {
        AddPastAttackContents(
            previewDraft,
            previewOverlaps,
            false,
            previewTimeFormatter,
            previewAddActions
        )
    }
}

@Preview(name = "Loading")
@Composable
private fun AddPastAttackLoadingPreview() {
    MigrelyTheme {
        AddPastAttackContents(null, null, false, previewTimeFormatter, previewAddActions)
    }
}
