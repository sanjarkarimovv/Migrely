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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import com.migrely.app.architecture.ui.view.ScreenEnterObserver
import com.migrely.app.architecture.ui.view.ScreenLeaveObserver
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
import com.migrely.app.attack.ui.model.AttackMessageUiModel
import com.migrely.app.attack.ui.model.AttackTagUiModel
import com.migrely.app.attack.ui.model.FunctionalImpactUiModel
import com.migrely.app.attack.ui.model.IntensityPointUiModel
import com.migrely.app.attack.ui.model.ShownAttackMessageUiModel
import com.migrely.app.attack.ui.model.TagKindUiModel
import com.migrely.app.attack.ui.model.TimeErrorUiModel
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.motion.rememberReduceMotion
import com.migrely.app.designsystem.snackbar.clearOfMessage
import com.migrely.app.designsystem.theme.MigrelyTheme
import java.time.Instant
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

private val CONTENT_MAX_WIDTH = 640.dp

/**
 * How long a message stays hidden once the safety card has become a live region. Compose sends accessibility events in
 * 100 ms batches, and in one batch TalkBack spoke the message first (#166, 36 ms). Two batches put the card's
 * announcement in an earlier one (BRD D165 (3)).
 */
private const val CARD_FIRST_MILLIS = 200L

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
 * being typed. [safetyCardArrivals] counts the safety card's events since the screen was composed, not since it was
 * restored, so a card that just appeared is brought into view and a restored one is not (BRD D167);
 * [onSafetyCardAnnounced] is told once the card of an event is a live region. [messageClearance] is where a message
 * ends in root coordinates while the content must start below it, and 0 dp while it may lie over it (BRD D165 (1)).
 */
data class AttackDetailsLocalState(
    val timeError: TimeErrorUiModel?,
    val safetyCardVisible: Boolean,
    val onDismissSafetyCard: () -> Unit,
    val onTimeErrorShown: () -> Unit,
    val noteDraft: String? = null,
    val onNoteDraft: (String) -> Unit = {},
    val safetyCardArrivals: Int = 0,
    val onSafetyCardAnnounced: (arrivals: Int) -> Unit = {},
    val messageClearance: () -> Dp = { 0.dp }
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
    // Not saved: a rotation or a process restore brings the card back, but not its event (BRD D167).
    var safetyCardArrivals by remember(attackId) { mutableIntStateOf(0) }
    var noteDraft by rememberSaveable(attackId) { mutableStateOf<String?>(null) }
    var safetyCardAnnounced by remember(attackId) { mutableIntStateOf(0) }
    var shownMessage by rememberSaveable(attackId) {
        mutableStateOf<ShownAttackMessageUiModel?>(null)
    }
    val messageBottom = remember { mutableStateOf(0.dp) }
    // Read by the event collector, which outlives each state.
    val ready by rememberUpdatedState(uiState is AttackDetailsViewStateUiModel.Ready)

    // Not saved: after a rotation or a process restore the card's event is gone, so the message shows at once.
    var messageHeld by remember(attackId) { mutableStateOf(false) }

    // An error belongs to the visit and to the last write (BRD D165 (4), (5)); Undo and information stay.
    fun errorShown(): Boolean =
        shownMessage?.let { attackMessageBehaviourUiMapper.toUi(it.message).isError } == true

    // Stored at once, so a rotation or a leave during the wait neither loses it nor brings it back. While the card of
    // an event has not been announced, it stays hidden: TalkBack speaks the card first (BRD D165 (3)).
    fun post(message: AttackMessageUiModel) {
        shownMessage = ShownAttackMessageUiModel(message, System.currentTimeMillis())
        if (safetyCardAnnounced < safetyCardArrivals) messageHeld = true
    }

    LaunchedEffect(messageHeld) {
        if (messageHeld) {
            snapshotFlow { safetyCardAnnounced >= safetyCardArrivals || !ready }.first { it }
            delay(CARD_FIRST_MILLIS)
            // Its time starts when it can be seen.
            shownMessage = shownMessage?.copy(shownAtEpochMillis = System.currentTimeMillis())
            messageHeld = false
        }
    }

    LaunchedEffect(attackDetailsEventPresenter) {
        attackDetailsEventPresenter.events.collect { event ->
            when (event) {
                is AttackDetailsEventUiModel.TimeRejected -> timeError = event.error
                AttackDetailsEventUiModel.FirstMaximumRecorded -> {
                    safetyCardVisible = true
                    safetyCardArrivals++
                }
                is AttackDetailsEventUiModel.Message -> post(event.message)
                // Cleared on success, never on the attempt, so a retry that fails shows no flash.
                AttackDetailsEventUiModel.Saved -> if (errorShown()) {
                    post(
                        AttackMessageUiModel.Saved
                    )
                }
            }
        }
    }
    ScreenLeaveObserver(navController) { if (errorShown()) shownMessage = null }

    val cardMode = rememberCardMode(
        cardShown = safetyCardVisible && uiState is AttackDetailsViewStateUiModel.Ready,
        messageOnScreen = remember { derivedStateOf { messageBottom.value > 0.dp } }.value
    )

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
                onNoteDraft = { noteDraft = it },
                safetyCardArrivals = safetyCardArrivals,
                onSafetyCardAnnounced = { safetyCardAnnounced = it },
                messageClearance = { if (cardMode) messageBottom.value else 0.dp }
            ),
            actions = actions
        )
        AttackMessages(
            shown = shownMessage.takeUnless { messageHeld },
            behaviourMapper = attackMessageBehaviourUiMapper,
            reduceMotion = rememberReduceMotion(),
            announcePolitely = cardMode,
            onMessageBottomChanged = { messageBottom.value = it },
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

/**
 * "Card mode" (BRD D165 (1)): on while the safety card shows, and held while a message that came in it is still on
 * screen, even once "Got it" closed the card, so the content never slides back under that message. Recomputed from
 * the restored state after a rotation or a process restore.
 */
@Composable
private fun rememberCardMode(cardShown: Boolean, messageOnScreen: Boolean): Boolean {
    var held by remember { mutableStateOf(false) }
    val on = cardShown || (held && messageOnScreen)
    SideEffect { held = on }
    return on
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
    val scrollState = rememberScrollState()
    val arrivals = localState.safetyCardArrivals
    val reveal = rememberSafetyCardReveal(scrollState, arrivals)
    val reduceMotion = rememberReduceMotion()
    // Keyed on the card's event, not on its restored visibility (BRD D167).
    LaunchedEffect(arrivals) {
        if (arrivals > 0) {
            reveal.reveal(arrivals, reduceMotion)
            localState.onSafetyCardAnnounced(arrivals)
        }
    }
    Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(28.dp),
            modifier = Modifier
                .widthIn(max = CONTENT_MAX_WIDTH)
                .fillMaxWidth()
                // Before the reveal's area, so in card mode "the top of the content" is below the message (D165 (2)).
                .clearOfMessage(localState.messageClearance)
                .then(reveal.viewportModifier)
                .verticalScroll(scrollState)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .padding(horizontal = 16.dp)
        ) {
            if (localState.safetyCardVisible) {
                SafetyCard(
                    onDismiss = localState.onDismissSafetyCard,
                    announced = reveal.announces(arrivals),
                    modifier = reveal.cardModifier
                )
            }
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

internal val previewActions = AttackDetailsActions(
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
