package com.migrely.app.settings.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
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
import com.migrely.app.designsystem.layout.MigrelyInsets
import com.migrely.app.designsystem.motion.rememberReduceMotion
import com.migrely.app.designsystem.snackbar.MESSAGE_OFFSET_BELOW_TOP_BAR
import com.migrely.app.designsystem.snackbar.MessageLayout
import com.migrely.app.designsystem.snackbar.MigrelySnackbar
import com.migrely.app.designsystem.snackbar.MigrelySnackbarHost
import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.designsystem.snackbar.messageTimeoutMillis
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.settings.presentation.model.SettingsViewState
import com.migrely.app.settings.ui.R
import com.migrely.app.settings.ui.di.SettingsDependencies
import com.migrely.app.settings.ui.model.SettingsUiModel
import com.migrely.app.settings.ui.model.ThemeUiModel
import kotlinx.coroutines.delay
import android.os.SystemClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.migrely.app.architecture.ui.view.ScreenLeaveObserver
import com.migrely.app.settings.ui.model.ExternalAppResultUiModel
import com.migrely.app.settings.ui.model.SettingsEventUiModel
import com.migrely.app.settings.ui.model.SettingsMessageUiModel
import com.migrely.app.settings.ui.model.ShownSettingsMessageUiModel

internal val CONTENT_MAX_WIDTH = 600.dp
private val ROW_MIN_HEIGHT = 56.dp

/** Settings' messages are errors: 8 s, stretched to the accessibility timeout (BRD EC-UX-16; QA #56). */
private const val READING_MILLIS = 8_000L

/** "Share and rate" fades and folds in this time (ux section 1, design D5). */
private const val SECTION_MOTION_MILLIS = 200

/** Callbacks from the Settings screen. [onTheme] opens its dialog. */
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
    val onDeleteAllData: () -> Unit,
    val onShare: () -> Unit,
    val onRate: () -> Unit
)

/**
 * Settings (BRD SET-F1, SET-F2, D93, D185), opened from home's header. [premiumSection] is drawn at the top; `:app`
 * fills it with the subscription feature's Premium section (change add-subscription, D112). [attackInProgress] is the
 * app's live attack state: when it changes while Settings shows, Settings reads again, so "Share and rate" follows
 * (EC-SHR-13).
 */
@Composable
fun SettingsDependencies.Settings(
    navController: NavController,
    modifier: Modifier = Modifier,
    attackInProgress: Boolean = false,
    premiumSection: @Composable () -> Unit = {}
) {
    ScreenEnterObserver { settingsViewModel.onEnter() }
    // A screen rebuilt after a rotation, or shown again, reads the settings again.
    LifecycleResumeEffect(settingsViewModel) {
        settingsViewModel.onResume()
        onPauseOrDispose { }
    }
    var attackSeen by remember { mutableStateOf(attackInProgress) }
    LaunchedEffect(attackInProgress) {
        if (attackInProgress != attackSeen) {
            attackSeen = attackInProgress
            settingsViewModel.onAttackChanged()
        }
    }
    ViewModelObserver(navController)
    val viewState by settingsViewModel.viewState.collectAsState(SettingsViewState.Loading)
    val settings = settingsViewStateUiMapper.toUi(viewState)
    // Stamped, so a second failure is a new message and is spoken again; saved, so it survives a rotation (D178).
    var message by rememberSaveable { mutableStateOf<ShownSettingsMessageUiModel?>(null) }
    val show: (SettingsMessageUiModel) -> Unit = {
        message = ShownSettingsMessageUiModel(it, System.currentTimeMillis())
    }
    // Kept across a rotation.
    var choosingTheme by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val launcher = remember(context) { ExternalAppLauncher(context::startActivity) }
    val shareText = stringResource(R.string.settings_share_text, SHARE_LINK)
    val shareSubject = stringResource(R.string.settings_share_subject)
    val shareChooser = stringResource(R.string.settings_share_chooser)
    LaunchedEffect(settingsEventPresenter, launcher) {
        settingsEventPresenter.events.collect { event ->
            when (event) {
                SettingsEventUiModel.NotSaved -> show(SettingsMessageUiModel.NotSaved)
                SettingsEventUiModel.OpenShareSheet -> if (
                    launcher.share(shareText, shareSubject, shareChooser) ==
                    ExternalAppResultUiModel.NothingCouldOpen
                ) {
                    show(SettingsMessageUiModel.ShareUnavailable)
                }
                SettingsEventUiModel.OpenStorePage -> if (
                    launcher.openStorePage() == ExternalAppResultUiModel.NothingCouldOpen
                ) {
                    show(SettingsMessageUiModel.PlayUnavailable)
                }
            }
        }
    }
    // Every Settings message is an error: 8 s, stretched to the accessibility timeout (BRD EC-UX-16; QA #56).
    val messageTimeout = messageTimeoutMillis(READING_MILLIS)
    LaunchedEffect(message, messageTimeout) {
        val current = message
        if (current != null && messageTimeout != null) {
            delay(current.remainingMillis(System.currentTimeMillis(), messageTimeout))
            message = null
        }
    }
    // An error belongs to the visit: Back or forward clears it; the share sheet and Google Play keep it (D178 (2)).
    ScreenLeaveObserver(navController) { message = null }
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
            onDeleteAllData = settingsViewModel::onDeleteAllDataAction,
            onShare = settingsViewModel::onShareAction,
            onRate = settingsViewModel::onRateAction
        ),
        messages = {
            MigrelySnackbarHost(
                message = message,
                reduceMotion = rememberReduceMotion(),
                topOffset = MESSAGE_OFFSET_BELOW_TOP_BAR
            ) { shown ->
                MigrelySnackbar(
                    text = stringResource(shown.message.text()),
                    tone = SnackbarTone.Error,
                    action = null,
                    baseDurationMillis = READING_MILLIS,
                    dismissLabel = stringResource(R.string.settings_message_dismiss),
                    onDismiss = { if (message == shown) message = null }
                )
            }
        },
        modifier = modifier,
        premiumSection = premiumSection
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
}

