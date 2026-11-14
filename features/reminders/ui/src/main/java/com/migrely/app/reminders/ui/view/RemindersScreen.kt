package com.migrely.app.reminders.ui.view

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.migrely.app.architecture.ui.view.ScreenEnterObserver
import com.migrely.app.designsystem.dialog.MigrelyDialog
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.motion.rememberReduceMotion
import com.migrely.app.designsystem.picker.DatePickerLabels
import com.migrely.app.designsystem.picker.MigrelyDatePicker
import com.migrely.app.designsystem.snackbar.MESSAGE_OFFSET_BELOW_TOP_BAR
import com.migrely.app.designsystem.snackbar.MigrelySnackbar
import com.migrely.app.designsystem.snackbar.MigrelySnackbarHost
import com.migrely.app.designsystem.snackbar.SnackbarAction
import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.designsystem.snackbar.messageTimeoutMillis
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.reminders.presentation.model.RemindersViewState
import com.migrely.app.reminders.ui.R
import com.migrely.app.reminders.ui.di.RemindersDependencies
import com.migrely.app.reminders.ui.model.ReminderKindUiModel
import com.migrely.app.reminders.ui.model.RemindersEventUiModel
import com.migrely.app.reminders.ui.model.RemindersMessageUiModel
import com.migrely.app.reminders.ui.model.RemindersUiModel
import com.migrely.app.reminders.ui.model.RemindersViewStateUiModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.delay

private const val MINUTES_PER_HOUR = 60
private const val DEFAULT_MINUTE_OF_DAY = 20 * MINUTES_PER_HOUR
private val CONTENT_MAX_WIDTH = 600.dp
private const val PICKABLE_DAYS = 365L

/** D161: a second tap within this of Done's lift must not reach the system dialog (design D10). */
private const val VISIT_ASK_WAIT_MILLIS = 500L

/** Callbacks from the Reminders screen to its view model. */
data class RemindersActions(
    val onBack: () -> Unit,
    val onDaily: (on: Boolean) -> Unit,
    val onDailyTime: (minuteOfDay: Int) -> Unit,
    val onEffect: (on: Boolean) -> Unit,
    val onOpenSettings: () -> Unit,
    val onVisitDate: (LocalDate) -> Unit = {},
    val onRemoveVisit: () -> Unit = {},
    val onOpenVisitChannelSettings: () -> Unit = {}
)

/**
 * The Reminders screen (BRD D65–D68). Notification permission is asked for only when a switch is turned on
 * (EC-REM-01) or a doctor visit is set (EC-REM-11); Android 12 and older have no such request, so a refusal there means
 * notifications are off in settings. After a visit is set, the system dialog waits until the picker's window has gone
 * and 500 ms have passed since the lift on Done, so a second tap cannot answer it (design D10).
 */
