package com.migrely.app.medication.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import com.migrely.app.architecture.ui.view.ScreenEnterObserver
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.motion.rememberReduceMotion
import com.migrely.app.designsystem.picker.DateTimePickerLabels
import com.migrely.app.designsystem.picker.MigrelyDateTimePicker
import com.migrely.app.designsystem.theme.MigrelyColors
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.medication.presentation.model.LogMedicationViewState
import com.migrely.app.medication.ui.R
import com.migrely.app.medication.ui.di.LogMedicationDependencies
import com.migrely.app.medication.ui.mapper.MedicationTimeUiFormatter
import com.migrely.app.medication.ui.model.IntakeUiModel
import com.migrely.app.medication.ui.model.LogMedicationViewStateUiModel
import com.migrely.app.medication.ui.model.MedicationCategoryUiModel
import com.migrely.app.medication.ui.model.MedicationEventUiModel
import com.migrely.app.medication.ui.model.MedicationKindUiModel
import com.migrely.app.medication.ui.model.MedicationLogUiModel
import com.migrely.app.medication.ui.model.MedicationUiModel
import com.migrely.app.medication.ui.model.SaveProblemUiModel
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

/** The earliest time an intake can be logged at: 1 January 2000. */
private const val EARLIEST_MILLIS = 946_684_800_000L
private const val CLOCK_TOLERANCE_MILLIS = 300_000L
private const val MAXIMUM_NAME_LENGTH = 60
private val CONTENT_MAX_WIDTH = 600.dp

/**
 * "Medication taken" (BRD MED-F3, D34, EC-MED-08). The picked time, the first-use form, the intake just logged and the
 * pending tap are saveable, so rotation and process death keep them and a double tap logs once. [darkRoom] keeps attack
 * mode's dim colours and large controls when opened during an attack (BRD 18.3 B, EC-UX-02).
 */
@Composable
fun LogMedicationDependencies.LogMedication(
    navController: NavController,
    attackId: String?,
    darkRoom: Boolean,
    modifier: Modifier = Modifier
) {
    ScreenEnterObserver { logMedicationViewModel.onEnter(attackId) }
    // Coming back from My medications shows the list as it is now.
    LifecycleResumeEffect(logMedicationViewModel) {
        logMedicationViewModel.onEnter(attackId)
        onPauseOrDispose { }
    }
    ViewModelObserver(navController)

    val viewState by logMedicationViewModel.viewState.collectAsState(LogMedicationViewState.Loading)
    val uiState by rememberSaveable(viewState) {
        mutableStateOf(logMedicationViewStateUiMapper.toUi(viewState))
    }
    // Null until the user picks a time, so the default follows every reload: "now" stays now (BRD D34).
    var pickedTime by rememberSaveable { mutableStateOf<Long?>(null) }
    var logged by rememberSaveable { mutableStateOf<IntakeUiModel?>(null) }
    // Not saved: a write still running when the screen is recreated must not leave every button disabled.
    var pending by remember { mutableStateOf(false) }
    var problem by rememberSaveable { mutableStateOf<SaveProblemUiModel?>(null) }
    val log = (uiState as? LogMedicationViewStateUiModel.Ready)?.log
    val shownTime = pickedTime ?: log?.defaultTimeEpochMillis
    LaunchedEffect(medicationEventPresenter) {
        medicationEventPresenter.events.collect { event ->
            pending = false
            when (event) {
                is MedicationEventUiModel.Logged -> {
                    logged = event.intake
                    problem = null
                }

                is MedicationEventUiModel.NotSaved -> problem = event.problem
                is MedicationEventUiModel.Message -> Unit
            }
        }
    }
    val reduceMotion = rememberReduceMotion()

    LogMedicationContents(
        log = log,
        shownTime = shownTime,
        logged = logged,
        problem = problem,
        pending = pending,
        darkRoom = darkRoom,
        timeUiFormatter = medicationTimeUiFormatter,
        actions = LogMedicationActions(
            onBack = logMedicationViewModel::onDoneAction,
            onTimePicked = { pickedTime = it },
            onMedication = { medication ->
                val time = shownTime
                if (!pending && log != null && time != null) {
                    pending = true
                    logMedicationViewModel.onMedicationAction(
                        medicationUiToPresentationMapper.toPresentation(medication),
                        log.attackId,
                        Instant.ofEpochMilli(time)
                    )
                }
            },
            onAddAndLog = { name, category ->
                val time = shownTime
                if (!pending && log != null && time != null) {
                    pending = true
                    logMedicationViewModel.onAddAndLogAction(
                        name,
                        medicationCategoryUiToPresentationMapper.toPresentation(category),
                        log.attackId,
                        Instant.ofEpochMilli(time)
                    )
                }
            },
            onUndo = {
                logged?.let { intake ->
                    logged = null
                    // The screen's own attack, not the one the intake joined, so a later time links afresh.
                    logMedicationViewModel.onUndoAction(intake.id, attackId)
                }
            },
            onDone = logMedicationViewModel::onDoneAction,
            onManage = logMedicationViewModel::onManageAction
        ),
        messages = {
            MedicationMessages(
                presenter = medicationEventPresenter,
                behaviourMapper = medicationMessageBehaviourUiMapper,
                reduceMotion = reduceMotion
            )
        },
        modifier = modifier
    )
}