private fun SettingsMessageUiModel.text() = when (this) {
    SettingsMessageUiModel.NotSaved -> R.string.settings_not_saved
    SettingsMessageUiModel.PlayUnavailable -> R.string.settings_play_unavailable
    SettingsMessageUiModel.ShareUnavailable -> R.string.settings_share_unavailable
}

/** The screen itself, without its view model. Each item is one labelled node (BRD D93, EC-UX-04). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContents(
    settings: SettingsUiModel?,
    actions: SettingsActions,
    messages: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    premiumSection: @Composable () -> Unit = {},
    openingGuard: OpeningGuard = rememberOpeningGuard()
) {
    MessageLayout(
        message = messages,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
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
                windowInsets = MigrelyInsets.bars.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                )
            )
        }
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .widthIn(max = CONTENT_MAX_WIDTH)
                        .fillMaxWidth()
                        .windowInsetsPadding(
                            MigrelyInsets.bars.only(
                                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                            )
                        )
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 16.dp)
                ) {
                    premiumSection()
                    SettingsItems(settings, actions, openingGuard)
                }
            }
        }
    }
}

@Composable
private fun SettingsItems(
    settings: SettingsUiModel?,
    actions: SettingsActions,
    openingGuard: OpeningGuard
) {
    val licencesFocus = remember { FocusRequester() }
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
        onClick = actions.onLicences,
        modifier = Modifier.focusRequester(licencesFocus)
    )
    if (settings != null && settings.version.isNotEmpty()) {
        ValueRow(title = stringResource(R.string.settings_version), value = settings.version)
    }
    // Composed with the content, so it never animates in when Settings opens (ux section 1).
    if (settings != null) {
        ShareAndRateSection(
            shown = settings.showShareAndRate,
            actions = actions,
            openingGuard = openingGuard,
            focusOnLeaving = licencesFocus
        )
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
        titleColor = MaterialTheme.colorScheme.error
    )
}

/**
 * "Share and rate" (BRD SET-F2, D185): absent during an attack and while that is unknown; it fades and folds away, or
 * at once with "Remove animations" (ux section 1, design D5). When it goes while keyboard focus is on one of its rows,
 * focus moves to [focusOnLeaving], never to "Delete all data", which slides into its place (EC-SHR-13). Both rows go
 * through [openingGuard], so one tap opens one thing (EC-SHR-05).
 */
