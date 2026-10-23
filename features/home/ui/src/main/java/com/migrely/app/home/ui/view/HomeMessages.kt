package com.migrely.app.home.ui.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.migrely.app.designsystem.snackbar.MigrelySnackbar
import com.migrely.app.designsystem.snackbar.MigrelySnackbarHost
import com.migrely.app.designsystem.snackbar.SnackbarAction
import com.migrely.app.home.ui.R
import com.migrely.app.home.ui.mapper.HomeMessageBehaviourUiMapper
import com.migrely.app.home.ui.mapper.TimeUiFormatter
import com.migrely.app.home.ui.model.HomeMessageBehaviourUiModel
import com.migrely.app.home.ui.model.HomeMessageUiModel
import com.migrely.app.home.ui.model.HomeUndoTargetUiModel
import com.migrely.app.home.ui.model.ShownHomeMessageUiModel
import com.migrely.app.home.ui.notification.HomeMessagePresenter
import java.time.ZoneId
import kotlinx.coroutines.delay

/**
 * The home screen's messages (BRD EC-UX-16). The newest message replaces the one on screen. The message and the time
 * it appeared are saved, so after a rotation it stays only for the time it has left. Each message's base duration is
 * stretched to the user's accessibility timeout ("Time to take action").
 */
@Composable
internal fun HomeMessages(
    presenter: HomeMessagePresenter,
    behaviourMapper: HomeMessageBehaviourUiMapper,
    timeUiFormatter: TimeUiFormatter,
    reduceMotion: Boolean,
    onUndoStart: (attackId: String) -> Unit,
    onUndoEnd: (attackId: String) -> Unit,
    onUndoDelete: (attackId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var shown by rememberSaveable { mutableStateOf<ShownHomeMessageUiModel?>(null) }
    // The deletion already offered, so returning to home or rotating does not offer it again after a dismissal.
    var offeredDeletion by rememberSaveable {
        mutableStateOf<HomeMessageUiModel.AttackDeleted?>(null)
    }
    val haptics = LocalHapticFeedback.current
    val accessibilityManager = LocalAccessibilityManager.current
    val currentOnUndoStart by rememberUpdatedState(onUndoStart)
    val currentOnUndoEnd by rememberUpdatedState(onUndoEnd)
    val currentOnUndoDelete by rememberUpdatedState(onUndoDelete)

    fun timeoutMillis(behaviour: HomeMessageBehaviourUiModel): Long? =
        behaviour.baseDurationMillis?.let { base ->
            accessibilityManager?.calculateRecommendedTimeoutMillis(
                originalTimeoutMillis = base,
                containsIcons = true,
                containsText = true,
                containsControls = behaviour.undoTarget != null
            ) ?: base
        }

    LaunchedEffect(presenter) {
        presenter.messages.collect { message ->
            val now = System.currentTimeMillis()
            // A deletion is timed from the moment it happened on the details screen, not from when home shows it.
            val shownAt = if (message is HomeMessageUiModel.AttackDeleted) {
                if (message == offeredDeletion) return@collect
                offeredDeletion = message
                message.deletedAtEpochMillis
            } else {
                now
            }
            val candidate = ShownHomeMessageUiModel(message, shownAt)
            val timeout = timeoutMillis(behaviourMapper.toUi(message))
            if (timeout != null && candidate.remainingMillis(now, timeout) == 0L) return@collect
            when (message) {
                is HomeMessageUiModel.AttackStarted,
                is HomeMessageUiModel.AttackEnded -> haptics.performHapticFeedback(
                    HapticFeedbackType.Confirm
                )

                HomeMessageUiModel.StorageFull,
                HomeMessageUiModel.SomethingWentWrong -> haptics.performHapticFeedback(
                    HapticFeedbackType.Reject
                )

                else -> Unit
            }
            shown = candidate
        }
    }

    val current = shown
    val currentTimeout = current?.let { timeoutMillis(behaviourMapper.toUi(it.message)) }
    LaunchedEffect(current, currentTimeout) {
        if (current != null && currentTimeout != null) {
            delay(current.remainingMillis(System.currentTimeMillis(), currentTimeout))
            shown = null
        }
    }

    MigrelySnackbarHost(
        message = current,
        reduceMotion = reduceMotion,
        modifier = modifier
    ) { visible ->
        val behaviour = behaviourMapper.toUi(visible.message)
        HomeSnackbar(
            shown = visible,
            behaviour = behaviour,
            timeoutMillis = timeoutMillis(behaviour),
            showTimeLeft = !reduceMotion,
            timeUiFormatter = timeUiFormatter,
            // Both act only while this is still the message on screen: a message fading out after a tap, or one a
            // newer message replaced, must not undo twice or clear the newer message.
            onUndo = {
                if (shown == visible) {
                    shown = null
                    when (val message = visible.message) {
                        is HomeMessageUiModel.AttackStarted -> currentOnUndoStart(message.attackId)
                        is HomeMessageUiModel.AttackEnded -> currentOnUndoEnd(message.attackId)
                        is HomeMessageUiModel.AttackDeleted -> currentOnUndoDelete(message.attackId)
                        else -> Unit
                    }
                }
            },
            onDismiss = { if (shown == visible) shown = null }
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun HomeSnackbar(
    shown: ShownHomeMessageUiModel,
    behaviour: HomeMessageBehaviourUiModel,
    timeoutMillis: Long?,
    showTimeLeft: Boolean,
    timeUiFormatter: TimeUiFormatter,
    onUndo: () -> Unit,
    onDismiss: () -> Unit
) {
    val undoTarget = behaviour.undoTarget
    val action = undoTarget?.let { target ->
        SnackbarAction(
            label = stringResource(R.string.snackbar_undo),
            spokenLabel = stringResource(
                when (target) {
                    HomeUndoTargetUiModel.Start -> R.string.snackbar_undo_start_description
                    HomeUndoTargetUiModel.End -> R.string.snackbar_undo_end_description
                    HomeUndoTargetUiModel.Delete -> R.string.snackbar_undo_delete_description
                }
            ),
            onClick = onUndo
        )
    }
    MigrelySnackbar(
        text = messageText(shown.message, timeUiFormatter),
        tone = behaviour.tone,
        action = action,
        dismissLabel = stringResource(R.string.snackbar_dismiss),
        onDismiss = onDismiss,
        timeLeft = if (showTimeLeft && undoTarget != null && timeoutMillis != null) {
            rememberTimeLeft(shown, timeoutMillis)
        } else {
            null
        }
    )
}

/** A fraction from the time left now down to 0, reaching 0 when the message's time is up. */
@Composable
private fun rememberTimeLeft(shown: ShownHomeMessageUiModel, timeoutMillis: Long): () -> Float {
    val remaining = remember(shown, timeoutMillis) {
        shown.remainingMillis(System.currentTimeMillis(), timeoutMillis)
    }
    val fraction = remember(shown, timeoutMillis) {
        Animatable(remaining.toFloat() / timeoutMillis)
    }
    LaunchedEffect(fraction) {
        fraction.animateTo(0f, tween(durationMillis = remaining.toInt(), easing = LinearEasing))
    }
    return { fraction.value }
}

@Composable
private fun messageText(message: HomeMessageUiModel, timeUiFormatter: TimeUiFormatter): String =
    when (message) {
        is HomeMessageUiModel.AttackStarted -> stringResource(R.string.snackbar_attack_started)
        is HomeMessageUiModel.AttackEnded -> stringResource(R.string.snackbar_attack_ended)
        is HomeMessageUiModel.AttackDeleted -> stringResource(R.string.snackbar_attack_deleted)
        HomeMessageUiModel.Undone -> stringResource(R.string.message_undone)
        HomeMessageUiModel.UndoNotPossible -> stringResource(R.string.message_undo_not_possible)
        HomeMessageUiModel.StorageFull -> stringResource(R.string.message_storage_full)
        is HomeMessageUiModel.StartMoveRejected -> stringResource(
            R.string.message_move_rejected,
            timeUiFormatter.formatClockTime(
                message.previousEndEpochMillis,
                ZoneId.systemDefault().id
            )
        )

        HomeMessageUiModel.EndTimeOutOfRange -> stringResource(R.string.message_end_out_of_range)
        HomeMessageUiModel.SomethingWentWrong -> stringResource(R.string.message_generic)
    }