/** Callbacks from "Medication taken" to its ViewModel and saveable state. */
data class LogMedicationActions(
    val onBack: () -> Unit,
    val onTimePicked: (epochMillis: Long) -> Unit,
    val onMedication: (MedicationUiModel) -> Unit,
    val onAddAndLog: (name: String, category: MedicationCategoryUiModel?) -> Unit,
    val onUndo: () -> Unit,
    val onDone: () -> Unit,
    val onManage: () -> Unit
)

/** The colours and sizes of the normal screen or of the dark room (BRD 18.3 B). */
private data class LogPalette(
    val background: Color,
    val surface: Color,
    val content: Color,
    // The colour of a value that can be changed, as on the details screen; never white in the dark room (EC-UX-01).
    val accent: Color,
    // The main action and the quiet one, dim in the dark room like "Migraine ended" and "+ Medication taken".
    val actionContainer: Color,
    val actionContent: Color,
    val outline: Color,
    val quietContent: Color,
    val selection: Color,
    val textField: TextFieldColors,
    val buttonHeight: Dp
)

@Composable
private fun LogPalette.actionColors() =
    ButtonDefaults.buttonColors(containerColor = actionContainer, contentColor = actionContent)

@Composable
private fun palette(darkRoom: Boolean) = if (darkRoom) {
    LogPalette(
        background = MigrelyColors.AttackModeBackground,
        surface = MigrelyColors.AttackModeSurface,
        content = MigrelyColors.AttackModeText,
        accent = MigrelyColors.AttackModeSelectedText,
        actionContainer = MigrelyColors.AttackModeEndContainer,
        actionContent = MigrelyColors.AttackModeEndText,
        outline = MigrelyColors.AttackModeOutline,
        quietContent = MigrelyColors.AttackModeText,
        selection = MigrelyColors.AttackModeSelectedOutline,
        // Boundaries in the dim text colour keep 3:1 against the dark room (WCAG 1.4.11).
        textField = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MigrelyColors.AttackModeEndText,
            unfocusedTextColor = MigrelyColors.AttackModeEndText,
            cursorColor = MigrelyColors.AttackModeSelectedOutline,
            focusedBorderColor = MigrelyColors.AttackModeSelectedOutline,
            unfocusedBorderColor = MigrelyColors.AttackModeText,
            focusedLabelColor = MigrelyColors.AttackModeSelectedOutline,
            unfocusedLabelColor = MigrelyColors.AttackModeText,
            focusedSupportingTextColor = MigrelyColors.AttackModeText,
            unfocusedSupportingTextColor = MigrelyColors.AttackModeText
        ),
        buttonHeight = 64.dp
    )
} else {
    LogPalette(
        background = MaterialTheme.colorScheme.background,
        surface = MaterialTheme.colorScheme.surfaceContainer,
        content = MaterialTheme.colorScheme.onSurface,
        accent = MaterialTheme.colorScheme.primary,
        actionContainer = MaterialTheme.colorScheme.primary,
        actionContent = MaterialTheme.colorScheme.onPrimary,
        outline = MaterialTheme.colorScheme.outline,
        quietContent = MaterialTheme.colorScheme.primary,
        selection = MaterialTheme.colorScheme.primary,
        textField = OutlinedTextFieldDefaults.colors(),
        buttonHeight = 56.dp
    )
}

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogMedicationContents(
    log: MedicationLogUiModel?,
    shownTime: Long?,
    logged: IntakeUiModel?,
    problem: SaveProblemUiModel?,
    pending: Boolean,
    darkRoom: Boolean,
    timeUiFormatter: MedicationTimeUiFormatter,
    actions: LogMedicationActions,
    messages: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = palette(darkRoom)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.log_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.content
                    )
                },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(
                            MigrelyIcons.Back,
                            contentDescription = stringResource(R.string.navigate_back),
                            tint = colors.content
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background),
                windowInsets = WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                )
            )
            Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .widthIn(max = CONTENT_MAX_WIDTH)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    when {
                        logged != null -> LoggedContents(logged, colors, timeUiFormatter, actions)
                        log == null || shownTime == null -> Unit
                        else -> {
                            TimeRow(shownTime, log.zoneId, colors, timeUiFormatter, actions)
                            if (log.medications.isEmpty()) {
                                FirstMedicationForm(problem, pending, colors, actions)
                            } else {
                                log.medications.forEach { medication ->
                                    MedicationButton(medication, pending, colors) {
                                        actions.onMedication(medication)
                                    }
                                }
                                TextButton(
                                    onClick = actions.onManage,
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = colors.accent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = colors.buttonHeight)
                                ) {
                                    Text(stringResource(R.string.log_manage))
                                }
                            }
                        }
                    }
                }
            }
        }
        messages()
    }
}

