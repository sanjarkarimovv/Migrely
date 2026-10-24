package com.migrely.app.backup.ui.view

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.migrely.app.architecture.ui.view.ScreenEnterObserver
import com.migrely.app.backup.presentation.model.ImportModePresentationModel
import com.migrely.app.backup.presentation.model.YourDataViewState
import com.migrely.app.backup.ui.R
import com.migrely.app.backup.ui.di.YourDataDependencies
import com.migrely.app.backup.ui.model.BackupSummaryUiModel
import com.migrely.app.backup.ui.model.PendingImportUiModel
import com.migrely.app.backup.ui.model.YourDataEventUiModel
import com.migrely.app.backup.ui.model.YourDataMessageUiModel
import com.migrely.app.backup.ui.model.YourDataViewStateUiModel
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.motion.rememberReduceMotion
import com.migrely.app.designsystem.snackbar.MigrelySnackbar
import com.migrely.app.designsystem.snackbar.MigrelySnackbarHost
import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.designsystem.theme.MigrelyTheme
import java.time.LocalDate
import kotlinx.coroutines.delay

private const val MESSAGE_MILLIS = 6_000L
private val CONTENT_MAX_WIDTH = 600.dp

/** Some file apps label a .json file as text or as plain bytes, so those are offered too; the file is checked anyway. */
private val BACKUP_TYPES = arrayOf("application/json", "text/*", "application/octet-stream")

/** Callbacks from the Your data screen. */
data class YourDataActions(
    val onBack: () -> Unit,
    val onExportBackup: () -> Unit,
    val onExportCsv: () -> Unit,
    val onImport: () -> Unit
)

/**
 * "Your data" (BRD BAK-F1–F3, BAK-F5, D71–D73). Files are chosen through the system's file picker, so no storage
 * permission is asked for. A picked backup is checked and shown before anything changes.
 */
@Composable
fun YourDataDependencies.YourData(navController: NavController, modifier: Modifier = Modifier) {
    ScreenEnterObserver { yourDataViewModel.onEnter() }
    ViewModelObserver(navController)
    val viewState by yourDataViewModel.viewState.collectAsState(YourDataViewState.Idle)
    val working = yourDataViewStateUiMapper.toUi(viewState) == YourDataViewStateUiModel.Working
    // Kept across a rotation, so the question is not lost while the user decides.
    var pending by rememberSaveable { mutableStateOf<PendingImportUiModel?>(null) }
    var message by remember { mutableStateOf<YourDataMessageUiModel?>(null) }
    LaunchedEffect(yourDataEventPresenter) {
        yourDataEventPresenter.events.collect { event ->
            when (event) {
                is YourDataEventUiModel.Confirm -> pending = event.pending
                is YourDataEventUiModel.Message -> message = event.message
            }
        }
    }
    LaunchedEffect(message) {
        if (message != null) {
            delay(MESSAGE_MILLIS)
            message = null
        }
    }
    val exportBackup =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/json")
        ) { uri ->
            uri?.let { yourDataViewModel.onExportBackupAction(it.toString()) }
        }
    val exportCsv =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("text/csv")
        ) { uri ->
            uri?.let { yourDataViewModel.onExportCsvAction(it.toString()) }
        }
    val pickBackup =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { yourDataViewModel.onImportFileAction(it.toString()) }
        }
    val launching: (() -> Unit) -> Unit = { launch ->
        try {
            launch()
        } catch (_: ActivityNotFoundException) {
            message = YourDataMessageUiModel.NoFilePicker
        }
    }
    YourDataContents(
        working = working,
        actions = YourDataActions(
            onBack = yourDataViewModel::onBackAction,
            onExportBackup = {
                launching { exportBackup.launch("migrely-${LocalDate.now()}.json") }
            },
            onExportCsv = {
                launching { exportCsv.launch("migrely-attacks-${LocalDate.now()}.csv") }
            },
            onImport = { launching { pickBackup.launch(BACKUP_TYPES) } }
        ),
        messages = {
            MigrelySnackbarHost(message = message, reduceMotion = rememberReduceMotion()) { shown ->
                MigrelySnackbar(
                    text = shown.text(),
                    tone = shown.tone(),
                    action = null,
                    dismissLabel = stringResource(R.string.your_data_message_dismiss),
                    onDismiss = { message = null }
                )
            }
        },
        modifier = modifier
    )
    pending?.let { import ->
        ImportDialog(
            pending = import,
            onImport = { mode ->
                pending = null
                yourDataViewModel.onImportAction(import.uri, mode)
            },
            onDismiss = { pending = null }
        )
    }
}

