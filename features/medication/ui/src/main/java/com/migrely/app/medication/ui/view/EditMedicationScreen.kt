package com.migrely.app.medication.ui.view

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.migrely.app.designsystem.motion.rememberReduceMotion
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.medication.presentation.model.EditMedicationViewState
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
private val CONTENT_MAX_WIDTH = 600.dp

/** Adds or edits a medication (BRD MED-F1, MED-F2, D35). [medicationId] is null when adding. */
@Composable
fun EditMedicationDependencies.EditMedication(
    navController: NavController,
    medicationId: String?,
    modifier: Modifier = Modifier
) {
    ScreenEnterObserver { editMedicationViewModel.onEnter(medicationId) }
    ViewModelObserver(navController)
    val viewState by editMedicationViewModel.viewState.collectAsState(
        EditMedicationViewState.Loading
    )
    val uiState by rememberSaveable(viewState) {
        mutableStateOf(editMedicationViewStateUiMapper.toUi(viewState))
    }
    var form by rememberSaveable { mutableStateOf<MedicationFormUiModel?>(null) }
    var problem by rememberSaveable { mutableStateOf<SaveProblemUiModel?>(null) }
    // Not saved: a write still running when the screen is recreated must not leave Save disabled.
    var pending by remember { mutableStateOf(false) }
    LaunchedEffect(uiState) {
        val ready = uiState as? EditMedicationViewStateUiModel.Ready
        if (form == null && ready != null) {
            form = ready.medication?.let {
                MedicationFormUiModel(it.name, it.kind, it.category, it.defaultDose.orEmpty())
            } ?: MedicationFormUiModel()
        }
    }
    LaunchedEffect(medicationEventPresenter) {
        medicationEventPresenter.events.collect { event ->
            pending = false
            if (event is MedicationEventUiModel.NotSaved) problem = event.problem
        }
    }
    val reduceMotion = rememberReduceMotion()
    EditMedicationContents(
        adding = medicationId == null,
        notFound = uiState == EditMedicationViewStateUiModel.NotFound,
        form = form,
        problem = problem,
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
                        notFound -> Text(
                            stringResource(R.string.edit_not_found),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        form != null -> MedicationForm(form, problem, pending, onFormChange, onSave)
                    }
                }
            }
        }
        messages()
    }
}

@Composable
private fun MedicationForm(
    form: MedicationFormUiModel,
    problem: SaveProblemUiModel?,
    pending: Boolean,
    onFormChange: (MedicationFormUiModel) -> Unit,
    onSave: () -> Unit
) {
    val nameProblem = problem.takeIf {
        it is SaveProblemUiModel.Duplicate ||
            it is SaveProblemUiModel.ArchivedName ||
            it == SaveProblemUiModel.InvalidName
    }
    OutlinedTextField(
        value = form.name,
        onValueChange = { onFormChange(form.copy(name = it.take(MAXIMUM_NAME_LENGTH))) },
        label = { Text(stringResource(R.string.edit_name)) },
        singleLine = true,
        isError = nameProblem != null,
        supportingText = {
            Text(
                nameProblem?.let { problemText(it) }
                    ?: stringResource(R.string.edit_counter, form.name.length, MAXIMUM_NAME_LENGTH)
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
    Text(stringResource(R.string.edit_kind), style = MaterialTheme.typography.labelLarge)
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
    OutlinedTextField(
        value = form.dose,
        onValueChange = { onFormChange(form.copy(dose = it.take(MAXIMUM_DOSE_LENGTH))) },
        label = { Text(stringResource(R.string.edit_dose)) },
        placeholder = { Text(stringResource(R.string.edit_dose_hint)) },
        singleLine = true,
        isError = problem == SaveProblemUiModel.InvalidDose,
        supportingText = {
            Text(
                if (problem == SaveProblemUiModel.InvalidDose) {
                    problemText(problem)
                } else {
                    stringResource(R.string.edit_counter, form.dose.length, MAXIMUM_DOSE_LENGTH)
                }
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
    Button(
        onClick = onSave,
        enabled = !pending,
        modifier = Modifier
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
