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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import com.migrely.app.architecture.ui.view.ScreenEnterObserver
import com.migrely.app.designsystem.motion.rememberReduceMotion
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
    val reduceMotion = rememberReduceMotion()

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
            onAttackDetails = homeViewModel::onAttackDetailsAction
        ),
        messages = {
            HomeMessages(
                presenter = homeMessagePresenter,
                behaviourMapper = homeMessageBehaviourUiMapper,
                timeUiFormatter = timeUiFormatter,
                reduceMotion = reduceMotion,
                onUndoStart = homeViewModel::onUndoStartAction,
                onUndoEnd = homeViewModel::onUndoEndAction,
                onUndoDelete = homeViewModel::onUndoDeleteAction
            )
        },
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
    val onAttackDetails: (attackId: String) -> Unit
)

private enum class HomeMode { Loading, Today, Attack }

private val HomeViewStateUiModel.mode: HomeMode
    get() = when (this) {
        HomeViewStateUiModel.Loading -> HomeMode.Loading
        is HomeViewStateUiModel.Today -> HomeMode.Today
        is HomeViewStateUiModel.AttackInProgress -> HomeMode.Attack
    }

/** The home screen for [viewState], with [messages] drawn on top so a message stays while the mode changes. */
@Composable
fun HomeContents(
    viewState: HomeViewStateUiModel,
    nowMillis: Long,
    reduceMotion: Boolean,
    skyAppearanceUiMapper: SkyAppearanceUiMapper,
    timeUiFormatter: TimeUiFormatter,
    actions: HomeActions,
    messages: @Composable () -> Unit,
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
                    nowMillis = nowMillis,
                    skyAppearanceUiMapper = skyAppearanceUiMapper,
                    timeUiFormatter = timeUiFormatter,
                    onStartAttack = actions.onStartAttack,
                    onAttackDetails = actions.onAttackDetails
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
        messages()
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
