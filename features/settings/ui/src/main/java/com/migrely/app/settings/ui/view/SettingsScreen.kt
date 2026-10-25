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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
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
import com.migrely.app.settings.ui.model.ThemeUiModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal val CONTENT_MAX_WIDTH = 600.dp
private val ROW_MIN_HEIGHT = 56.dp

private const val MESSAGE_MILLIS = 6_000L

/** Callbacks from the Settings screen. [onTheme] and [onDeleteAllData] open their dialogs. */
data class SettingsActions(
    val onBack: () -> Unit,
    val onReminders: () -> Unit,
    val onPeriodTracking: (Boolean) -> Unit,
    val onYourData: () -> Unit,
    val onTheme: () -> Unit,
    val onWarningSigns: () -> Unit,
    val onAboutMigrely: () -> Unit,
    val onPrivacy: () -> Unit,
    val onLicences: () -> Unit,
    val onDeleteAllData: () -> Unit
)

/** Settings (BRD SET-F1, D93), opened from home's header. */
@Composable
fun SettingsDependencies.Settings(navController: NavController, modifier: Modifier = Modifier) {
    ScreenEnterObserver { settingsViewModel.onEnter() }
    // A screen rebuilt after a rotation reads the settings again, and sees a deletion going on or done.
    LifecycleResumeEffect(settingsViewModel) {
        settingsViewModel.onResume()
        onPauseOrDispose { }
    }
    ViewModelObserver(navController)
    val viewState by settingsViewModel.viewState.collectAsState(SettingsViewState.Loading)
    val settings = settingsViewStateUiMapper.toUi(viewState)
    var notSaved by remember { mutableStateOf(false) }
    // The dialogs, and what the user typed, are kept across a rotation.
    var choosingTheme by rememberSaveable { mutableStateOf(false) }
    var confirmingDelete by rememberSaveable { mutableStateOf(false) }
    var typed by rememberSaveable { mutableStateOf("") }
    var deleteFailed by rememberSaveable { mutableStateOf(false) }
    // Set on confirming and cleared only by a failure: the progress stays until the app starts again (BRD D96).
    var deleteStarted by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(settingsEventPresenter) {
        launch { settingsEventPresenter.notSaved.collect { notSaved = true } }
        settingsEventPresenter.notDeleted.collect {
            deleteStarted = false
            deleteFailed = true
            settingsViewModel.onDeleteFailureShownAction()
        }
    }
    LaunchedEffect(notSaved) {
        if (notSaved) {
            delay(MESSAGE_MILLIS)
            notSaved = false
        }
    }
    SettingsContents(
        settings = settings,
        actions = SettingsActions(
            onBack = settingsViewModel::onBackAction,
            onReminders = settingsViewModel::onRemindersAction,
            onPeriodTracking = settingsViewModel::onPeriodTrackingAction,
            onYourData = settingsViewModel::onYourDataAction,
            onTheme = { choosingTheme = true },
            onWarningSigns = settingsViewModel::onWarningSignsAction,
            onAboutMigrely = settingsViewModel::onAboutMigrelyAction,
            onPrivacy = settingsViewModel::onPrivacyAction,
            onLicences = settingsViewModel::onLicencesAction,
            onDeleteAllData = { confirmingDelete = true }
        ),
        messages = {
            MigrelySnackbarHost(
                message = Unit.takeIf { notSaved },
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
    if (choosingTheme && settings != null) {
        ThemeDialog(
            current = settings.theme,
            onChoose = { theme ->
                choosingTheme = false
                settingsViewModel.onThemeAction(themeUiToPresentationMapper.toPresentation(theme))
            },
            onDismiss = { choosingTheme = false }
        )
    }
    if (confirmingDelete) {
        DeleteAllDataDialog(
            typed = typed,
            word = deleteConfirmationResolver.word,
            confirmed = deleteConfirmationResolver.resolve(typed),
            deleting = deleteProgressUiResolver.resolve(deleteStarted, settings),
            failed = deleteFailed,
            onTyped = {
                typed = it
                deleteFailed = false
            },
            onDelete = {
                deleteFailed = false
                deleteStarted = true
                settingsViewModel.onDeleteAllDataAction(typed)
            },
            onDismiss = {
                confirmingDelete = false
                typed = ""
                deleteFailed = false
            }
        )
    }
}

/** The screen itself, without its view model. Each item is one labelled node (BRD D93, EC-UX-04). */
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
                        .padding(bottom = 16.dp)
                ) {
                    SettingsItems(settings, actions)
                }
            }
        }
        messages()
    }
}