@Composable
private fun TimeRow(
    shownTime: Long,
    zoneId: String,
    colors: LogPalette,
    timeUiFormatter: MedicationTimeUiFormatter,
    actions: LogMedicationActions
) {
    var picking by rememberSaveable { mutableStateOf(false) }
    val now = remember(picking) { System.currentTimeMillis() }
    val time = timeUiFormatter.formatTime(shownTime, zoneId, now)
    // Read as "Taken at, 14:20, button": the surface merges its two texts.
    Surface(
        onClick = { picking = true },
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = colors.buttonHeight)
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(
                text = stringResource(R.string.log_taken_at),
                style = MaterialTheme.typography.labelMedium,
                color = colors.content
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
                color = colors.accent
            )
        }
    }
    if (picking) {
        MigrelyDateTimePicker(
            initialEpochMillis = shownTime,
            minEpochMillis = EARLIEST_MILLIS,
            maxEpochMillis = now + CLOCK_TOLERANCE_MILLIS,
            zoneId = runCatching { ZoneId.of(zoneId) }.getOrDefault(ZoneId.systemDefault()),
            labels = DateTimePickerLabels(
                title = stringResource(R.string.picker_title),
                next = stringResource(R.string.picker_next),
                cancel = stringResource(R.string.picker_cancel),
                confirm = stringResource(R.string.picker_confirm),
                outOfRange = stringResource(R.string.picker_out_of_range)
            ),
            onConfirm = {
                picking = false
                actions.onTimePicked(it)
            },
            onDismiss = { picking = false }
        )
    }
}

