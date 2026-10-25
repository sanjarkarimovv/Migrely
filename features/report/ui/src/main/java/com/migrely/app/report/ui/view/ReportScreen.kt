package com.migrely.app.report.ui.view

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.migrely.app.architecture.ui.view.ScreenEnterObserver
import com.migrely.app.designsystem.icon.MigrelyIcons
import com.migrely.app.designsystem.motion.rememberReduceMotion
import com.migrely.app.designsystem.snackbar.MigrelySnackbar
import com.migrely.app.designsystem.snackbar.MigrelySnackbarHost
import com.migrely.app.designsystem.snackbar.messageTimeoutMillis
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.report.presentation.model.ReportViewState
import com.migrely.app.report.ui.R
import com.migrely.app.report.ui.di.ReportDependencies
import com.migrely.app.report.ui.model.ReportEventUiModel
import com.migrely.app.report.ui.model.ReportMessageUiModel
import com.migrely.app.report.ui.model.ReportOptionsUiModel
import com.migrely.app.report.ui.model.ReportPeriodUiModel
import com.migrely.app.report.ui.model.ReportViewStateUiModel
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onSubscription

private val CONTENT_MAX_WIDTH = 600.dp
private val MONTH_CHOICES = listOf(1, 3, 6, 12)
private val DEFAULT_PERIOD = ReportPeriodUiModel.LastMonths(3)

/** Callbacks from the report screen. */
data class ReportActions(
    val onBack: () -> Unit,
    val onPeriod: (ReportPeriodUiModel) -> Unit,
    val onCustom: () -> Unit,
    val onNotes: (Boolean) -> Unit,
    val onCreate: () -> Unit
)

/** A message as shown, with its place in the order shown, so the same one twice shows again. */
private data class ShownMessage(val message: ReportMessageUiModel, val number: Int)

/**
 * The doctor report (BRD REP-F1–F4, EC-REP-01–03, D81–D85). The chosen period and the notes switch are kept here, and
 * the finished PDF goes out through the system's share sheet with a one-off read grant, so no permission is needed.
 */
@Composable
fun ReportDependencies.Report(navController: NavController, modifier: Modifier = Modifier) {
    var period by rememberSaveable { mutableStateOf<ReportPeriodUiModel>(DEFAULT_PERIOD) }
    var includeNotes by rememberSaveable { mutableStateOf(false) }
    var pickingRange by rememberSaveable { mutableStateOf(false) }
    val toPresentation = reportPeriodPresentationMapper
    ScreenEnterObserver { reportViewModel.onEnter(toPresentation.toPresentation(period)) }
    ViewModelObserver(navController)
    val viewState by reportViewModel.viewState.collectAsState(ReportViewState.Loading)
    val context = LocalContext.current
    var message by remember { mutableStateOf<ShownMessage?>(null) }
    var shownCount by remember { mutableIntStateOf(0) }
    val show: (ReportMessageUiModel) -> Unit = {
        shownCount++
        message = ShownMessage(it, shownCount)
    }
    val shareTitle = stringResource(R.string.report_share)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(reportEventPresenter, lifecycle) {
        // Only while the screen shows, so the share sheet never opens over another app. An outcome that comes while
        // the app is away is asked for again when it comes back, and after a rotation (review I3).
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            reportEventPresenter.events
                .onSubscription { reportViewModel.onResume(toPresentation.toPresentation(period)) }
                .collect { event ->
                    reportViewModel.onOutcomeShownAction()
                    when (event) {
                        is ReportEventUiModel.Share ->
                            if (!share(
                                    context,
                                    event.uri,
                                    shareTitle
                                )
                            ) {
                                show(ReportMessageUiModel.NoAppToShare)
                            }
                        is ReportEventUiModel.Message -> show(event.message)
                    }
                }
        }
    }
    // Stretched to the accessibility timeout (BRD EC-UX-16; QA #56).
    val messageTimeout = messageTimeoutMillis(
        message?.let { reportMessageBehaviourUiMapper.toUi(it.message).baseDurationMillis }
    )
    LaunchedEffect(message, messageTimeout) {
        if (message != null && messageTimeout != null) {
            delay(messageTimeout)
            message = null
        }
    }
    ReportContents(
        viewState = reportViewStateUiMapper.toUi(viewState),
        period = period,
        includeNotes = includeNotes,
        actions = ReportActions(
            onBack = reportViewModel::onBackAction,
            onPeriod = {
                period = it
                reportViewModel.onPeriodAction(toPresentation.toPresentation(it))
            },
            onCustom = { pickingRange = true },
            onNotes = { includeNotes = it },
            onCreate = {
                reportViewModel.onCreateAction(toPresentation.toPresentation(period), includeNotes)
            }
        ),
        messages = {
            MigrelySnackbarHost(message = message, reduceMotion = rememberReduceMotion()) { shown ->
                MigrelySnackbar(
                    text = stringResource(shown.message.text()),
                    tone = reportMessageBehaviourUiMapper.toUi(shown.message).tone,
                    action = null,
                    dismissLabel = stringResource(R.string.report_message_dismiss),
                    onDismiss = { message = null }
                )
            }
        },
        modifier = modifier
    )
    if (pickingRange) {
        RangeDialog(
            current = period as? ReportPeriodUiModel.Range,
            onPick = { from, to ->
                pickingRange = false
                period = ReportPeriodUiModel.Range(from, to)
                reportViewModel.onPeriodAction(toPresentation.toPresentation(period))
            },
            onDismiss = { pickingRange = false }
        )
    }
}

