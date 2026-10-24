package com.migrely.app.settings.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.settings.ui.R
import com.migrely.app.settings.ui.model.ThemeUiModel

/** Dark, Light or Match the phone (BRD D94); a choice applies at once and closes the dialog. */
@Composable
internal fun ThemeDialog(
    current: ThemeUiModel,
    onChoose: (ThemeUiModel) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_theme)) },
        text = {
            Column {
                Column(Modifier.selectableGroup()) {
                    ThemeUiModel.entries.forEach { theme ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .selectable(
                                    selected = theme == current,
                                    role = Role.RadioButton,
                                    onClick = { onChoose(theme) }
                                )
                        ) {
                            RadioButton(selected = theme == current, onClick = null)
                            Text(
                                text = stringResource(theme.label()),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
                Text(
                    text = stringResource(R.string.settings_theme_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_cancel)) }
        }
    )
}

/**
 * "Delete all data" (BRD EC-STO-03, D96). The button turns on only once [typed] is [word] exactly, which [confirmed]
 * says. While [deleting], the dialog shows its progress and cannot be closed; [failed] says the deletion stopped
 * partway, and the user can try again.
 */
@Suppress("LongParameterList")
@Composable
internal fun DeleteAllDataDialog(
    typed: String,
    word: String,
    confirmed: Boolean,
    deleting: Boolean,
    failed: Boolean,
    onTyped: (String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!deleting) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !deleting,
            dismissOnClickOutside = !deleting
        ),
        title = { Text(stringResource(R.string.settings_delete_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(stringResource(R.string.settings_delete_what))
                Text(stringResource(R.string.settings_android_backup))
                Text(stringResource(R.string.settings_delete_type, word))
                OutlinedTextField(
                    value = typed,
                    onValueChange = onTyped,
                    enabled = !deleting,
                    singleLine = true,
                    label = { Text(stringResource(R.string.settings_delete_field, word)) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (deleting) {
                    val description = stringResource(R.string.settings_deleting)
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = description
                                liveRegion = LiveRegionMode.Polite
                            }
                    )
                }
                if (failed) {
                    Text(
                        text = stringResource(R.string.settings_not_deleted),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDelete,
                enabled = confirmed && !deleting,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.settings_delete_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !deleting) {
                Text(stringResource(R.string.settings_cancel))
            }
        }
    )
}

@Preview(name = "Theme")
@Composable
private fun ThemeDialogPreview() {
    MigrelyTheme {
        ThemeDialog(current = ThemeUiModel.System, onChoose = {}, onDismiss = {})
    }
}

@Preview(name = "Delete all data, typed")
@Composable
private fun DeleteAllDataDialogPreview() {
    MigrelyTheme {
        DeleteAllDataDialog(
            typed = "DELETE",
            word = "DELETE",
            confirmed = true,
            deleting = false,
            failed = false,
            onTyped = {},
            onDelete = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "Delete all data, deleting")
@Composable
private fun DeleteAllDataDeletingPreview() {
    MigrelyTheme(dark = false) {
        DeleteAllDataDialog(
            typed = "DELETE",
            word = "DELETE",
            confirmed = true,
            deleting = true,
            failed = false,
            onTyped = {},
            onDelete = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "Delete all data, failed")
@Composable
private fun DeleteAllDataFailedPreview() {
    MigrelyTheme {
        DeleteAllDataDialog(
            typed = "DELETE",
            word = "DELETE",
            confirmed = true,
            deleting = false,
            failed = true,
            onTyped = {},
            onDelete = {},
            onDismiss = {}
        )
    }
}
