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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.settings.ui.R
import com.migrely.app.settings.ui.di.SettingsDependencies

private val CONTENT_MAX_WIDTH = 600.dp

/** Callbacks from the Settings screen. */
data class SettingsActions(
    val onBack: () -> Unit,
    val onReminders: () -> Unit,
    val onYourData: () -> Unit
)

/** Settings (BRD D70), opened from home's header. Later changes add subscription, privacy and safety here. */
@Composable
fun SettingsDependencies.Settings(navController: NavController, modifier: Modifier = Modifier) {
    ScreenEnterObserver { settingsViewModel.onEnter() }
    ViewModelObserver(navController)
    SettingsContents(
        actions = SettingsActions(
            onBack = settingsViewModel::onBackAction,
            onReminders = settingsViewModel::onRemindersAction,
            onYourData = settingsViewModel::onYourDataAction
        ),
        modifier = modifier
    )
}

/** The screen itself, without its view model. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContents(actions: SettingsActions, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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
            }
        }
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
        SettingsContents(actions = SettingsActions({}, {}, {}))
    }
}