@Composable
private fun ShareAndRateSection(
    shown: Boolean,
    actions: SettingsActions,
    openingGuard: OpeningGuard,
    focusOnLeaving: FocusRequester
) {
    val reduceMotion = rememberReduceMotion()
    val currentlyShown by rememberUpdatedState(shown)
    var focusInSection by remember { mutableStateOf(false) }
    val followFocus = Modifier.onFocusChanged { state ->
        // A row that leaves while focused reports the loss too; that one must not reset the mark.
        if (state.hasFocus) focusInSection = true else if (currentlyShown) focusInSection = false
    }
    LaunchedEffect(shown) {
        if (!shown && focusInSection) {
            focusInSection = false
            focusOnLeaving.requestFocus()
        }
    }
    AnimatedVisibility(
        visible = shown,
        enter = if (reduceMotion) {
            EnterTransition.None
        } else {
            fadeIn(tween(SECTION_MOTION_MILLIS)) + expandVertically(tween(SECTION_MOTION_MILLIS))
        },
        exit = if (reduceMotion) {
            ExitTransition.None
        } else {
            fadeOut(tween(SECTION_MOTION_MILLIS)) + shrinkVertically(tween(SECTION_MOTION_MILLIS))
        }
    ) {
        Column {
            SectionHeader(stringResource(R.string.settings_section_share))
            LinkRow(
                title = stringResource(R.string.settings_share),
                summary = stringResource(R.string.settings_share_summary),
                onClick = { if (openingGuard.tryOpen()) actions.onShare() },
                modifier = followFocus.then(IgnoreHeldActivationKeys),
                opensScreen = false,
                onClickLabel = stringResource(R.string.settings_share_action),
                // Not while it leaves: a tap on a fading row must not open anything during an attack.
                enabled = shown
            )
            LinkRow(
                title = stringResource(R.string.settings_rate),
                summary = stringResource(R.string.settings_rate_summary),
                onClick = { if (openingGuard.tryOpen()) actions.onRate() },
                modifier = followFocus.then(IgnoreHeldActivationKeys),
                opensScreen = false,
                onClickLabel = stringResource(R.string.settings_rate_action),
                enabled = shown
            )
        }
    }
}

/** A held Enter or Space repeats; only its first press counts (EC-SHR-05). */
private val IgnoreHeldActivationKeys = Modifier.onPreviewKeyEvent { event ->
    event.type == KeyEventType.KeyDown &&
        event.nativeKeyEvent.repeatCount > 0 &&
        event.key in ACTIVATION_KEYS
}

private val ACTIVATION_KEYS = setOf(Key.Enter, Key.NumPadEnter, Key.Spacebar, Key.DirectionCenter)

/** The guard for Share and Rate, which Settings' pause and resume release (design D1). */
@Composable
internal fun rememberOpeningGuard(): OpeningGuard {
    val timeout = LocalViewConfiguration.current.doubleTapTimeoutMillis
    val guard = remember(timeout) { OpeningGuard(timeout) { SystemClock.uptimeMillis() } }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { guard.onPause() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { guard.onResume() }
    return guard
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

/**
 * A row that opens a screen, with a chevron, or a dialog or another app, without one. [onClickLabel] names the action
 * for TalkBack ("Double-tap to …"); null keeps the system's words.
 */
@Composable
private fun LinkRow(
    title: String,
    summary: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    opensScreen: Boolean = true,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClickLabel: String? = null,
    enabled: Boolean = true
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ROW_MIN_HEIGHT)
            .clickable(
                enabled = enabled,
                onClickLabel = onClickLabel,
                role = Role.Button,
                onClick = onClick
            )
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

internal val PREVIEW_SETTINGS_ACTIONS = SettingsActions({}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
internal val PREVIEW_SETTINGS = SettingsUiModel(
    periodTracking = true,
    theme = ThemeUiModel.Dark,
    version = "0.1.0",
    showShareAndRate = true
)

@Preview(name = "Settings")
@Composable
private fun SettingsPreview() {
    MigrelyTheme {
        SettingsContents(settings = PREVIEW_SETTINGS, actions = PREVIEW_SETTINGS_ACTIONS, messages = {})
    }
}

@Preview(name = "Settings, light")
@Composable
private fun SettingsLightPreview() {
    MigrelyTheme(dark = false) {
        SettingsContents(
            settings = PREVIEW_SETTINGS.copy(theme = ThemeUiModel.Light),
            actions = PREVIEW_SETTINGS_ACTIONS,
            messages = {}
        )
    }
}

@Preview(name = "Settings, loading")
@Composable
private fun SettingsLoadingPreview() {
    MigrelyTheme {
        SettingsContents(settings = null, actions = PREVIEW_SETTINGS_ACTIONS, messages = {})
    }
}
