package com.migrely.app.settings.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.migrely.app.architecture.ui.view.ScreenEnterObserver
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.motion.rememberReduceMotion
import com.migrely.app.designsystem.snackbar.MigrelySnackbar
import com.migrely.app.designsystem.snackbar.MigrelySnackbarHost
import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.settings.presentation.model.SettingsViewState
import com.migrely.app.settings.ui.R
import com.migrely.app.settings.ui.di.SettingsDependencies
import com.migrely.app.settings.ui.model.SettingsUiModel
import kotlinx.coroutines.delay

private val CONTENT_MAX_WIDTH = 600.dp

private const val MESSAGE_MILLIS = 6_000L

/** Callbacks from the Settings screen. */
data class SettingsActions(
    val onBack: () -> Unit,
    val onReminders: () -> Unit,
    val onYourData: () -> Unit,
    val onPeriodTracking: (Boolean) -> Unit
)

/** Settings (BRD D70, D79), opened from home's header. Later changes add subscription, privacy and safety here. */
@Composable
fun SettingsDependencies.Settings(navController: NavController, modifier: Modifier = Modifier) {
    ScreenEnterObserver { settingsViewModel.onEnter() }
    ViewModelObserver(navController)
    val viewState by settingsViewModel.viewState.collectAsState(SettingsViewState.Loading)
    var notSaved by remember { mutableStateOf(false) }
    LaunchedEffect(settingsEventPresenter) {
        settingsEventPresenter.notSaved.collect { notSaved = true }
    }
    LaunchedEffect(notSaved) {
        if (notSaved) {
            delay(MESSAGE_MILLIS)
            notSaved = false
        }
    }
    SettingsContents(
        settings = settingsViewStateUiMapper.toUi(viewState),
        actions = SettingsActions(
            onBack = settingsViewModel::onBackAction,
            onReminders = settingsViewModel::onRemindersAction,
            onYourData = settingsViewModel::onYourDataAction,
            onPeriodTracking = settingsViewModel::onPeriodTrackingAction
        ),
        messages = {
            MigrelySnackbarHost(
                message = Unit.takeIf {
                    notSaved
                },
                reduceMotion = rememberReduceMotion()
            ) {
                MigrelySnackbar(
                    text = stringResource(R.string.settings_not_saved),
                    tone = SnackbarTone.Error,
                    action = null,
                    dismissLabel = stringResource(R.string.settings_message_dismiss),
                    onDismiss = { notSaved = false }
                )
            }
        },
        modifier = modifier
    )
}

/** The screen itself, without its view model. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContents(
    settings: SettingsUiModel?,
    actions: SettingsActions,
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
                        stringResource(R.string.settings_title),
                        modifier = Modifier.semantics { heading() }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(
                            MigrelyIcons.Back,
                            contentDescription = stringResource(R.string.settings_back)
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
                    SettingsRow(
                        title = stringResource(R.string.settings_reminders),
                        summary = stringResource(R.string.settings_reminders_summary),
                        onClick = actions.onReminders
                    )
                    SettingsRow(
                        title = stringResource(R.string.settings_your_data),
                        summary = stringResource(R.string.settings_your_data_summary),
                        onClick = actions.onYourData
                    )
                    if (settings != null) {
                        SwitchRow(
                            title = stringResource(R.string.settings_track_periods),
                            summary = stringResource(R.string.settings_track_periods_summary),
                            checked = settings.periodTracking,
                            onCheckedChange = actions.onPeriodTracking
                        )
                    }
                }
            }
        }
        messages()
    }
}

/** One node with Role switch, like the Reminders screen's switches (EC-UX-04). */
@Composable
private fun SwitchRow(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
private fun SettingsRow(title: String, summary: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            MigrelyIcons.ChevronEnd,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(name = "Settings")
@Composable
private fun SettingsPreview() {
    MigrelyTheme {
        SettingsContents(
            settings = SettingsUiModel(periodTracking = true),
            actions = SettingsActions({}, {}, {}, {}),
            messages = {}
        )
    }
}