@Composable
private fun MedicationButton(
    medication: MedicationUiModel,
    pending: Boolean,
    colors: LogPalette,
    onClick: () -> Unit
) {
    val preventive = medication.kind == MedicationKindUiModel.Preventive
    val title = medication.title()
    val what = if (preventive) "$title, ${medication.typeLabel()}" else title
    val description = stringResource(R.string.log_medication_description, what)
    Surface(
        onClick = onClick,
        enabled = !pending,
        shape = RoundedCornerShape(20.dp),
        color = colors.surface,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = colors.buttonHeight)
            // Merged into the clickable (EC-UX-19); the texts inside are cleared so it is read once.
            .semantics {
                contentDescription = description
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .clearAndSetSemantics { }
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = medication.title(),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.content
                )
                if (preventive) {
                    Text(
                        text = medication.typeLabel(),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.content
                    )
                }
            }
        }
    }
}

@Composable
private fun LoggedContents(
    logged: IntakeUiModel,
    colors: LogPalette,
    timeUiFormatter: MedicationTimeUiFormatter,
    actions: LogMedicationActions
) {
    val now = remember(logged) { System.currentTimeMillis() }
    val what = listOfNotNull(logged.medicationName, logged.dose).joinToString(" ")
    Text(
        text = stringResource(
            R.string.log_logged,
            what,
            timeUiFormatter.formatTime(logged.takenAtEpochMillis, logged.zoneId, now)
        ),
        style = MaterialTheme.typography.titleMedium,
        color = colors.content,
        // Announced, since the tapped button has just gone (EC-UX-19).
        modifier = Modifier
            .padding(vertical = 16.dp)
            .semantics { liveRegion = LiveRegionMode.Polite }
    )
    Button(
        onClick = actions.onDone,
        colors = colors.actionColors(),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = colors.buttonHeight)
    ) {
        Text(stringResource(R.string.log_done), style = MaterialTheme.typography.labelLarge)
    }
    OutlinedButton(
        onClick = actions.onUndo,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.quietContent),
        border = BorderStroke(1.dp, colors.outline),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = colors.buttonHeight)
    ) {
        Text(stringResource(R.string.log_undo), style = MaterialTheme.typography.labelLarge)
    }
}

/** No medication yet: add one with its type and log it at once (BRD EC-MED-08). */
@Composable
private fun FirstMedicationForm(
    problem: SaveProblemUiModel?,
    pending: Boolean,
    colors: LogPalette,
    actions: LogMedicationActions
) {
    var name by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<MedicationCategoryUiModel?>(null) }
    Text(
        text = stringResource(R.string.log_first_title),
        style = MaterialTheme.typography.titleMedium,
        color = colors.content,
        modifier = Modifier.padding(top = 8.dp)
    )
    Text(
        text = stringResource(R.string.log_first_body),
        style = MaterialTheme.typography.bodyMedium,
        color = colors.content
    )
    val nameProblem = problem.takeIf {
        it is SaveProblemUiModel.Duplicate ||
            it is SaveProblemUiModel.ArchivedName ||
            it == SaveProblemUiModel.InvalidName
    }
    OutlinedTextField(
        value = name,
        onValueChange = { name = it.take(MAXIMUM_NAME_LENGTH) },
        label = { Text(stringResource(R.string.edit_name)) },
        singleLine = true,
        isError = nameProblem != null,
        supportingText = {
            Text(
                nameProblem?.let { problemText(it) } ?: stringResource(
                    R.string.edit_counter,
                    name.length,
                    MAXIMUM_NAME_LENGTH
                )
            )
        },
        colors = colors.textField,
        modifier = Modifier.fillMaxWidth()
    )
    CategoryChoices(category, colors.content, colors.selection, colors.buttonHeight) {
        category = it
    }
    Button(
        onClick = { actions.onAddAndLog(name, category) },
        enabled = !pending,
        colors = colors.actionColors(),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = colors.buttonHeight)
    ) {
        Text(stringResource(R.string.log_add_and_log), style = MaterialTheme.typography.labelLarge)
    }
}

