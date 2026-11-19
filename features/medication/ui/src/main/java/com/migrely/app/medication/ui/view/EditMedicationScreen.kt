package com.migrely.app.medication.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.migrely.app.architecture.ui.view.ScreenEnterObserver
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.input.SingleLineFieldInView
import com.migrely.app.designsystem.input.TapGuard
import com.migrely.app.designsystem.input.rememberKeyboardRoom
import com.migrely.app.designsystem.input.rememberLimitedField
import com.migrely.app.designsystem.input.rememberTypingSpace
import com.migrely.app.designsystem.layout.MigrelyInsets
import com.migrely.app.designsystem.motion.rememberReduceMotion
import com.migrely.app.designsystem.snackbar.MessageLayout
import com.migrely.app.designsystem.snackbar.rememberParkWhenPressed
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.medication.ui.R
import com.migrely.app.medication.ui.di.EditMedicationDependencies
import com.migrely.app.medication.ui.model.EditMedicationViewStateUiModel
import com.migrely.app.medication.ui.model.MedicationCategoryUiModel
import com.migrely.app.medication.ui.model.MedicationEventUiModel
import com.migrely.app.medication.ui.model.MedicationFormUiModel
import com.migrely.app.medication.ui.model.MedicationKindUiModel
import com.migrely.app.medication.ui.model.SaveProblemUiModel

private const val MAXIMUM_NAME_LENGTH = 60
private const val MAXIMUM_DOSE_LENGTH = 20
private const val KIND_TAP_GUARD_MILLIS = 700L
private val FORM_SPACING = 12.dp
private val CONTENT_MAX_WIDTH = 600.dp

/** Adds or edits a medication (BRD MED-F1, MED-F2, D35). [medicationId] is null when adding. */
@Composable
fun EditMedicationDependencies.EditMedication(
    navController: NavController,
    medicationId: String?,
    modifier: Modifier = Modifier
) {
    // All the screen holds belongs to one medication, so another id starts it afresh (issue #138).
    key(medicationId) { MedicationEditor(navController, medicationId, modifier) }
}

