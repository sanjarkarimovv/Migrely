package com.migrely.app.history.ui.view

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
import com.migrely.app.designsystem.snackbar.isFreshDeletion
import com.migrely.app.history.ui.R
import com.migrely.app.history.ui.mapper.HistoryMessageBehaviourUiMapper
import com.migrely.app.history.ui.model.HistoryMessageBehaviourUiModel
import com.migrely.app.history.ui.model.HistoryMessageUiModel
import com.migrely.app.history.ui.model.ShownHistoryMessageUiModel
import com.migrely.app.history.ui.notification.HistoryMessagePresenter
import kotlinx.coroutines.delay

private const val OFFERED_DELETIONS_KEPT = 10

/**
 * History's messages (BRD EC-UX-16), as on home: the newest replaces the one on screen, and the message and the time it
 * appeared are saved, so after a rotation it stays only for the time it has left. Each base duration is stretched to
 * the user's accessibility timeout. A deletion made on the details screen opened from here is offered once
 * (BRD EC-HIS-04, D31); one made before History was opened, for example from home, belongs to home and is not offered.
 */
@Composable
internal fun HistoryMessages(
    presenter: HistoryMessagePresenter,
    behaviourMapper: HistoryMessageBehaviourUiMapper,
    reduceMotion: Boolean,
    onUndoDelete: (attackId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var shown by rememberSaveable { mutableStateOf<ShownHistoryMessageUiModel?>(null) }
    val enteredAtEpochMillis = rememberSaveable { System.currentTimeMillis() }
    // Deletions already offered, so a return to History or a rotation does not offer one again after a dismissal.
    var offeredDeletions by rememberSaveable {
        mutableStateOf(ArrayList<HistoryMessageUiModel.AttackDeleted>())
    }
    val haptics = LocalHapticFeedback.current
    val accessibilityManager = LocalAccessibilityManager.current
    val currentOnUndoDelete by rememberUpdatedState(onUndoDelete)

    fun timeoutMillis(behaviour: HistoryMessageBehaviourUiModel): Long? =
        behaviour.baseDurationMillis?.let { base ->
            accessibilityManager?.calculateRecommendedTimeoutMillis(
                originalTimeoutMillis = base,
                containsIcons = true,
                containsText = true,
                containsControls = behaviour.undoesDeletion
            ) ?: base
        }

    LaunchedEffect(presenter) {
        presenter.messages.collect { message ->
            if (message is HistoryMessageUiModel.AttackDeleted) {
                // An older deletion, for example after the app was closed and opened again, is past its undo.
                val fresh =
                    isFreshDeletion(message.deletedAtEpochMillis, System.currentTimeMillis())
                val madeHere = message.deletedAtEpochMillis >= enteredAtEpochMillis
                if (!fresh || !madeHere || message in offeredDeletions) return@collect
                offeredDeletions = ArrayList(
                    offeredDeletions.takeLast(OFFERED_DELETIONS_KEPT - 1) + message
                )
            }
            if (message == HistoryMessageUiModel.SomethingWentWrong ||
                message == HistoryMessageUiModel.StorageFull
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.Reject)
            }
            shown = ShownHistoryMessageUiModel(message, System.currentTimeMillis())
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
        val timeout = timeoutMillis(behaviour)
        MigrelySnackbar(
            text = messageText(visible.message),
            tone = behaviour.tone,
            action = if (behaviour.undoesDeletion) {
                SnackbarAction(
                    label = stringResource(R.string.history_snackbar_undo),
                    spokenLabel = stringResource(R.string.history_snackbar_undo_delete_description),
                    // Acts only while this is still the message on screen, so a fading or replaced message
                    // never undoes twice.
                    onClick = {
                        val message = visible.message
                        if (shown == visible && message is HistoryMessageUiModel.AttackDeleted) {
                            shown = null
                            currentOnUndoDelete(message.attackId)
                        }
                    }
                )
            } else {
                null
            },
            dismissLabel = stringResource(R.string.history_snackbar_dismiss),
            onDismiss = { if (shown == visible) shown = null },
            timeLeft = if (!reduceMotion && behaviour.undoesDeletion && timeout != null) {
                rememberTimeLeft(visible, timeout)
            } else {
                null
            }
        )
    }
}

/** A fraction from the time left now down to 0, reaching 0 when the message's time is up. */
@Composable
private fun rememberTimeLeft(shown: ShownHistoryMessageUiModel, timeoutMillis: Long): () -> Float {
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
private fun messageText(message: HistoryMessageUiModel): String = when (message) {
    is HistoryMessageUiModel.AttackDeleted -> stringResource(
        R.string.history_snackbar_attack_deleted
    )
    HistoryMessageUiModel.Undone -> stringResource(R.string.history_message_undone)
    HistoryMessageUiModel.UndoNotPossible -> stringResource(
        R.string.history_message_undo_not_possible
    )
    HistoryMessageUiModel.AttackLoggedOnDay -> stringResource(R.string.message_attack_logged_on_day)
    HistoryMessageUiModel.StorageFull -> stringResource(R.string.history_message_storage_full)
    HistoryMessageUiModel.SomethingWentWrong -> stringResource(R.string.history_message_generic)
}