@Composable
fun RemindersDependencies.Reminders(navController: NavController, modifier: Modifier = Modifier) {
    ScreenEnterObserver { remindersViewModel.onEnter() }
    LifecycleResumeEffect(remindersViewModel) {
        remindersViewModel.onResume()
        onPauseOrDispose { }
    }
    ClockChangeEffect(remindersViewModel::onClockChanged)
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
    // The lift on the picker's Done; lost with a rotation, which starts the wait again (design D10).
    var visitPickedAt by remember { mutableLongStateOf(0L) }
    var visitAskPending by rememberSaveable { mutableStateOf(false) }
    DropVisitRequestOnLeaving { visitAskPending = false }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(visitAskPending) {
        if (!visitAskPending) return@LaunchedEffect
        val start = visitPickedAt.takeIf { it > 0L } ?: SystemClock.uptimeMillis()
        delay((start + VISIT_ASK_WAIT_MILLIS - SystemClock.uptimeMillis()).coerceAtLeast(0L))
        visitAskPending = false
        // Only while Reminders is the screen in front; otherwise nothing is asked and nothing stamped.
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            asking = ReminderKindUiModel.Visit
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    var message by remember { mutableStateOf<RemindersMessageUiModel?>(null) }
    LaunchedEffect(remindersEventPresenter) {
        remindersEventPresenter.events.collect { event ->
            when (event) {
                is RemindersEventUiModel.AskForPermission -> when {
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> answer(
                        false,
                        event.kind
                    )
                    event.kind == ReminderKindUiModel.Visit -> visitAskPending = true
                    else -> {
                        asking = event.kind
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                is RemindersEventUiModel.Message -> {
                    // "Saved." only replaces an error that the write made untrue; any other message stays, and with
                    // none the write needs no word (BRD D178 (1)).
                    val saved = event.message == RemindersMessageUiModel.Saved
                    val errorShown = message?.let {
                        remindersMessageBehaviourUiMapper.toUi(it).tone == SnackbarTone.Error
                    } == true
                    if (!saved || errorShown) message = event.message
                }
            }
        }
    }
    // Stretched to the accessibility timeout (BRD EC-UX-16; QA #56).
    val messageTimeout = messageTimeoutMillis(
        message?.let { remindersMessageBehaviourUiMapper.toUi(it).baseDurationMillis }
    )
    LaunchedEffect(message, messageTimeout) {
        if (message != null && messageTimeout != null) {
            delay(messageTimeout)
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
            onVisitDate = { date ->
                visitPickedAt = SystemClock.uptimeMillis()
                remindersViewModel.onVisitDateAction(date)
            },
            onRemoveVisit = remindersViewModel::onRemoveVisitAction,
            onOpenVisitChannelSettings = {
                openSettings(
                    context,
                    Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        .putExtra(Settings.EXTRA_CHANNEL_ID, visitNotificationChannelId)
                )
            },
            onOpenSettings = {
                openSettings(
                    context,
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                )
            }
        ),
        messages = {
            MigrelySnackbarHost(
                message = message,
                reduceMotion = rememberReduceMotion(),
                topOffset = MESSAGE_OFFSET_BELOW_TOP_BAR
            ) { shown ->
                val behaviour = remindersMessageBehaviourUiMapper.toUi(shown)
                val removed = shown as? RemindersMessageUiModel.VisitRemoved
                MigrelySnackbar(
                    text = stringResource(shown.text()),
                    tone = behaviour.tone,
                    action = removed?.let {
                        SnackbarAction(
                            label = stringResource(R.string.reminders_snackbar_undo),
                            spokenLabel = stringResource(
                                R.string.reminders_snackbar_undo_visit_description
                            ),
                            onClick = {
                                // Acts only while this is still the message on screen, so it never undoes twice.
                                if (message == shown) {
                                    message = null
                                    remindersViewModel.onUndoRemoveVisitAction(
                                        doctorVisitUiToPresentationMapper.toPresentation(it.visit)
                                    )
                                }
                            }
                        )
                    },
                    baseDurationMillis = behaviour.baseDurationMillis,
                    dismissLabel = stringResource(R.string.reminders_message_dismiss),
                    onDismiss = { if (message == shown) message = null },
                    timeLeft = if (removed != null && messageTimeout != null &&
                        !rememberReduceMotion()
                    ) {
                        rememberTimeLeft(shown, messageTimeout)
                    } else {
                        null
                    }
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
    var pickingVisit by rememberSaveable { mutableStateOf(false) }
    val visitRowFocus = remember { FocusRequester() }
    val today = LocalDate.ofEpochDay(reminders.todayEpochDay)
    val nextVisit = reminders.nextVisitEpochDay?.let(LocalDate::ofEpochDay)
    val allowed = reminders.notificationsAllowed
    if (!allowed) {
        NotificationsOffNotice(
            text = stringResource(R.string.reminders_notifications_off),
            openSettingsDescription = null,
            onOpenSettings = actions.onOpenSettings
        )
    }
    // First, since only this screen sets a visit (design D9).
    DoctorVisitSection(
        today = today,
        nextVisit = nextVisit,
        removable = reminders.visitRemovable,
        notificationsAllowed = allowed,
        channelOff = reminders.visitChannelOff,
        rowFocus = visitRowFocus,
        onPick = { pickingVisit = true },
        onRemove = actions.onRemoveVisit,
        onOpenChannelSettings = actions.onOpenVisitChannelSettings
    )
    Spacer(Modifier.height(12.dp))
    // A switch shows on only while its reminder can arrive.
    SwitchRow(
        title = stringResource(R.string.reminders_daily_title),
        summary = stringResource(R.string.reminders_daily_summary),
        checked = reminders.dailyEnabled && allowed,
        onCheckedChange = actions.onDaily
    )
    // Shown while off too, so the time can be chosen before the reminder is turned on.
    run {
        val time = reminders.dailyMinuteOfDay.toLocalTime().format(reminderTimePattern())
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
    if (pickingVisit) {
        // The same "today" the save checks against (SetDoctorVisitUseCase), not a clock read here.
        MigrelyDatePicker(
            initial = nextVisit,
            firstDate = today,
            lastDate = today.plusDays(PICKABLE_DAYS),
            labels = DatePickerLabels(
                title = stringResource(R.string.reminders_visit_picker_title),
                confirm = stringResource(R.string.reminders_visit_picker_done),
                cancel = stringResource(R.string.reminders_visit_picker_cancel),
                none = stringResource(R.string.reminders_visit_picker_none)
            ),
            onConfirm = { date ->
                pickingVisit = false
                actions.onVisitDate(date)
            },
            onDismiss = { pickingVisit = false }
        )
    }
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
internal fun NotificationsOffNotice(
    text: String,
    openSettingsDescription: String?,
    onOpenSettings: () -> Unit
) {
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
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            TextButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .align(Alignment.End)
                    .heightIn(min = 48.dp)
                    .then(
                        if (openSettingsDescription == null) {
                            Modifier
                        } else {
                            Modifier.semantics { contentDescription = openSettingsDescription }
                        }
                    )
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
    // The whole dialog scrolls, as the design system's picker does, so in landscape the title and buttons leave room
    // to drag outside the dial (QA #79).
    MigrelyDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.reminders_picker_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                TimePicker(state = state, layoutType = TimePickerLayoutType.Vertical)
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.reminders_picker_cancel))
                    }
                    TextButton(onClick = { onPick(state.hour * MINUTES_PER_HOUR + state.minute) }) {
                        Text(stringResource(R.string.reminders_picker_ok))
                    }
                }
            }
        }
    }
}

private fun Int.toLocalTime(): LocalTime = LocalTime.of(
    this / MINUTES_PER_HOUR,
    this % MINUTES_PER_HOUR
)

private fun RemindersMessageUiModel.text() = when (this) {
    RemindersMessageUiModel.StorageFull -> R.string.reminders_message_storage_full
    RemindersMessageUiModel.SomethingWentWrong -> R.string.reminders_message_something_went_wrong
    is RemindersMessageUiModel.VisitRemoved -> R.string.reminders_message_visit_removed
    RemindersMessageUiModel.Undone -> R.string.reminders_message_undone
    RemindersMessageUiModel.Saved -> R.string.reminders_message_saved
}

/** Some builds lack a settings page; the app's own details page always exists. */
private fun openSettings(context: Context, intent: Intent) {
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", context.packageName, null))
        )
    }
}