/** The screen itself, without its view model. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReportContents(
    viewState: ReportViewStateUiModel,
    period: ReportPeriodUiModel,
    includeNotes: Boolean,
    actions: ReportActions,
    messages: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.report_title),
                        modifier = Modifier.semantics {
                            heading()
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(
                            MigrelyIcons.Back,
                            contentDescription = stringResource(R.string.report_back)
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
            val working = viewState == ReportViewStateUiModel.Working
            if (working) {
                val description = stringResource(R.string.report_working)
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().semantics {
                        contentDescription = description
                        liveRegion = LiveRegionMode.Polite
                    }
                )
            }
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
                    Text(
                        stringResource(R.string.report_period),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.semantics { heading() }
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MONTH_CHOICES.forEach { months ->
                            FilterChip(
                                selected = period == ReportPeriodUiModel.LastMonths(months),
                                onClick = {
                                    actions.onPeriod(ReportPeriodUiModel.LastMonths(months))
                                },
                                enabled = !working,
                                label = {
                                    Text(
                                        pluralStringResource(
                                            R.plurals.report_months,
                                            months,
                                            months
                                        )
                                    )
                                },
                                modifier = Modifier.heightIn(min = 48.dp)
                            )
                        }
                        FilterChip(
                            selected = period is ReportPeriodUiModel.Range,
                            onClick = actions.onCustom,
                            enabled = !working,
                            label = { Text(stringResource(R.string.report_custom)) },
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                    // The dates stay while a report is made, so the screen does not jump (walk B1).
                    var shownOptions by rememberSaveable {
                        mutableStateOf<ReportOptionsUiModel?>(null)
                    }
                    val options = (viewState as? ReportViewStateUiModel.Ready)?.options
                    if (options != null) shownOptions = options
                    shownOptions?.let { Covers(it) }
                    if (viewState == ReportViewStateUiModel.Unavailable) {
                        Text(
                            stringResource(R.string.report_unavailable),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    NotesRow(includeNotes, enabled = !working, onChange = actions.onNotes)
                    Button(
                        onClick = actions.onCreate,
                        enabled = !working && options?.anythingLogged == true,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    ) {
                        Text(stringResource(R.string.report_create))
                    }
                    if (options != null && !options.anythingLogged) {
                        Text(
                            stringResource(R.string.report_nothing_logged),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                        )
                    }
                    Text(
                        stringResource(R.string.report_about),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        messages()
    }
}

@Composable
private fun Covers(options: ReportOptionsUiModel) {
    val format = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    Text(
        stringResource(
            R.string.report_covers,
            LocalDate.ofEpochDay(options.fromEpochDay).format(format),
            LocalDate.ofEpochDay(options.toEpochDay).format(format)
        ),
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
    )
}

/** One node with Role switch, like the app's other switches (EC-UX-04). */
@Composable
private fun NotesRow(checked: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onChange
            )
    ) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.report_notes), style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(R.string.report_notes_summary),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