@Composable
private fun EditMedicationDependencies.MedicationEditor(
    navController: NavController,
    medicationId: String?,
    modifier: Modifier
) {
    var form by rememberSaveable { mutableStateOf<MedicationFormUiModel?>(null) }
    var notFound by rememberSaveable { mutableStateOf(false) }
    // Counts NotSaved results, so the same problem twice in a row is still a new result (review F2).
    var problemArrivals by remember { mutableIntStateOf(0) }
    var problem by rememberSaveable { mutableStateOf<SaveProblemUiModel?>(null) }
    // Not saved: a write still running when the screen is recreated must not leave Save disabled.
    var pending by remember { mutableStateOf(false) }
    // One ViewModel serves every editor, so a late read of another medication can arrive here. Save
    // writes medicationId, so the form only ever takes that row. Each read is taken as it comes: the
    // screen asks once, so a conflated state could hide the right read behind a late one for good.
    LaunchedEffect(editMedicationViewModel) {
        editMedicationViewModel.viewState.collect { state ->
            when (val read = editMedicationViewStateUiMapper.toUi(state)) {
                is EditMedicationViewStateUiModel.Ready ->
                    if (form == null && read.medication?.id == medicationId) {
                        form = read.medication?.let {
                            MedicationFormUiModel(
                                it.name,
                                it.kind,
                                it.category,
                                it.defaultDose.orEmpty()
                            )
                        } ?: MedicationFormUiModel()
                    }

                EditMedicationViewStateUiModel.NotFound -> notFound = true
                EditMedicationViewStateUiModel.Loading -> Unit
            }
        }
    }
    ScreenEnterObserver { editMedicationViewModel.onEnter(medicationId) }
    ViewModelObserver(navController)
    LaunchedEffect(medicationEventPresenter) {
        medicationEventPresenter.events.collect { event ->
            pending = false
            if (event is MedicationEventUiModel.NotSaved) {
                problem = event.problem
                problemArrivals++
            }
        }
    }
    val reduceMotion = rememberReduceMotion()
    EditMedicationContents(
        adding = medicationId == null,
        // Carries no id, so it may be another medication's: a form that loaded wins.
        notFound = notFound && form == null,
        form = form,
        problem = problem,
        problemArrivals = problemArrivals,
        pending = pending,
        onFormChange = {
            form = it
            problem = null
        },
        onSave = {
            val entered = form
            if (!pending && entered != null) {
                pending = true
                editMedicationViewModel.onSaveAction(
                    medicationId,
                    entered.name,
                    medicationKindUiToPresentationMapper.toPresentation(entered.kind),
                    medicationCategoryUiToPresentationMapper.toPresentation(entered.category),
                    entered.dose
                )
            }
        },
        onBack = editMedicationViewModel::onLeaveAction,
        messages = {
            MedicationMessages(
                presenter = medicationEventPresenter,
                navController = navController,
                behaviourMapper = medicationMessageBehaviourUiMapper,
                reduceMotion = reduceMotion
            )
        },
        modifier = modifier
    )
}

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMedicationContents(
    adding: Boolean,
    notFound: Boolean,
    form: MedicationFormUiModel?,
    problem: SaveProblemUiModel?,
    pending: Boolean,
    onFormChange: (MedicationFormUiModel) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    messages: @Composable () -> Unit,
    problemArrivals: Int = 0,
    modifier: Modifier = Modifier
) {
    // The line being typed always shows: the bar steps aside where it cannot (BRD D192, #245).
    val typingSpace = rememberTypingSpace()
    MessageLayout(
        message = messages,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (adding) R.string.edit_title_add else R.string.edit_title_edit
                        ),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            MigrelyIcons.Back,
                            contentDescription = stringResource(R.string.medication_navigate_back)
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
        },
        typingSpace = typingSpace
    ) {
        Column(Modifier.fillMaxSize()) {
            val scrollState = rememberScrollState()
            val viewportInsets = MigrelyInsets.barsAndKeyboard.only(WindowInsetsSides.Bottom)
            // Closing the keyboard leaves the page where it is, so TalkBack stays on the field (BRD D189, #238).
            val keyboardRoom = rememberKeyboardRoom(scrollState, viewportInsets)
            val announcer = rememberProblemAnnouncer()
            Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(FORM_SPACING),
                    modifier = Modifier
                        .widthIn(max = CONTENT_MAX_WIDTH)
                        .fillMaxWidth()
                        // Before the scroll, so its visible part ends at the keyboard's top and a focused
                        // field is kept above it (BRD D180 (2)).
                        .windowInsetsPadding(viewportInsets)
                        .then(typingSpace.page(scrollState))
                        .verticalScroll(scrollState)
                        // After the scroll, never a child: the column's spacing would add a gap.
                        .then(keyboardRoom.modifier)
                        .windowInsetsPadding(
                            MigrelyInsets.barsAndKeyboard.only(WindowInsetsSides.Horizontal)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    when {
                        notFound -> Text(
                            stringResource(R.string.edit_not_found),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        form != null -> MedicationForm(
                            form,
                            problem,
                            problemArrivals,
                            pending,
                            announcer,
                            onFormChange,
                            onSave
                        )
                    }
                }
                ProblemAnnouncement(announcer, Modifier.align(Alignment.TopStart))
            }
        }
    }
}

