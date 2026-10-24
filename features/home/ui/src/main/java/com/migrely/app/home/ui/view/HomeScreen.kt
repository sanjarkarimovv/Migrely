package com.migrely.app.home.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import com.migrely.app.architecture.ui.view.ScreenEnterObserver
import com.migrely.app.designsystem.input.TapGuard
import com.migrely.app.designsystem.motion.rememberReduceMotion
import com.migrely.app.designsystem.theme.MigrelyDarkRoom
import com.migrely.app.home.presentation.model.HomeViewState
import com.migrely.app.home.ui.di.HomeDependencies
import com.migrely.app.home.ui.mapper.SkyAppearanceUiMapper
import com.migrely.app.home.ui.mapper.TimeUiFormatter
import com.migrely.app.home.ui.model.HomeViewStateUiModel
import com.migrely.app.home.ui.model.IntensityLevelUiModel
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.delay

private const val TICK_MILLIS = 30_000L
private const val MODE_FADE_IN_MILLIS = 1_200
private const val MODE_FADE_OUT_MILLIS = 600
private const val COMPACT_HEIGHT_DP = 480
private const val MODE_TAP_GUARD_MILLIS = 1_000L
private const val GUIDANCE_COLUMN_WIDTH_FRACTION = 0.5f

/** Holds the mode shown last without being snapshot state, so reading it never recomposes. */
private class ShownMode(var mode: HomeMode)

/** True in a short window (a phone in landscape), where the home layouts put text and controls side by side. */
@Composable
internal fun isCompactHeight(): Boolean =
    LocalConfiguration.current.screenHeightDp < COMPACT_HEIGHT_DP

/**
 * [monthCalendar] is drawn under today's card and list; `:app` fills it with this month's calendar (change
 * add-attack-history, design D1). [medicationPrompt] is drawn right under today's card; `:app` fills it with the
 * medication card (BRD D46). Both receive the number of view states home has received, so they can load again whenever
 * home does: after every action and on every resume. Attack mode shows neither (G4).
 */
@Composable
fun HomeDependencies.Home(
    navController: NavController,
    startAttackRequested: Boolean,
    modifier: Modifier = Modifier,
    monthCalendar: @Composable (refreshKey: Int) -> Unit = {},
    medicationPrompt: @Composable (refreshKey: Int) -> Unit = {}
) {
    ScreenEnterObserver { homeViewModel.onEnter(startAttackRequested) }
    LifecycleResumeEffect(homeViewModel) {
        homeViewModel.onResume()
        onPauseOrDispose { }
    }
    ViewModelObserver(navController)

    val viewState by homeViewModel.viewState.collectAsState(HomeViewState.Loading)
    val homeLoads by produceState(initialValue = 0, homeViewModel) {
        homeViewModel.viewState.collect { value += 1 }
    }
    val uiState by rememberSaveable(viewState) {
        mutableStateOf(homeViewStateUiMapper.toUi(viewState))
    }
    val nowMillis by rememberNowMillis(
        onDateChanged = homeViewModel::onDateChanged,
        restartKey = (uiState as? HomeViewStateUiModel.AttackInProgress)?.attack?.startEpochMillis
    )
    val reduceMotion = rememberReduceMotion()

    // Where the message ends, so landscape attack mode keeps its guidance below it (BRD D52). It is read only in the
    // layout phase, so a moving message never recomposes the screen.
    val messageBottom = remember { mutableStateOf(0.dp) }
    HomeContents(
        viewState = uiState,
        nowMillis = nowMillis,
        reduceMotion = reduceMotion,
        skyAppearanceUiMapper = skyAppearanceUiMapper,
        timeUiFormatter = timeUiFormatter,
        actions = HomeActions(
            onStartAttack = homeViewModel::onStartAttackAction,
            onIntensity = { level ->
                homeViewModel.onIntensityAction(
                    intensityLevelUiToPresentationMapper.toPresentation(level)
                )
            },
            onMoveStartEarlier = homeViewModel::onMoveStartEarlierAction,
            onEndAttack = homeViewModel::onEndAttackAction,
            onStillGoing = homeViewModel::onStillGoingAction,
            onDontAskAgain = homeViewModel::onDontAskAgainAction,
            onEndedAt = { endEpochMillis ->
                homeViewModel.onEndedAtAction(Instant.ofEpochMilli(endEpochMillis))
            },
            onAttackDetails = homeViewModel::onAttackDetailsAction,
            onAddPastAttack = homeViewModel::onAddPastAttackAction,
            onLogMedication = homeViewModel::onLogMedicationAction,
            onPeriodToday = homeViewModel::onPeriodTodayAction,
            onSettings = homeViewModel::onSettingsAction
        ),
        monthCalendar = { monthCalendar(homeLoads) },
        medicationPrompt = { medicationPrompt(homeLoads) },
        messages = {
            HomeMessages(
                presenter = homeMessagePresenter,
                behaviourMapper = homeMessageBehaviourUiMapper,
                timeUiFormatter = timeUiFormatter,
                reduceMotion = reduceMotion,
                onUndoStart = homeViewModel::onUndoStartAction,
                onUndoEnd = homeViewModel::onUndoEndAction,
                onUndoDelete = homeViewModel::onUndoDeleteAction,
                onMessageBottomChanged = { messageBottom.value = it }
            )
        },
        messageBottom = messageBottom::value,
        modifier = modifier
    )
}