/** The screen itself, without its view model. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YourDataContents(
    working: Boolean,
    actions: YourDataActions,
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
                        stringResource(R.string.your_data_title),
                        modifier = Modifier.semantics { heading() }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(
                            MigrelyIcons.Back,
                            contentDescription = stringResource(R.string.your_data_back)
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
            if (working) {
                val description = stringResource(R.string.your_data_working)
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = description
                            liveRegion = LiveRegionMode.Polite
                        }
                )
            }
            Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .widthIn(max = CONTENT_MAX_WIDTH)
                        .fillMaxWidth()
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                            )
                        )
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    ActionRow(
                        title = stringResource(R.string.your_data_export_backup),
                        summary = stringResource(R.string.your_data_export_backup_summary),
                        enabled = !working,
                        onClick = actions.onExportBackup
                    )
                    ActionRow(
                        title = stringResource(R.string.your_data_export_csv),
                        summary = stringResource(R.string.your_data_export_csv_summary),
                        enabled = !working,
                        onClick = actions.onExportCsv
                    )
                    ActionRow(
                        title = stringResource(R.string.your_data_import),
                        summary = stringResource(R.string.your_data_import_summary),
                        enabled = !working,
                        onClick = actions.onImport
                    )
                    Text(
                        text = stringResource(R.string.your_data_note),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
        messages()
    }
}

@Composable
private fun ActionRow(title: String, summary: String, enabled: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ImportDialog(
    pending: PendingImportUiModel,
    onImport: (ImportModePresentationModel) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.your_data_confirm_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(pending.summary.text())
                if (pending.phoneHasRecords) Text(stringResource(R.string.your_data_confirm_choice))
            }
        },
        confirmButton = {
            if (pending.phoneHasRecords) {
                Row {
                    TextButton(onClick = { onImport(ImportModePresentationModel.Add) }) {
                        Text(stringResource(R.string.your_data_add))
                    }
                    TextButton(onClick = { onImport(ImportModePresentationModel.Replace) }) {
                        Text(stringResource(R.string.your_data_replace))
                    }
                }
            } else {
                // With nothing on the phone, replacing and adding are the same.
                TextButton(onClick = { onImport(ImportModePresentationModel.Replace) }) {
                    Text(stringResource(R.string.your_data_import_confirm))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.your_data_cancel)) }
        }
    )
}

@Composable
private fun BackupSummaryUiModel.text() = stringResource(
    R.string.your_data_confirm_holds,
    pluralStringResource(R.plurals.your_data_migraines, attacks, attacks),
    pluralStringResource(R.plurals.your_data_medications, medications, medications),
    pluralStringResource(R.plurals.your_data_marked_days, dayMarks, dayMarks)
)

@Composable
private fun YourDataMessageUiModel.text(): String = when (this) {
    YourDataMessageUiModel.BackupSaved -> stringResource(R.string.your_data_backup_saved)
    YourDataMessageUiModel.CsvSaved -> stringResource(R.string.your_data_csv_saved)
    is YourDataMessageUiModel.Replaced ->
        pluralStringResource(R.plurals.your_data_replaced, attacks, attacks)
    is YourDataMessageUiModel.Added -> when {
        addedAttacks == 0 -> stringResource(R.string.your_data_nothing_new)
        skippedAttacks == 0 ->
            pluralStringResource(R.plurals.your_data_added, addedAttacks, addedAttacks)
        else -> pluralStringResource(R.plurals.your_data_added, addedAttacks, addedAttacks) + " " +
            pluralStringResource(R.plurals.your_data_already_here, skippedAttacks, skippedAttacks)
    }
    YourDataMessageUiModel.NotABackup -> stringResource(R.string.your_data_not_a_backup)
    YourDataMessageUiModel.Newer -> stringResource(R.string.your_data_newer)
    YourDataMessageUiModel.StorageFull -> stringResource(R.string.your_data_storage_full)
    YourDataMessageUiModel.SomethingWentWrong -> stringResource(
        R.string.your_data_something_went_wrong
    )
    YourDataMessageUiModel.NoFilePicker -> stringResource(R.string.your_data_no_file_picker)
}

private fun YourDataMessageUiModel.tone() = when (this) {
    YourDataMessageUiModel.BackupSaved,
    YourDataMessageUiModel.CsvSaved,
    is YourDataMessageUiModel.Replaced -> SnackbarTone.Confirmation
    is YourDataMessageUiModel.Added ->
        if (addedAttacks == 0) SnackbarTone.Information else SnackbarTone.Confirmation
    YourDataMessageUiModel.NotABackup,
    YourDataMessageUiModel.Newer,
    YourDataMessageUiModel.StorageFull,
    YourDataMessageUiModel.SomethingWentWrong,
    YourDataMessageUiModel.NoFilePicker -> SnackbarTone.Error
}

@Preview(name = "Your data")
@Composable
private fun YourDataPreview() {
    MigrelyTheme {
        YourDataContents(
            working = false,
            actions = YourDataActions({}, {}, {}, {}),
            messages = {}
        )
    }
}

@Preview(name = "Your data, working")
@Composable
private fun YourDataWorkingPreview() {
    MigrelyTheme {
        YourDataContents(
            working = true,
            actions = YourDataActions({}, {}, {}, {}),
            messages = {}
        )
    }
}