@Composable
private fun MedicationForm(
    form: MedicationFormUiModel,
    problem: SaveProblemUiModel?,
    problemArrivals: Int,
    pending: Boolean,
    announcer: ProblemAnnouncer,
    onFormChange: (MedicationFormUiModel) -> Unit,
    onSave: () -> Unit
) {
    val nameProblem = problem.takeIf {
        it is SaveProblemUiModel.Duplicate ||
            it is SaveProblemUiModel.ArchivedName ||
            it == SaveProblemUiModel.InvalidName
    }
    val nameField = rememberLimitedField(form.name, MAXIMUM_NAME_LENGTH) {
        onFormChange(form.copy(name = it))
    }
    val doseField = rememberLimitedField(form.dose, MAXIMUM_DOSE_LENGTH) {
        onFormChange(form.copy(dose = it))
    }
    val doseProblem = problem.takeIf { it == SaveProblemUiModel.InvalidDose }
    val nameText = nameProblem?.let { problemText(it) }
    val doseText = doseProblem?.let { problemText(it) }
    val name = remember { ProblemField() }
    val dose = remember { ProblemField() }
    // One result, one field: Name before Usual dose (ux F1).
    val (problemField, problemFieldValue) = when {
        nameProblem != null -> name to nameField
        doseProblem != null -> dose to doseField
        else -> null to null
    }
    FieldProblemFeedback(
        arrivals = problemArrivals,
        pending = pending,
        field = problemField,
        problem = nameText ?: doseText,
        announcer = announcer,
        onFocusMoving = { problemFieldValue?.cursorToEnd() }
    )
    // Save turns disabled while it waits (BRD D181 (6)).
    val save = rememberParkWhenPressed()
    // Kept above the keyboard with its supporting line where that fits (BRD D180 (2), ux U1).
    SingleLineFieldInView(
        hasLabel = true,
        inViewRequests = name.inViewRequests
    ) { inView, interactionSource ->
        OutlinedTextField(
            value = nameField.value,
            onValueChange = nameField.onValueChange,
            label = { Text(stringResource(R.string.edit_name)) },
            singleLine = true,
            isError = nameProblem != null,
            supportingText = {
                FieldSupportingLine(
                    nameText,
                    stringResource(R.string.edit_counter, form.name.length, MAXIMUM_NAME_LENGTH)
                )
            },
            interactionSource = interactionSource,
            modifier = inView
                .then(name.modifier)
                .problemSemantics(nameText)
                .fillMaxWidth()
        )
    }
    Text(stringResource(R.string.edit_kind), style = MaterialTheme.typography.labelLarge)
    Box(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(FORM_SPACING)) {
            Column(Modifier.selectableGroup()) {
                listOf(
                    MedicationKindUiModel.Acute to R.string.kind_acute,
                    MedicationKindUiModel.Preventive to R.string.kind_preventive
                ).forEach { (kind, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .selectable(
                                selected = form.kind == kind,
                                role = Role.RadioButton,
                                onClick = { onFormChange(form.copy(kind = kind)) }
                            )
                    ) {
                        RadioButton(selected = form.kind == kind, onClick = null)
                        Text(
                            stringResource(label),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }
            if (form.kind == MedicationKindUiModel.Acute) {
                CategoryChoices(
                    form.category,
                    MaterialTheme.colorScheme.onSurface,
                    MaterialTheme.colorScheme.primary,
                    48.dp
                ) {
                    onFormChange(form.copy(category = it))
                }
            }
        }
        // A second tap meant for the list must not flip the kind or the category: Preventive leaves
        // the overuse count (issue #140). Longer than the screen's fade, for the gaps measured up to
        // 649 ms. Name, dose, Save and Back stay free.
        TapGuard(
            key = Unit,
            durationMillis = KIND_TAP_GUARD_MILLIS,
            modifier = Modifier.matchParentSize()
        )
    }
    SingleLineFieldInView(
        hasLabel = true,
        inViewRequests = dose.inViewRequests
    ) { inView, interactionSource ->
        OutlinedTextField(
            value = doseField.value,
            onValueChange = doseField.onValueChange,
            label = { Text(stringResource(R.string.edit_dose)) },
            placeholder = { Text(stringResource(R.string.edit_dose_hint)) },
            singleLine = true,
            isError = doseProblem != null,
            supportingText = {
                FieldSupportingLine(
                    doseText,
                    stringResource(R.string.edit_counter, form.dose.length, MAXIMUM_DOSE_LENGTH)
                )
            },
            interactionSource = interactionSource,
            modifier = inView
                .then(dose.modifier)
                .problemSemantics(doseText)
                .fillMaxWidth()
        )
    }
    Button(
        onClick = {
            save.pressed()
            onSave()
        },
        enabled = !pending,
        modifier = save.modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
    ) {
        Text(stringResource(R.string.edit_save), style = MaterialTheme.typography.labelLarge)
    }
}

@Preview(name = "Edit medication")
@Composable
private fun EditMedicationPreview() {
    MigrelyTheme {
        EditMedicationContents(
            adding = false,
            notFound = false,
            form = MedicationFormUiModel(
                "Ibuprofen",
                MedicationKindUiModel.Acute,
                MedicationCategoryUiModel.SimplePainkiller,
                "400 mg"
            ),
            problem = SaveProblemUiModel.Duplicate("Ibuprofen"),
            pending = false,
            onFormChange = {},
            onSave = {},
            onBack = {},
            messages = {}
        )
    }
}