/** Callbacks from the home screen to its ViewModel, grouped to keep [HomeContents] readable. */
data class HomeActions(
    val onStartAttack: () -> Unit,
    val onIntensity: (IntensityLevelUiModel) -> Unit,
    val onMoveStartEarlier: (minutes: Int) -> Unit,
    val onEndAttack: () -> Unit,
    val onStillGoing: (attackId: String) -> Unit,
    val onDontAskAgain: (attackId: String) -> Unit,
    val onEndedAt: (endEpochMillis: Long) -> Unit,
    val onAttackDetails: (attackId: String) -> Unit,
    val onAddPastAttack: () -> Unit,
    val onLogMedication: (attackId: String?) -> Unit,
    val onPeriodToday: (marked: Boolean) -> Unit = {},
    val onSettings: () -> Unit = {}
)

internal enum class HomeMode { Loading, Today, Attack }

/**
 * Whether the content of [next] ignores taps for its first second (BRD EC-UX-18, D32): only when it replaces the other
 * interactive mode, where the second tap of a double tap would land on a different control. Never after Loading, so the
 * app opens and rotates without a wait.
 */
internal fun guardsTaps(previous: HomeMode, next: HomeMode): Boolean =
    previous != HomeMode.Loading && previous != next

/**
 * Whether messages keep to the guidance column (BRD EC-UX-20, D37): in a short window attack mode puts its controls in
 * the right half, starting at the top, where a centred message would cover them (QA #20).
 */
internal fun messagesBesideControls(mode: HomeMode, compactHeight: Boolean): Boolean =
    mode == HomeMode.Attack && compactHeight

private val HomeViewStateUiModel.mode: HomeMode
    get() = when (this) {
        HomeViewStateUiModel.Loading -> HomeMode.Loading
        is HomeViewStateUiModel.Today -> HomeMode.Today
        is HomeViewStateUiModel.AttackInProgress -> HomeMode.Attack
    }

/**
 * The home screen for [viewState], with [messages] drawn on top so a message stays while the mode changes.
 * [messageBottom] reads where the message shown ends, in root coordinates, or 0 dp.
 */
