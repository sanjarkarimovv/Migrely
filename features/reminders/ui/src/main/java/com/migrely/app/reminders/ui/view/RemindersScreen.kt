package com.migrely.app.reminders.ui.view

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
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
import com.migrely.app.reminders.presentation.model.RemindersViewState
import com.migrely.app.reminders.ui.R
import com.migrely.app.reminders.ui.di.RemindersDependencies
import com.migrely.app.reminders.ui.model.ReminderKindUiModel
import com.migrely.app.reminders.ui.model.RemindersEventUiModel
import com.migrely.app.reminders.ui.model.RemindersMessageUiModel
import com.migrely.app.reminders.ui.model.RemindersUiModel
import com.migrely.app.reminders.ui.model.RemindersViewStateUiModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.delay

private const val MINUTES_PER_HOUR = 60
private const val DEFAULT_MINUTE_OF_DAY = 20 * MINUTES_PER_HOUR
private const val MESSAGE_MILLIS = 6_000L
private val CONTENT_MAX_WIDTH = 600.dp

/** Callbacks from the Reminders screen to its view model. */
data class RemindersActions(
    val onBack: () -> Unit,
    val onDaily: (on: Boolean) -> Unit,
    val onDailyTime: (minuteOfDay: Int) -> Unit,
    val onEffect: (on: Boolean) -> Unit,
    val onOpenSettings: () -> Unit
)

/**
 * The Reminders screen (BRD D65–D68). Notification permission is asked for only when a switch is turned on
 * (EC-REM-01); Android 12 and older have no such request, so a refusal there means notifications are off in settings.
 */
@Composable
fun RemindersDependencies.Reminders(navController: NavController, modifier: Modifier = Modifier) {
    ScreenEnterObserver { remindersViewModel.onEnter() }
    LifecycleResumeEffect(remindersViewModel) {
        remindersViewModel.onResume()
        onPauseOrDispose { }
    }
    ViewModelObserver(navController)
    val viewState by remindersViewModel.viewState.collectAsState(RemindersViewState.Loading)
    val uiState by rememberSaveable(viewState) {
        mutableStateOf(remindersViewStateUiMapper.toUi(viewState))
    }
    val reminders = (uiState as? RemindersViewStateUiModel.Ready)?.reminders
    val minuteOfDay = reminders?.dailyMinuteOfDay ?: DEFAULT_MINUTE_OF_DAY
    // Which switch asked, kept across the system dialog and a rotation.
    var asking by rememberSaveable { mutableStateOf<ReminderKindUiModel?>(null) }
    val answer: (Boolean, ReminderKindUiModel) -> Unit = { granted, kind ->
        remindersViewModel.onPermissionResultAction(
            granted,
            reminderKindUiToPresentationMapper.toPresentation(kind),
            minuteOfDay.toLocalTime()
        )
    }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            asking?.let { answer(granted, it) }
            asking = null
        }
    var message by remember { mutableStateOf<RemindersMessageUiModel?>(null) }
    LaunchedEffect(remindersEventPresenter) {
        remindersEventPresenter.events.collect { event ->
            when (event) {
                is RemindersEventUiModel.AskForPermission ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        asking = event.kind
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        answer(false, event.kind)
                    }
                is RemindersEventUiModel.Message -> message = event.message
            }
        }
    }
    LaunchedEffect(message) {
        if (message != null) {
            delay(MESSAGE_MILLIS)
            message = null
        }
    }
    val context = LocalContext.current
    RemindersContents(
        viewState = uiState,
        actions = RemindersActions(
            onBack = remindersViewModel::onBackAction,
            onDaily = { on ->
                remindersViewModel.onDailyReminderAction(on, minuteOfDay.toLocalTime())
            },
            onDailyTime = { minute ->
                remindersViewModel.onDailyTimeAction(
                    reminders?.dailyEnabled == true,
                    minute.toLocalTime()
                )
            },
            onEffect = remindersViewModel::onEffectReminderAction,
            onOpenSettings = {
                try {
                    context.startActivity(
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    )
                } catch (_: ActivityNotFoundException) {
                    // Some builds lack the notification screen; the app's own settings page always exists.
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData(Uri.fromParts("package", context.packageName, null))
                    )
                }
            }
        ),
        messages = {
            MigrelySnackbarHost(message = message, reduceMotion = rememberReduceMotion()) { shown ->
                MigrelySnackbar(
                    text = stringResource(shown.text()),
                    tone = SnackbarTone.Error,
                    action = null,
                    dismissLabel = stringResource(R.string.reminders_message_dismiss),
                    onDismiss = { message = null }
                )
            }
        },
        modifier = modifier
    )
}