/** A date range up to today; the picker works in UTC midnights. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RangeDialog(
    current: ReportPeriodUiModel.Range?,
    onPick: (fromEpochDay: Long, toEpochDay: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val today = LocalDate.now()
    val state = rememberDateRangePickerState(
        // The range chosen before, so it can be changed rather than chosen again (walk B5).
        initialSelectedStartDateMillis = current?.fromEpochDay?.let(::utcMillisOf),
        initialSelectedEndDateMillis = current?.toEpochDay?.let(::utcMillisOf),
        // A report covers at most 24 months (BRD D84), so the picker offers this year and the two before.
        yearRange = (today.year - 2)..today.year,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) =
                utcTimeMillis <= today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

            override fun isSelectableYear(year: Int) = year <= today.year
        }
    )
    val from = state.selectedStartDateMillis
    val to = state.selectedEndDateMillis ?: from
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    if (from != null &&
                        to != null
                    ) {
                        onPick(epochDayOf(from), epochDayOf(to))
                    }
                },
                enabled = from != null
            ) {
                Text(stringResource(R.string.report_range_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.report_range_cancel)) }
        }
    ) {
        DateRangePicker(
            state = state,
            title = {
                Text(
                    stringResource(R.string.report_range_title),
                    modifier = Modifier.padding(16.dp)
                )
            }
        )
    }
}

private const val DAY_MILLIS = 86_400_000L

private fun epochDayOf(utcMillis: Long) = Math.floorDiv(utcMillis, DAY_MILLIS)

private fun utcMillisOf(epochDay: Long) = epochDay * DAY_MILLIS

/** The PDF through the share sheet, readable by the chosen app only. False when no app can take it. */
private fun share(context: Context, uri: String, title: String): Boolean {
    val document = Uri.parse(uri)
    val send = Intent(Intent.ACTION_SEND)
        .setType("application/pdf")
        .putExtra(Intent.EXTRA_STREAM, document)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    send.clipData = ClipData.newRawUri(title, document)
    return try {
        context.startActivity(
            Intent.createChooser(send, title).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        )
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}

private fun ReportMessageUiModel.text() = when (this) {
    ReportMessageUiModel.NothingLogged -> R.string.report_message_nothing
    ReportMessageUiModel.StorageFull -> R.string.report_message_storage
    ReportMessageUiModel.SomethingWentWrong -> R.string.report_message_failed
    ReportMessageUiModel.NoAppToShare -> R.string.report_message_no_app
}

@Preview(name = "Doctor report")
@Composable
private fun ReportPreview() {
    MigrelyTheme {
        ReportContents(
            viewState = ReportViewStateUiModel.Ready(
                ReportOptionsUiModel(20_660, 20_751, anythingLogged = true)
            ),
            period = DEFAULT_PERIOD,
            includeNotes = false,
            actions = ReportActions({}, {}, {}, {}, {}),
            messages = {}
        )
    }
}

@Preview(name = "Doctor report, nothing logged")
@Composable
private fun ReportEmptyPreview() {
    MigrelyTheme {
        ReportContents(
            viewState = ReportViewStateUiModel.Ready(
                ReportOptionsUiModel(20_721, 20_751, anythingLogged = false)
            ),
            period = ReportPeriodUiModel.LastMonths(1),
            includeNotes = false,
            actions = ReportActions({}, {}, {}, {}, {}),
            messages = {}
        )
    }
}