@Composable
fun HomeContents(
    viewState: HomeViewStateUiModel,
    nowMillis: Long,
    reduceMotion: Boolean,
    skyAppearanceUiMapper: SkyAppearanceUiMapper,
    timeUiFormatter: TimeUiFormatter,
    actions: HomeActions,
    messages: @Composable () -> Unit,
    messageBottom: () -> Dp,
    modifier: Modifier = Modifier,
    monthCalendar: @Composable () -> Unit = {},
    medicationPrompt: @Composable () -> Unit = {}
) {
    // Decided from the target mode in the same pass as the switch, so a quick switch back (for example Undo) can never
    // leave a stale previous mode behind (BRD EC-UX-18, D32).
    val shownMode = remember { ShownMode(HomeMode.Loading) }
    val mode = viewState.mode
    val guardTaps = remember(mode) { guardsTaps(shownMode.mode, mode) }
    SideEffect { shownMode.mode = mode }
    // The leaving mode stays touchable until its fade ends, so the guard lasts at least as long as the fade.
    val guardMillis = if (reduceMotion) {
        MODE_TAP_GUARD_MILLIS
    } else {
        maxOf(MODE_TAP_GUARD_MILLIS, MODE_FADE_IN_MILLIS.toLong())
    }
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = viewState,
            contentKey = { it.mode },
            transitionSpec = {
                // Never animate out of Loading: opening the app during an attack shows attack mode at once (EC-UX-12).
                if (reduceMotion || initialState.mode == HomeMode.Loading) {
                    EnterTransition.None togetherWith ExitTransition.None
                } else {
                    fadeIn(tween(MODE_FADE_IN_MILLIS)) togetherWith
                        fadeOut(tween(MODE_FADE_OUT_MILLIS))
                }
            },
            label = "homeMode"
        ) { state ->
            when (state) {
                HomeViewStateUiModel.Loading -> Box(Modifier.fillMaxSize())

                is HomeViewStateUiModel.Today -> TodayContents(
                    today = state.today,
                    dayTitle = timeUiFormatter.formatDayTitle(nowMillis),
                    nowMillis = nowMillis,
                    skyAppearanceUiMapper = skyAppearanceUiMapper,
                    timeUiFormatter = timeUiFormatter,
                    onStartAttack = actions.onStartAttack,
                    onAttackDetails = actions.onAttackDetails,
                    onAddPastAttack = actions.onAddPastAttack,
                    onLogMedication = { actions.onLogMedication(null) },
                    onPeriodToday = actions.onPeriodToday,
                    onSettings = actions.onSettings,
                    monthCalendar = monthCalendar,
                    medicationPrompt = medicationPrompt
                )

                is HomeViewStateUiModel.AttackInProgress -> AttackModeContents(
                    attack = state.attack,
                    nowMillis = nowMillis,
                    reduceMotion = reduceMotion,
                    messageBottom = messageBottom,
                    timeUiFormatter = timeUiFormatter,
                    actions = actions
                )
            }
        }
        // Above both modes and below the messages, so Undo always stays tappable.
        if (guardTaps) {
            TapGuard(
                key = mode,
                durationMillis = guardMillis,
                Modifier.matchParentSize()
            )
        }
        // Half the width at the start edge always ends before the controls column of SideBySideAttackLayout, whatever
        // the insets (design D4).
        val messageArea = if (messagesBesideControls(mode, isCompactHeight())) {
            Modifier.fillMaxWidth(GUIDANCE_COLUMN_WIDTH_FRACTION)
        } else {
            Modifier
        }
        // Messages over attack mode are dark like it; the same call either way, so a message showing keeps its state.
        MigrelyDarkRoom(active = mode == HomeMode.Attack) {
            Box(messageArea) { messages() }
        }
    }
}

/**
 * Current time for the elapsed timer, ticking every 30 seconds; reports when the local date changes (design D11). A new
 * [restartKey], the start of the attack in progress, reads the clock at once, so a moved start never shows a stale
 * elapsed time (QA #15).
 */
@Composable
private fun rememberNowMillis(onDateChanged: () -> Unit, restartKey: Long?): State<Long> {
    val currentOnDateChanged by rememberUpdatedState(onDateChanged)
    return produceState(initialValue = System.currentTimeMillis(), restartKey) {
        value = System.currentTimeMillis()
        var date = LocalDate.now()
        while (true) {
            delay(TICK_MILLIS)
            value = System.currentTimeMillis()
            val today = LocalDate.now()
            if (today != date) {
                date = today
                currentOnDateChanged()
            }
        }
    }
}
