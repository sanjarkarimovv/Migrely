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
import androidx.compose.ui.unit.Dp
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

private const val DELETION_OFFER_MAX_AGE_MILLIS = 30_000L
private const val OFFERED_DELETIONS_KEPT = 10

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
    onMessageBottomChanged: (Dp) -> Unit,
    modifier: Modifier = Modifier
) {
    var shown by rememberSaveable { mutableStateOf<ShownHomeMessageUiModel?>(null) }
    // Deletions already offered, so a return to home or a rotation does not offer one again after a dismissal.
    var offeredDeletions by rememberSaveable {
        mutableStateOf(ArrayList<HomeMessageUiModel.AttackDeleted>())
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
            // A deletion arrives with every return to home while it is restorable; offer it once. Its window starts
            // when home shows it, so the trip back from the details screen does not shorten it.
            if (message is HomeMessageUiModel.AttackDeleted) {
                // Home learns of a deletion right after the details screen closes; an older one, for example after the
                // app was closed and opened again, is past its undo and is never offered.
                val fresh =
                    isFreshDeletion(message.deletedAtEpochMillis, System.currentTimeMillis())
                if (!fresh || message in offeredDeletions) return@collect
                offeredDeletions = ArrayList(
                    offeredDeletions.takeLast(OFFERED_DELETIONS_KEPT - 1) + message
                )
            }
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
            shown = ShownHomeMessageUiModel(message, System.currentTimeMillis())
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
        modifier = modifier,
        onMessageBottomChanged = onMessageBottomChanged
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
            label = stringResource(R.string.home_snackbar_undo),
            spokenLabel = stringResource(
                when (target) {
                    HomeUndoTargetUiModel.Start -> R.string.snackbar_undo_start_description
                    HomeUndoTargetUiModel.End -> R.string.snackbar_undo_end_description
                    HomeUndoTargetUiModel.Delete -> R.string.home_snackbar_undo_delete_description
                }
            ),
            onClick = onUndo
        )
    }
    MigrelySnackbar(
        text = messageText(shown.message, timeUiFormatter),
        tone = behaviour.tone,
        action = action,
        dismissLabel = stringResource(R.string.home_snackbar_dismiss),
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
        is HomeMessageUiModel.AttackDeleted -> stringResource(R.string.home_snackbar_attack_deleted)
        HomeMessageUiModel.Undone -> stringResource(R.string.home_message_undone)
        HomeMessageUiModel.UndoNotPossible -> stringResource(
            R.string.home_message_undo_not_possible
        )
        HomeMessageUiModel.StorageFull -> stringResource(R.string.home_message_storage_full)
        is HomeMessageUiModel.StartMoveRejected -> stringResource(
            R.string.message_move_rejected,
            timeUiFormatter.formatClockTime(
                message.previousEndEpochMillis,
                ZoneId.systemDefault().id
            )
        )

        HomeMessageUiModel.EndTimeOutOfRange -> stringResource(R.string.message_end_out_of_range)
        HomeMessageUiModel.SomethingWentWrong -> stringResource(R.string.home_message_generic)
    }

/**
 * Whether a deletion made at [deletedAtMillis] may still be offered for undo at [nowMillis]: at most 30 s old, and not
 * stamped after now. After the clock was set back, a deletion's age reads negative, and it was offered at every start
 * (QA #112).
 */
internal fun isFreshDeletion(deletedAtMillis: Long, nowMillis: Long): Boolean =
    nowMillis - deletedAtMillis in 0..DELETION_OFFER_MAX_AGE_MILLIS
