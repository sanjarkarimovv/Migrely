package com.migrely.app.designsystem.snackbar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.delay

/**
 * The countdown of the message a host shows, from [rememberMessageCountdown]. Give it to [MigrelySnackbarHost], which
 * tells it where keyboard and TalkBack focus are, and draw its line with [rememberTimeLeft].
 */
@Stable
class MessageCountdownState internal constructor(
    internal val countdown: MessageCountdown?,
    /** True when a restored message's time or ceiling had already passed: show nothing (BRD EC-UX-57). */
    val isOver: Boolean
) {
    internal var keyboardFocus by mutableStateOf(false)
    internal var screenReaderFocus by mutableStateOf(false)

    // Bumped whenever the countdown must be looked at again: a hold, a resume, a timeout, a wake-up.
    internal var version by mutableIntStateOf(0)
        private set

    internal fun wake() {
        version++
    }
}

/**
 * One countdown for [message], the value a host shows, or null when none shows (BRD D182 (2), (3)). It runs for
 * [timeoutMillis] of unpaused time, from [messageTimeoutMillis], and an Undo, with [undoActionWallMillis] set, never
 * passes [UNDO_CEILING_MILLIS] after that action. At the end it calls [onExpired], where the host drops the message,
 * as on a timeout.
 *
 * It pauses while keyboard or TalkBack focus holds the message, goes on after the grace or at once when the window loses
 * focus, and looks again on every resume, so a lock and unlock never shows an Undo past its ceiling (EC-ATK-24). Its
 * state survives rotation and process death keyed by [message]; a restored message whose time has run out shows
 * nothing ([MessageCountdownState.isOver]).
 */
@Composable
fun rememberMessageCountdown(
    message: Any?,
    timeoutMillis: Long?,
    undoActionWallMillis: Long?,
    onExpired: () -> Unit
): MessageCountdownState {
    val clock = LocalMessageClock.current
    val currentTimeout by rememberUpdatedState(timeoutMillis)
    val currentOnExpired by rememberUpdatedState(onExpired)
    val state = rememberSaveable(message, saver = countdownSaver(clock) { currentTimeout }) {
        MessageCountdownState(
            countdown = message?.let {
                MessageCountdown.start(clock, timeoutMillis, undoActionWallMillis)
            },
            isOver = false
        )
    }
    HoldEffect(state)
    LaunchedEffect(state, timeoutMillis) {
        state.countdown?.timeoutChanged(timeoutMillis)
        state.wake()
    }
    LifecycleResumeEffect(state) {
        state.wake()
        onPauseOrDispose {}
    }
    LaunchedEffect(state, state.version) {
        val countdown = state.countdown
        when {
            countdown == null -> if (state.isOver) currentOnExpired()
            countdown.isExpired() -> currentOnExpired()
            else -> countdown.nextCheckInMillis()?.let {
                delay(it)
                state.wake()
            }
        }
    }
    return state
}

/** Feeds the hold: the focus [MigrelySnackbarHost] reports, whether TalkBack is on, and the window's focus. */
@Composable
private fun HoldEffect(state: MessageCountdownState) {
    val countdown = state.countdown ?: return
    val windowInfo = LocalWindowInfo.current
    val screenReaderOn = rememberScreenReaderOn()
    LaunchedEffect(state, windowInfo, screenReaderOn) {
        snapshotFlow {
            CountdownHold(
                keyboardFocus = state.keyboardFocus,
                screenReaderFocus = state.screenReaderFocus,
                screenReaderOn = screenReaderOn.value,
                windowFocused = windowInfo.isWindowFocused
            )
        }.collect { hold ->
            countdown.update(hold)
            state.wake()
        }
    }
}

/**
 * The time-left line for [MigrelySnackbar]'s `timeLeft`: it stops while paused, never grows, and runs to 0 at the real
 * end, the earlier of the timeout and the ceiling (BRD EC-UX-53). Ask for it only when the line is drawn.
 */
@Composable
fun MessageCountdownState.rememberTimeLeft(): () -> Float {
    val line = remember(this) { Animatable(countdown?.timeLeftFraction() ?: 0f) }
    LaunchedEffect(this, version) {
        val countdown = countdown ?: return@LaunchedEffect
        line.snapTo(countdown.timeLeftFraction())
        val left = countdown.remainingMillis()
        if (!countdown.isPaused() && left != null && left > 0) {
            line.animateTo(0f, tween(left.toInt(), easing = LinearEasing))
        }
    }
    return { line.value }
}

/** Saves the countdown, not the message: the host saves that, and keys this to it. */
private fun countdownSaver(
    clock: MessageClock,
    timeoutMillis: () -> Long?
): Saver<MessageCountdownState, Any> = Saver(
    save = { state ->
        state.countdown?.save()?.let {
            arrayListOf(
                it.runMillis,
                it.savedElapsedMillis,
                it.shownElapsedMillis,
                it.actionWallMillis,
                it.lineFraction
            )
        }
    },
    restore = { saved ->
        val values = saved as List<*>
        val countdown = MessageCountdown.restore(
            clock,
            timeoutMillis(),
            SavedMessageCountdown(
                runMillis = values[0] as Long,
                savedElapsedMillis = values[1] as Long,
                shownElapsedMillis = values[2] as Long,
                actionWallMillis = values[3] as Long?,
                lineFraction = values[4] as Float
            )
        )
        MessageCountdownState(countdown, isOver = countdown == null)
    }
)
