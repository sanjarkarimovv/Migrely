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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import com.migrely.app.architecture.ui.view.ScreenEnterObserver
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.motion.rememberReduceMotion
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.medication.presentation.model.MedicationsViewState
import com.migrely.app.medication.ui.R
import com.migrely.app.medication.ui.di.MedicationsDependencies
import com.migrely.app.medication.ui.model.MedicationCategoryUiModel
import com.migrely.app.medication.ui.model.MedicationKindUiModel
import com.migrely.app.medication.ui.model.MedicationUiModel
import com.migrely.app.medication.ui.model.MedicationsViewStateUiModel

private val CONTENT_MAX_WIDTH = 600.dp

/** My medications (BRD MED-F1, EC-MED-01): open one to edit it, remove it with undo, or add one. */
@Composable
fun MedicationsDependencies.Medications(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    ScreenEnterObserver { medicationsViewModel.onEnter() }
    LifecycleResumeEffect(medicationsViewModel) {
        medicationsViewModel.onResume()
        onPauseOrDispose { }
    }
    ViewModelObserver(navController)
    val viewState by medicationsViewModel.viewState.collectAsState(MedicationsViewState.Loading)
    val uiState by rememberSaveable(viewState) {
        mutableStateOf(medicationsViewStateUiMapper.toUi(viewState))
    }
    val reduceMotion = rememberReduceMotion()
    MedicationsContents(
        viewState = uiState,
        actions = MedicationsActions(
            onBack = medicationsViewModel::onLeaveAction,
            onAdd = medicationsViewModel::onAddAction,
            onMedication = medicationsViewModel::onMedicationAction,
            onRemove = medicationsViewModel::onArchiveAction
        ),
        messages = {
            MedicationMessages(
                presenter = medicationEventPresenter,
                behaviourMapper = medicationMessageBehaviourUiMapper,
                reduceMotion = reduceMotion,
                onUndoArchive = medicationsViewModel::onUndoArchiveAction
            )
        },
        modifier = modifier
    )
}

data class MedicationsActions(
    val onBack: () -> Unit,
    val onAdd: () -> Unit,
    val onMedication: (medicationId: String) -> Unit,
    val onRemove: (medicationId: String) -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationsContents(
    viewState: MedicationsViewStateUiModel,
    actions: MedicationsActions,
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
                        stringResource(R.string.list_title),
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
            val medications = (viewState as? MedicationsViewStateUiModel.Ready)?.medications
            if (medications != null) {
                Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
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
                        if (medications.isEmpty()) {
                            Text(
                                text = stringResource(R.string.list_empty),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        medications.forEach { medication ->
                            MedicationRow(
                                medication = medication,
                                onOpen = { actions.onMedication(medication.id) },
                                onRemove = { actions.onRemove(medication.id) }
                            )
                        }
                        Button(
                            onClick = actions.onAdd,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .heightIn(min = 56.dp)
                        ) {
                            Text(
                                stringResource(R.string.list_add),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
        messages()
    }
}

@Composable
private fun MedicationRow(medication: MedicationUiModel, onOpen: () -> Unit, onRemove: () -> Unit) {
    val summary = "${medication.spokenTitle()}, ${medication.typeLabel()}"
    val description = stringResource(R.string.list_item_description, summary)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            onClick = onOpen,
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp)
                .semantics {
                    contentDescription = description
                    role = Role.Button
                }
        ) {
            Column(
                Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clearAndSetSemantics { }
            ) {
                Text(medication.title(), style = MaterialTheme.typography.bodyLarge)
                Text(
                    medication.typeLabel(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        IconButton(onClick = onRemove) {
            Icon(
                MigrelyIcons.Close,
                contentDescription = stringResource(R.string.list_remove, medication.name)
            )
        }
    }
}

@Preview(name = "My medications")
@Composable
private fun MedicationsPreview() {
    MigrelyTheme {
        MedicationsContents(
            viewState = MedicationsViewStateUiModel.Ready(
                listOf(
                    MedicationUiModel(
                        "i",
                        "Ibuprofen",
                        MedicationKindUiModel.Acute,
                        MedicationCategoryUiModel.SimplePainkiller,
                        "400 mg"
                    ),
                    MedicationUiModel(
                        "t",
                        "Topiramate",
                        MedicationKindUiModel.Preventive,
                        null,
                        null
                    )
                )
            ),
            actions = MedicationsActions({}, {}, {}, {}),
            messages = {}
        )
    }
}