/** Radio choices for an acute medication's type, with examples (BRD MED-F2). */
@Composable
internal fun CategoryChoices(
    selected: MedicationCategoryUiModel?,
    content: Color,
    selection: Color,
    rowHeight: Dp,
    onSelect: (MedicationCategoryUiModel?) -> Unit
) {
    Text(
        text = stringResource(R.string.edit_category),
        style = MaterialTheme.typography.labelLarge,
        color = content
    )
    Column(Modifier.selectableGroup()) {
        CATEGORY_CHOICES.forEach { choice ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = rowHeight)
                    .selectable(
                        selected = choice == selected,
                        role = Role.RadioButton,
                        onClick = { onSelect(choice) }
                    )
            ) {
                RadioButton(
                    selected = choice == selected,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = selection,
                        unselectedColor = content
                    )
                )
                Column(Modifier.padding(start = 12.dp)) {
                    Text(
                        text = categoryLabel(choice),
                        style = MaterialTheme.typography.bodyLarge,
                        color = content
                    )
                    categoryExamples(choice)?.let {
                        Text(text = it, style = MaterialTheme.typography.bodySmall, color = content)
                    }
                }
            }
        }
    }
}

private val previewFormatter = MedicationTimeUiFormatter(
    locale = { Locale.UK },
    is24HourFormat = { true },
    deviceZone = { ZoneId.of("Asia/Tashkent") },
    bestDateTimePattern = { _, skeleton -> if (skeleton == "EEEdMMM") "EEE d MMM" else "HH:mm" }
)

private val previewActions = LogMedicationActions({}, {}, {}, { _, _ -> }, {}, {}, {})

private val previewLog = MedicationLogUiModel(
    defaultTimeEpochMillis = 1_792_829_200_000L,
    zoneId = "Asia/Tashkent",
    attackId = "a",
    medications = listOf(
        MedicationUiModel(
            "i",
            "Ibuprofen",
            MedicationKindUiModel.Acute,
            MedicationCategoryUiModel.SimplePainkiller,
            "400 mg"
        ),
        MedicationUiModel(
            "s",
            "Sumatriptan",
            MedicationKindUiModel.Acute,
            MedicationCategoryUiModel.Triptan,
            "50 mg"
        )
    )
)

@Preview(name = "Medication taken: dark room")
@Composable
private fun LogMedicationDarkPreview() {
    MigrelyTheme {
        LogMedicationContents(
            log = previewLog,
            shownTime = previewLog.defaultTimeEpochMillis,
            logged = null,
            problem = null,
            pending = false,
            darkRoom = true,
            timeUiFormatter = previewFormatter,
            actions = previewActions,
            messages = {}
        )
    }
}

@Preview(name = "Medication taken: first use")
@Composable
private fun LogMedicationFirstUsePreview() {
    MigrelyTheme {
        LogMedicationContents(
            log = previewLog.copy(medications = emptyList()),
            shownTime = previewLog.defaultTimeEpochMillis,
            logged = null,
            problem = null,
            pending = false,
            darkRoom = false,
            timeUiFormatter = previewFormatter,
            actions = previewActions,
            messages = {}
        )
    }
}

@Preview(name = "Medication taken: logged")
@Composable
private fun LogMedicationLoggedPreview() {
    MigrelyTheme {
        LogMedicationContents(
            log = previewLog,
            shownTime = previewLog.defaultTimeEpochMillis,
            logged = IntakeUiModel(
                "x",
                "Ibuprofen",
                "400 mg",
                1_792_829_200_000L,
                "Asia/Tashkent",
                "a"
            ),
            problem = null,
            pending = false,
            darkRoom = true,
            timeUiFormatter = previewFormatter,
            actions = previewActions,
            messages = {}
        )
    }
}
