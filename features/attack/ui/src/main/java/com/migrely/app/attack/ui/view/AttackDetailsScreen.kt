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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import com.migrely.app.architecture.ui.view.ScreenEnterObserver
import com.migrely.app.attack.presentation.model.AttackDetailsViewState
import com.migrely.app.attack.presentation.model.TimeFieldPresentationModel
import com.migrely.app.attack.ui.R
import com.migrely.app.attack.ui.di.AttackDetailsDependencies
import com.migrely.app.attack.ui.mapper.AttackTimeUiFormatter
import com.migrely.app.attack.ui.model.AttackDetailsEventUiModel
import com.migrely.app.attack.ui.model.AttackDetailsUiModel
import com.migrely.app.attack.ui.model.AttackDetailsViewStateUiModel
import com.migrely.app.attack.ui.model.AttackIntakeEffectUiModel
import com.migrely.app.attack.ui.model.AttackIntakeUiModel
import com.migrely.app.attack.ui.model.AttackTagUiModel
import com.migrely.app.attack.ui.model.FunctionalImpactUiModel
import com.migrely.app.attack.ui.model.IntensityPointUiModel
import com.migrely.app.attack.ui.model.ShownAttackMessageUiModel
import com.migrely.app.attack.ui.model.TagKindUiModel
import com.migrely.app.attack.ui.model.TimeErrorUiModel
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.motion.rememberReduceMotion
import com.migrely.app.designsystem.theme.MigrelyTheme
import java.time.Instant
import java.util.Locale

private val CONTENT_MAX_WIDTH = 640.dp

/** Callbacks from the details screen; each already knows the attack it belongs to. */
data class AttackDetailsActions(
    val onBack: () -> Unit,
    val onStartPicked: (epochMillis: Long) -> Unit,
    val onEndPicked: (epochMillis: Long) -> Unit,
    val onMerge: (startEpochMillis: Long, endEpochMillis: Long?) -> Unit,
    val onAddPoint: (point: IntensityPointUiModel) -> Unit,
    val onRemovePoint: (point: IntensityPointUiModel) -> Unit,
    val onTag: (tag: AttackTagUiModel, selected: Boolean) -> Unit,
    val onImpact: (impact: FunctionalImpactUiModel?) -> Unit,
    val onNote: (note: String) -> Unit,
    val onAddCustomTag: (kind: TagKindUiModel, name: String) -> Unit,
    val onArchiveCustomTag: (tagId: String) -> Unit,
    val onDelete: () -> Unit,
    val onAddMedication: () -> Unit = {},
    val onRemoveIntake: (intake: AttackIntakeUiModel) -> Unit = {},
    val onIntakeEffect: (intake: AttackIntakeUiModel, effect: AttackIntakeEffectUiModel) -> Unit =
        { _, _ -> }
)

/**
 * Everything the screen keeps for itself between the ViewModel's states: inline errors, the safety card and the note
 * being typed.
 */
data class AttackDetailsLocalState(
    val timeError: TimeErrorUiModel?,
    val safetyCardVisible: Boolean,
    val onDismissSafetyCard: () -> Unit,
    val onTimeErrorShown: () -> Unit,
    val noteDraft: String? = null,
    val onNoteDraft: (String) -> Unit = {}
)

