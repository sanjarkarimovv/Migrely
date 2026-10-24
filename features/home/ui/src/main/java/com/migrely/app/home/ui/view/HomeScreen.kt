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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import com.migrely.app.architecture.ui.view.ScreenEnterObserver
import com.migrely.app.designsystem.motion.rememberReduceMotion
import com.migrely.app.designsystem.theme.MigrelyColors
import com.migrely.app.home.presentation.model.HomeViewState
import com.migrely.app.home.ui.R
import com.migrely.app.home.ui.di.HomeDependencies
import com.migrely.app.home.ui.mapper.SkyAppearanceUiMapper
import com.migrely.app.home.ui.mapper.TimeUiFormatter
import com.migrely.app.home.ui.model.HomeMessageUiModel
import com.migrely.app.home.ui.model.HomeViewStateUiModel
import com.migrely.app.home.ui.model.IntensityLevelUiModel
import com.migrely.app.home.ui.notification.HomeMessagePresenter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TICK_MILLIS = 30_000L
private const val UNDO_WINDOW_MILLIS = 5_000L
private const val MODE_FADE_IN_MILLIS = 1_200
private const val MODE_FADE_OUT_MILLIS = 600
private const val COMPACT_HEIGHT_DP = 480

/** True in a short window (a phone in landscape), where the home layouts put text and controls side by side. */
@Composable
internal fun isCompactHeight(): Boolean =
    LocalConfiguration.current.screenHeightDp < COMPACT_HEIGHT_DP

@Composable
fun HomeDependencies.Home(
    navController: NavController,
    startAttackRequested: Boolean,
    modifier: Modifier = Modifier
) {
    ScreenEnterObserver { homeViewModel.onEnter(startAttackRequested) }
    LifecycleResumeEffect(homeViewModel) {
        homeViewModel.onResume()
        onPauseOrDispose { }
    }
    ViewModelObserver(navController)

    val viewState by homeViewModel.viewState.collectAsState(HomeViewState.Loading)
    val uiState by rememberSaveable(viewState) {
        mutableStateOf(homeViewStateUiMapper.toUi(viewState))
    }
    val nowMillis by rememberNowMillis(onDateChanged = homeViewModel::onDateChanged)
    val snackbarHostState = remember { SnackbarHostState() }

    HomeMessagesEffect(
        presenter = homeMessagePresenter,
        snackbarHostState = snackbarHostState,
        timeUiFormatter = timeUiFormatter,
        onUndoStart = homeViewModel::onUndoStartAction,
        onUndoEnd = homeViewModel::onUndoEndAction
    )

    HomeContents(
        viewState = uiState,
        nowMillis = nowMillis,
        reduceMotion = rememberReduceMotion(),
        snackbarHostState = snackbarHostState,
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
            }
        ),
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
    val onEndedAt: (endEpochMillis: Long) -> Unit
)

private enum class HomeMode { Loading, Today, Attack }

private val HomeViewStateUiModel.mode: HomeMode
    get() = when (this) {
        HomeViewStateUiModel.Loading -> HomeMode.Loading
        is HomeViewStateUiModel.Today -> HomeMode.Today
        is HomeViewStateUiModel.AttackInProgress -> HomeMode.Attack
    }

@Composable
fun HomeContents(
    viewState: HomeViewStateUiModel,
    nowMillis: Long,
    reduceMotion: Boolean,
    snackbarHostState: SnackbarHostState,
    skyAppearanceUiMapper: SkyAppearanceUiMapper,
    timeUiFormatter: TimeUiFormatter,
    actions: HomeActions,
    modifier: Modifier = Modifier
) {
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
                    skyAppearanceUiMapper = skyAppearanceUiMapper,
                    onStartAttack = actions.onStartAttack
                )

                is HomeViewStateUiModel.AttackInProgress -> AttackModeContents(
                    attack = state.attack,
                    nowMillis = nowMillis,
                    reduceMotion = reduceMotion,
                    timeUiFormatter = timeUiFormatter,
                    actions = actions
                )
            }
        }
        // At the top so it never covers the controls in the lower half, and dim so it is never the brightest thing on
        // a dark-room screen (BRD EC-UX-01).
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = MigrelyColors.AttackModeSurface,
                contentColor = MigrelyColors.AttackModeSelectedText,
                actionColor = MigrelyColors.AttackModeSelectedOutline
            )
        }
    }
}

/** Current time for the elapsed timer, ticking every 30 seconds; reports when the local date changes (design D11). */
@Composable
private fun rememberNowMillis(onDateChanged: () -> Unit): State<Long> {
    val currentOnDateChanged by rememberUpdatedState(onDateChanged)
    return produceState(initialValue = System.currentTimeMillis()) {
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

@Composable
private fun HomeMessagesEffect(
    presenter: HomeMessagePresenter,
    snackbarHostState: SnackbarHostState,
    timeUiFormatter: TimeUiFormatter,
    onUndoStart: (String) -> Unit,
    onUndoEnd: (String) -> Unit
) {
    val resources = LocalContext.current.resources
    val haptics = LocalHapticFeedback.current
    val currentOnUndoStart by rememberUpdatedState(onUndoStart)
    val currentOnUndoEnd by rememberUpdatedState(onUndoEnd)
    LaunchedEffect(presenter) {
        presenter.messages.collect { message ->
            if (message is HomeMessageUiModel.AttackStarted ||
                message is HomeMessageUiModel.AttackEnded
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
            }
            snackbarHostState.currentSnackbarData?.dismiss()
            launch {
                val text = when (message) {
                    is HomeMessageUiModel.AttackStarted -> resources.getString(
                        R.string.snackbar_attack_started
                    )
                    is HomeMessageUiModel.AttackEnded -> resources.getString(
                        R.string.snackbar_attack_ended
                    )
                    HomeMessageUiModel.StorageFull -> resources.getString(
                        R.string.message_storage_full
                    )
                    is HomeMessageUiModel.StartMoveRejected -> resources.getString(
                        R.string.message_move_rejected,
                        timeUiFormatter.formatClockTime(
                            message.previousEndEpochMillis,
                            ZoneId.systemDefault().id
                        )
                    )

                    HomeMessageUiModel.EndTimeOutOfRange -> resources.getString(
                        R.string.message_end_out_of_range
                    )
                    HomeMessageUiModel.SomethingWentWrong -> resources.getString(
                        R.string.message_generic
                    )
                }
                val canUndo =
                    message is HomeMessageUiModel.AttackStarted ||
                        message is HomeMessageUiModel.AttackEnded
                val autoDismiss = launch {
                    delay(UNDO_WINDOW_MILLIS)
                    snackbarHostState.currentSnackbarData?.dismiss()
                }
                val result = snackbarHostState.showSnackbar(
                    message = text,
                    actionLabel = if (canUndo) {
                        resources.getString(
                            R.string.snackbar_undo
                        )
                    } else {
                        null
                    },
                    duration = SnackbarDuration.Indefinite
                )
                autoDismiss.cancel()
                if (result == SnackbarResult.ActionPerformed) {
                    when (message) {
                        is HomeMessageUiModel.AttackStarted -> currentOnUndoStart(message.attackId)
                        is HomeMessageUiModel.AttackEnded -> currentOnUndoEnd(message.attackId)
                        else -> Unit
                    }
                }
            }
        }
    }
}