/**
 * A request waiting after the picker is dropped when the screen is left, but kept across a rotation, whose wait starts
 * again (design D10).
 */
@Composable
private fun DropVisitRequestOnLeaving(drop: () -> Unit) {
    val owner = LocalLifecycleOwner.current
    val activity = LocalActivity.current
    val currentDrop by rememberUpdatedState(drop)
    DisposableEffect(owner, activity) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP &&
                activity?.isChangingConfigurations != true
            ) {
                currentDrop()
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
}

// The line under "Visit removed" runs down with its time (BRD EC-UX-16).
@Composable
private fun rememberTimeLeft(shown: RemindersMessageUiModel, timeoutMillis: Long): () -> Float {
    val fraction = remember(shown, timeoutMillis) { Animatable(1f) }
    LaunchedEffect(fraction) {
        fraction.animateTo(0f, tween(timeoutMillis.toInt(), easing = LinearEasing))
    }
    return { fraction.value }
}

private val previewActions = RemindersActions({}, {}, {}, {}, {})
private val PREVIEW_TODAY = LocalDate.of(2026, 11, 10).toEpochDay()

@Preview(name = "Reminders")
@Composable
private fun RemindersPreview() {
    MigrelyTheme {
        RemindersContents(
            viewState = RemindersViewStateUiModel.Ready(
                RemindersUiModel(true, DEFAULT_MINUTE_OF_DAY, true, true, true, PREVIEW_TODAY)
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
                RemindersUiModel(false, DEFAULT_MINUTE_OF_DAY, true, false, false, PREVIEW_TODAY)
            ),
            actions = previewActions,
            messages = {}
        )
    }
}