@Composable
private fun SettingsItems(settings: SettingsUiModel?, actions: SettingsActions) {
    SectionHeader(stringResource(R.string.settings_section_diary))
    LinkRow(
        title = stringResource(R.string.settings_reminders),
        summary = stringResource(R.string.settings_reminders_summary),
        onClick = actions.onReminders
    )
    if (settings != null) {
        SwitchRow(
            title = stringResource(R.string.settings_track_periods),
            summary = stringResource(R.string.settings_track_periods_summary),
            checked = settings.periodTracking,
            onCheckedChange = actions.onPeriodTracking
        )
    }
    LinkRow(
        title = stringResource(R.string.settings_your_data),
        summary = stringResource(R.string.settings_your_data_summary),
        onClick = actions.onYourData
    )
    SectionHeader(stringResource(R.string.settings_section_look))
    if (settings != null) {
        LinkRow(
            title = stringResource(R.string.settings_theme),
            summary = stringResource(settings.theme.label()),
            onClick = actions.onTheme,
            opensScreen = false
        )
    }
    SectionHeader(stringResource(R.string.settings_section_help))
    LinkRow(
        title = stringResource(R.string.settings_warning_signs),
        summary = stringResource(R.string.settings_warning_signs_summary),
        onClick = actions.onWarningSigns
    )
    LinkRow(
        title = stringResource(R.string.settings_about),
        summary = stringResource(R.string.settings_about_summary),
        onClick = actions.onAboutMigrely
    )
    SectionHeader(stringResource(R.string.settings_section_privacy))
    LinkRow(
        title = stringResource(R.string.settings_privacy),
        summary = stringResource(R.string.settings_privacy_summary),
        onClick = actions.onPrivacy
    )
    LinkRow(
        title = stringResource(R.string.settings_licences),
        summary = stringResource(R.string.settings_licences_summary),
        onClick = actions.onLicences
    )
    if (settings != null && settings.version.isNotEmpty()) {
        ValueRow(title = stringResource(R.string.settings_version), value = settings.version)
    }
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
    // Apart from the rest, and in the error colour: it cannot be undone.
    LinkRow(
        title = stringResource(R.string.settings_delete_all),
        summary = stringResource(R.string.settings_delete_all_summary),
        onClick = actions.onDeleteAllData,
        opensScreen = false,
        titleColor = MaterialTheme.colorScheme.error
    )
}

/** The label of a theme choice, as the row and the dialog show it. */
internal fun ThemeUiModel.label() = when (this) {
    ThemeUiModel.Dark -> R.string.settings_theme_dark
    ThemeUiModel.Light -> R.string.settings_theme_light
    ThemeUiModel.System -> R.string.settings_theme_system
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 4.dp)
            .semantics { heading() }
    )
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
            .heightIn(min = ROW_MIN_HEIGHT)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        RowTexts(title, summary, MaterialTheme.colorScheme.onSurface, Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = null,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

/** A row that opens a screen, with a chevron, or a dialog, without one. */
@Composable
private fun LinkRow(
    title: String,
    summary: String,
    onClick: () -> Unit,
    opensScreen: Boolean = true,
    titleColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ROW_MIN_HEIGHT)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        RowTexts(title, summary, titleColor, Modifier.weight(1f))
        if (opensScreen) {
            Icon(
                MigrelyIcons.ChevronEnd,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** A fact that cannot be changed, read as one node. */
@Composable
private fun ValueRow(title: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ROW_MIN_HEIGHT)
            .semantics(mergeDescendants = true) { }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        RowTexts(title, value, MaterialTheme.colorScheme.onSurface, Modifier.weight(1f))
    }
}

@Composable
private fun RowTexts(title: String, summary: String, titleColor: Color, modifier: Modifier) {
    Column(modifier) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = titleColor)
        Text(
            text = summary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private val PREVIEW_ACTIONS = SettingsActions({}, {}, {}, {}, {}, {}, {}, {}, {}, {})
private val PREVIEW_SETTINGS = SettingsUiModel(
    periodTracking = true,
    theme = ThemeUiModel.Dark,
    version = "0.1.0",
    deleting = false
)

@Preview(name = "Settings")
@Composable
private fun SettingsPreview() {
    MigrelyTheme {
        SettingsContents(settings = PREVIEW_SETTINGS, actions = PREVIEW_ACTIONS, messages = {})
    }
}

@Preview(name = "Settings, light")
@Composable
private fun SettingsLightPreview() {
    MigrelyTheme(dark = false) {
        SettingsContents(
            settings = PREVIEW_SETTINGS.copy(theme = ThemeUiModel.Light),
            actions = PREVIEW_ACTIONS,
            messages = {}
        )
    }
}

@Preview(name = "Settings, loading")
@Composable
private fun SettingsLoadingPreview() {
    MigrelyTheme {
        SettingsContents(settings = null, actions = PREVIEW_ACTIONS, messages = {})
    }
}