/** The screen itself, without its view model. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersContents(
    viewState: RemindersViewStateUiModel,
    actions: RemindersActions,
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
                        stringResource(R.string.reminders_title),
                        modifier = Modifier.semantics { heading() }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(
                            MigrelyIcons.Back,
                            contentDescription = stringResource(R.string.reminders_back)
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
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                            )
                        )
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    when (viewState) {
                        RemindersViewStateUiModel.Loading -> Unit
                        is RemindersViewStateUiModel.Ready -> ReminderRows(
                            viewState.reminders,
                            actions
                        )
                        RemindersViewStateUiModel.Unavailable -> Text(
                            text = stringResource(R.string.reminders_unavailable),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        messages()
    }
}

@Composable
private fun ReminderRows(reminders: RemindersUiModel, actions: RemindersActions) {
    var picking by rememberSaveable { mutableStateOf(false) }
    val allowed = reminders.notificationsAllowed
    if (!allowed) NotificationsOffNotice(onOpenSettings = actions.onOpenSettings)
    // A switch shows on only while its reminder can arrive.
    SwitchRow(
        title = stringResource(R.string.reminders_daily_title),
        summary = stringResource(R.string.reminders_daily_summary),
        checked = reminders.dailyEnabled && allowed,
        onCheckedChange = actions.onDaily
    )
    // Shown while off too, so the time can be chosen before the reminder is turned on.
    run {
        // The phone's own 12- or 24-hour choice, as the picker uses.
        val pattern = if (android.text.format.DateFormat.is24HourFormat(LocalContext.current)) {
            DateTimeFormatter.ofPattern("HH:mm")
        } else {
            DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        }
        val time = reminders.dailyMinuteOfDay.toLocalTime().format(pattern)
        val description = stringResource(R.string.reminders_daily_time_description, time)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                // Merged into the clickable, so the row keeps its click action for TalkBack and Switch Access
                // (EC-UX-19, QA #14); the texts inside are cleared so the row is read once.
                .semantics { contentDescription = description }
                .clickable(role = Role.Button) { picking = true }
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.reminders_daily_time),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .clearAndSetSemantics { }
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clearAndSetSemantics { }
            )
        }
    }
    SwitchRow(
        title = stringResource(R.string.reminders_effect_title),
        summary = stringResource(R.string.reminders_effect_summary),
        checked = reminders.effectEnabled && allowed,
        onCheckedChange = actions.onEffect
    )
    if (picking) {
        TimeDialog(
            minuteOfDay = reminders.dailyMinuteOfDay,
            onPick = { minute ->
                picking = false
                actions.onDailyTime(minute)
            },
            onDismiss = { picking = false }
        )
    }
}

/** One node for screen readers: the title, then the summary, with the switch's state (BRD EC-UX-19). */
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                summary,
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
private fun NotificationsOffNotice(onOpenSettings: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Icon(
                    MigrelyIcons.Information,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(
                    text = stringResource(R.string.reminders_notifications_off),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            TextButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .align(Alignment.End)
                    .heightIn(min = 48.dp)
            ) {
                Text(stringResource(R.string.reminders_open_settings))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeDialog(minuteOfDay: Int, onPick: (Int) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val state = rememberTimePickerState(
        initialHour = minuteOfDay / MINUTES_PER_HOUR,
        initialMinute = minuteOfDay % MINUTES_PER_HOUR,
        is24Hour = android.text.format.DateFormat.is24HourFormat(context)
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.reminders_picker_title)) },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onPick(state.hour * MINUTES_PER_HOUR + state.minute) }) {
                Text(stringResource(R.string.reminders_picker_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.reminders_picker_cancel))
            }
        }
    )
}

private fun Int.toLocalTime(): LocalTime = LocalTime.of(
    this / MINUTES_PER_HOUR,
    this % MINUTES_PER_HOUR
)

private fun RemindersMessageUiModel.text() = when (this) {
    RemindersMessageUiModel.StorageFull -> R.string.reminders_message_storage_full
    RemindersMessageUiModel.SomethingWentWrong -> R.string.reminders_message_something_went_wrong
}

private val previewActions = RemindersActions({}, {}, {}, {}, {})

@Preview(name = "Reminders")
@Composable
private fun RemindersPreview() {
    MigrelyTheme {
        RemindersContents(
            viewState = RemindersViewStateUiModel.Ready(
                RemindersUiModel(true, DEFAULT_MINUTE_OF_DAY, true, true, true)
            ),
            actions = previewActions,
            messages = {}
        )
    }
}

@Preview(name = "Reminders, notifications off")
@Composable
private fun RemindersOffPreview() {
    MigrelyTheme {
        RemindersContents(
            viewState = RemindersViewStateUiModel.Ready(
                RemindersUiModel(false, DEFAULT_MINUTE_OF_DAY, true, false, false)
            ),
            actions = previewActions,
            messages = {}
        )
    }
}