@Composable
fun AttackDetailsDependencies.AttackDetails(
    attackId: String,
    announceMerge: Boolean,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Entered once per visit, so "Migraines merged." is not repeated after rotation or process death.
    ScreenEnterObserver { attackDetailsViewModel.onEnter(attackId, attacksMerged = announceMerge) }
    LifecycleResumeEffect(attackId) {
        attackDetailsViewModel.onResume(attackId)
        onPauseOrDispose { }
    }
    ViewModelObserver(navController)

    val viewState by attackDetailsViewModel.viewState.collectAsState(AttackDetailsViewState.Loading)
    // One ViewModel serves every details screen, so a late result for another attack is ignored here.
    val uiState by rememberSaveable(viewState) {
        val mapped = attackDetailsViewStateUiMapper.toUi(viewState)
        val otherAttack =
            mapped is AttackDetailsViewStateUiModel.Ready && mapped.details.id != attackId
        mutableStateOf(if (otherAttack) AttackDetailsViewStateUiModel.Loading else mapped)
    }
    var timeError by rememberSaveable(attackId) { mutableStateOf<TimeErrorUiModel?>(null) }
    var safetyCardVisible by rememberSaveable(attackId) { mutableStateOf(false) }
    var noteDraft by rememberSaveable(attackId) { mutableStateOf<String?>(null) }
    var shownMessage by rememberSaveable(attackId) {
        mutableStateOf<ShownAttackMessageUiModel?>(null)
    }

    LaunchedEffect(attackDetailsEventPresenter) {
        attackDetailsEventPresenter.events.collect { event ->
            when (event) {
                is AttackDetailsEventUiModel.TimeRejected -> timeError = event.error
                AttackDetailsEventUiModel.FirstMaximumRecorded -> safetyCardVisible = true
                is AttackDetailsEventUiModel.Message ->
                    shownMessage = ShownAttackMessageUiModel(
                        event.message,
                        System.currentTimeMillis()
                    )
            }
        }
    }

    val details = (uiState as? AttackDetailsViewStateUiModel.Ready)?.details
    val viewModel = attackDetailsViewModel
    val actions = AttackDetailsActions(
        onBack = viewModel::onLeaveAction,
        onStartPicked = { start ->
            timeError = null
            viewModel.onTimesAction(
                attackId,
                TimeFieldPresentationModel.Start,
                Instant.ofEpochMilli(start),
                details?.endEpochMillis?.let(Instant::ofEpochMilli)
            )
        },
        onEndPicked = { end ->
            timeError = null
            viewModel.onTimesAction(
                attackId,
                TimeFieldPresentationModel.End,
                Instant.ofEpochMilli(details?.startEpochMillis ?: end),
                Instant.ofEpochMilli(end)
            )
        },
        onMerge = { start, end ->
            timeError = null
            // The merge stores and joins the note on screen; dropping the draft keeps it from overwriting the result.
            val note = noteDraft
            noteDraft = null
            viewModel.onMergeAction(
                attackId,
                Instant.ofEpochMilli(start),
                end?.let(Instant::ofEpochMilli),
                note
            )
        },
        onAddPoint = { point ->
            viewModel.onAddPointAction(
                attackId,
                intensityPointUiToPresentationMapper
                    .toPresentation(point)
            )
        },
        onRemovePoint = { point ->
            viewModel.onRemovePointAction(
                attackId,
                intensityPointUiToPresentationMapper
                    .toPresentation(point)
            )
        },
        onTag = { tag, selected ->
            viewModel.onTagAction(
                attackId,
                attackTagUiToPresentationMapper
                    .toPresentation(tag),
                selected
            )
        },
        onImpact = { impact ->
            viewModel.onImpactAction(
                attackId,
                impact?.let(functionalImpactUiToPresentationMapper::toPresentation)
            )
        },
        onNote = { note -> viewModel.onNoteAction(attackId, note) },
        onAddCustomTag = { kind, name ->
            viewModel.onAddCustomTagAction(
                attackId,
                tagKindUiToPresentationMapper
                    .toPresentation(kind),
                name
            )
        },
        onArchiveCustomTag = { tagId -> viewModel.onArchiveCustomTagAction(attackId, tagId) },
        onDelete = { viewModel.onDeleteAction(attackId) },
        onAddMedication = { viewModel.onLogMedicationAction(attackId) },
        onRemoveIntake = { intake ->
            viewModel.onRemoveIntakeAction(
                attackId,
                attackIntakeUiToPresentationMapper.toPresentation(intake)
            )
        },
        onIntakeEffect = { intake, effect ->
            viewModel.onIntakeEffectAction(
                attackId,
                intake.id,
                attackIntakeUiToPresentationMapper.toPresentation(effect)
            )
        }
    )

    Box(modifier = modifier.fillMaxSize()) {
        AttackDetailsContents(
            viewState = uiState,
            timeUiFormatter = attackTimeUiFormatter,
            labels = labels,
            localState = AttackDetailsLocalState(
                timeError = timeError,
                safetyCardVisible = safetyCardVisible,
                onDismissSafetyCard = { safetyCardVisible = false },
                onTimeErrorShown = { timeError = null },
                noteDraft = noteDraft,
                onNoteDraft = { noteDraft = it }
            ),
            actions = actions
        )
        AttackMessages(
            shown = shownMessage,
            behaviourMapper = attackMessageBehaviourUiMapper,
            reduceMotion = rememberReduceMotion(),
            isShown = { message -> shownMessage == message },
            onClear = { message -> if (shownMessage == message) shownMessage = null },
            onUndoTimes = { undo ->
                viewModel.onUndoTimesChangeAction(
                    timesUndoUiToPresentationMapper
                        .toPresentation(undo)
                )
            },
            onUndoPoint = { message ->
                viewModel.onUndoPointRemovalAction(
                    message.attackId,
                    intensityPointUiToPresentationMapper.toPresentation(message.point)
                )
            },
            onUndoIntake = { message ->
                viewModel.onUndoIntakeRemovalAction(
                    message.attackId,
                    attackIntakeUiToPresentationMapper.toPresentation(message.intake)
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttackDetailsContents(
    viewState: AttackDetailsViewStateUiModel,
    timeUiFormatter: AttackTimeUiFormatter,
    labels: AttackLabels,
    localState: AttackDetailsLocalState,
    actions: AttackDetailsActions,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val details = (viewState as? AttackDetailsViewStateUiModel.Ready)?.details
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = stringResource(R.string.attack_details_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (details != null) {
                        Text(
                            text = timeUiFormatter.formatDay(
                                details.startEpochMillis,
                                details.startZoneId
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
        when (viewState) {
            AttackDetailsViewStateUiModel.Loading -> Box(Modifier.fillMaxSize())
            AttackDetailsViewStateUiModel.NotFound -> NotFoundContents(actions.onBack)
            is AttackDetailsViewStateUiModel.Ready ->
                ReadyContents(viewState.details, timeUiFormatter, labels, localState, actions)
        }
    }
}

@Composable
private fun ReadyContents(
    details: AttackDetailsUiModel,
    timeUiFormatter: AttackTimeUiFormatter,
    labels: AttackLabels,
    localState: AttackDetailsLocalState,
    actions: AttackDetailsActions
) {
    Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(28.dp),
            modifier = Modifier
                .widthIn(max = CONTENT_MAX_WIDTH)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .padding(horizontal = 16.dp)
        ) {
            if (localState.safetyCardVisible) SafetyCard(onDismiss = localState.onDismissSafetyCard)
            TimeSection(details, timeUiFormatter, localState.timeError, actions)
            PainSection(details, timeUiFormatter, actions)
            MedicationsSection(details, timeUiFormatter, actions)
            ChoiceSections(details, labels, actions)
            ImpactSection(details.functionalImpact, labels, actions.onImpact)
            NoteSection(
                attackId = details.id,
                draft = localState.noteDraft,
                savedNote = details.note,
                onDraftChange = localState.onNoteDraft,
                onNote = actions.onNote
            )
            TextButton(
                onClick = actions.onDelete,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                Text(
                    text = stringResource(R.string.delete_attack),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
internal fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .padding(bottom = 8.dp)
            .semantics { heading() }
    )
}

@Composable
private fun NotFoundContents(onBack: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(48.dp))
        Text(
            text = stringResource(R.string.not_found_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.not_found_back))
        }
    }
}

private val previewActions = AttackDetailsActions(
    onBack = {},
    onStartPicked = {},
    onEndPicked = {},
    onMerge = { _, _ -> },
    onAddPoint = {},
    onRemovePoint = {},
    onTag = { _, _ -> },
    onImpact = {},
    onNote = {},
    onAddCustomTag = { _, _ -> },
    onArchiveCustomTag = {},
    onDelete = {}
)

private val previewLocalState = AttackDetailsLocalState(null, safetyCardVisible = true, {}, {})

internal val previewTimeFormatter = AttackTimeUiFormatter(
    locale = { Locale.UK },
    is24HourFormat = { true },
    deviceZone = { java.time.ZoneId.of("Asia/Tashkent") },
    bestDateTimePattern = { _, skeleton ->
        when (skeleton) {
            "EEEdMMM" -> "EEE d MMM"
            "EEEEdMMMM" -> "EEEE, d MMMM"
            else -> "HH:mm"
        }
    }
)

@Preview(name = "Attack details", heightDp = 1800)
@Composable
private fun AttackDetailsPreview() {
    MigrelyTheme {
        AttackDetailsContents(
            viewState = AttackDetailsViewStateUiModel.Ready(previewDetails()),
            timeUiFormatter = previewTimeFormatter,
            labels = AttackLabels.Default,
            localState = previewLocalState,
            actions = previewActions
        )
    }
}

@Preview(name = "Attack details at 200%", heightDp = 2400, fontScale = 2f)
@Composable
private fun AttackDetailsLargeFontPreview() {
    MigrelyTheme {
        AttackDetailsContents(
            viewState = AttackDetailsViewStateUiModel.Ready(previewDetails()),
            timeUiFormatter = previewTimeFormatter,
            labels = AttackLabels.Default,
            localState = previewLocalState.copy(safetyCardVisible = false),
            actions = previewActions
        )
    }
}

@Preview(name = "Attack not found")
@Composable
private fun AttackNotFoundPreview() {
    MigrelyTheme {
        AttackDetailsContents(
            viewState = AttackDetailsViewStateUiModel.NotFound,
            timeUiFormatter = previewTimeFormatter,
            labels = AttackLabels.Default,
            localState = previewLocalState,
            actions = previewActions
        )
    }
}
