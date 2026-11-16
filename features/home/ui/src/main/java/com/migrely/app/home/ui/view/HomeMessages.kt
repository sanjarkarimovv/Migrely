package com.migrely.app.home.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.migrely.app.architecture.ui.view.ScreenLeaveObserver
import com.migrely.app.designsystem.snackbar.LocalMessageClock
import com.migrely.app.designsystem.snackbar.MigrelySnackbar
import com.migrely.app.designsystem.snackbar.MigrelySnackbarHost
import com.migrely.app.designsystem.snackbar.SnackbarAction
import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.designsystem.snackbar.isFreshDeletion
import com.migrely.app.designsystem.snackbar.messageTimeoutMillis
import com.migrely.app.designsystem.snackbar.rememberMessageCountdown
import com.migrely.app.designsystem.snackbar.rememberTimeLeft
import com.migrely.app.home.ui.R
import com.migrely.app.home.ui.mapper.HomeMessageBehaviourUiMapper
import com.migrely.app.home.ui.mapper.TimeUiFormatter
import com.migrely.app.home.ui.model.HomeMessageBehaviourUiModel
import com.migrely.app.home.ui.model.HomeMessageUiModel
import com.migrely.app.home.ui.model.HomeUndoTargetUiModel
import com.migrely.app.home.ui.model.ShownHomeMessageUiModel
import com.migrely.app.home.ui.notification.HomeMessagePresenter
import java.time.ZoneId

private const val OFFERED_DELETIONS_KEPT = 10

/**
 * The home screen's messages (BRD EC-UX-16). The newest message replaces the one on screen. Each message's base duration
 * is stretched to the user's "Time to take action", and its countdown pauses while focus holds it and survives a
 * rotation; an Undo never outlives what it restores (BRD D182).
 *
 * An error belongs to the visit: leaving Home by Back or forward clears it, while the background, Android Settings and a
 * rotation keep it (BRD D178 (2)). [onShownChanged] tells whether any message shows and [onErrorPosted] that an error
 * came, so Home can hold its review request (BRD D178 (3)).
 */
@Suppress("LongParameterList")
@Composable
internal fun HomeMessages(
    presenter: HomeMessagePresenter,
    navController: NavController,
    behaviourMapper: HomeMessageBehaviourUiMapper,
    timeUiFormatter: TimeUiFormatter,
    reduceMotion: Boolean,
    onUndoStart: (attackId: String) -> Unit,
    onUndoEnd: (attackId: String) -> Unit,
    onUndoDelete: (attackId: String) -> Unit,
    onMessageBottomChanged: (Dp) -> Unit,
    modifier: Modifier = Modifier,
    topOffset: Dp = 0.dp,
    onShownChanged: (Boolean) -> Unit = {},
    onErrorPosted: () -> Unit = {}
) {
    var shown by rememberSaveable { mutableStateOf<ShownHomeMessageUiModel?>(null) }
    // Deletions already offered, so a return to home or a rotation does not offer one again after a dismissal.
    var offeredDeletions by rememberSaveable {
        mutableStateOf(ArrayList<HomeMessageUiModel.AttackDeleted>())
    }
    val haptics = LocalHapticFeedback.current
    val clock = LocalMessageClock.current
    val currentOnUndoStart by rememberUpdatedState(onUndoStart)
    val currentOnUndoEnd by rememberUpdatedState(onUndoEnd)
    val currentOnUndoDelete by rememberUpdatedState(onUndoDelete)
    val currentOnErrorPosted by rememberUpdatedState(onErrorPosted)

    fun errorShown(): Boolean =
        shown?.let { behaviourMapper.toUi(it.message).tone == SnackbarTone.Error } == true

    LaunchedEffect(presenter) {
        presenter.messages.collect { message ->
            // "Saved." only replaces an error that the write made untrue; any other message stays, and with none the
            // write needs no word (BRD D178 (1)). Cleared on success, never on the attempt, so a failed retry shows no
            // flash.
            if (message == HomeMessageUiModel.Saved && !errorShown()) return@collect
            // A deletion arrives with every return to home while it is restorable; offer it once. Its window starts
            // when home shows it, so the trip back from the details screen does not shorten it.
            if (message is HomeMessageUiModel.AttackDeleted) {
                // Home learns of a deletion right after the details screen closes; an older one, for example after the
                // app was closed and opened again, is past its undo and is never offered.
                val fresh =
                    isFreshDeletion(message.deletedAtEpochMillis, clock.wallMillis())
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
            if (behaviourMapper.toUi(message).tone == SnackbarTone.Error) currentOnErrorPosted()
            shown = ShownHomeMessageUiModel(message, clock.wallMillis())
        }
    }
    ScreenLeaveObserver(navController) { if (errorShown()) shown = null }
    val showing = shown != null
    val currentOnShownChanged by rememberUpdatedState(onShownChanged)
    LaunchedEffect(showing) { currentOnShownChanged(showing) }

    val current = shown
    val currentBehaviour = current?.let { behaviourMapper.toUi(it.message) }
    val countdown = rememberMessageCountdown(
        message = current,
        timeoutMillis = messageTimeoutMillis(currentBehaviour?.baseDurationMillis),
        undoActionWallMillis = current?.undoActionWallMillis(currentBehaviour),
        onExpired = { shown = null }
    )

    MigrelySnackbarHost(
        message = current?.takeUnless { countdown.isOver },
        reduceMotion = reduceMotion,
        modifier = modifier,
        topOffset = topOffset,
        onMessageBottomChanged = onMessageBottomChanged,
        countdown = countdown
    ) { visible ->
        val behaviour = behaviourMapper.toUi(visible.message)
        HomeSnackbar(
            shown = visible,
            behaviour = behaviour,
            timeLeft = if (!reduceMotion && behaviour.undoTarget != null) {
                countdown.rememberTimeLeft()
            } else {
                null
            },
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

/** The action an Undo undoes, where its ceiling counts from: the deletion itself, or the moment it showed. */
private fun ShownHomeMessageUiModel.undoActionWallMillis(
    behaviour: HomeMessageBehaviourUiModel?
): Long? = when {
    behaviour?.undoTarget == null -> null
    message is HomeMessageUiModel.AttackDeleted -> message.deletedAtEpochMillis
    else -> shownAtEpochMillis
}

@Suppress("LongParameterList")
@Composable
private fun HomeSnackbar(
    shown: ShownHomeMessageUiModel,
    behaviour: HomeMessageBehaviourUiModel,
    timeLeft: (() -> Float)?,
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
        baseDurationMillis = behaviour.baseDurationMillis,
        dismissLabel = stringResource(R.string.home_snackbar_dismiss),
        onDismiss = onDismiss,
        timeLeft = timeLeft
    )
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
        HomeMessageUiModel.Saved -> stringResource(R.string.home_message_saved)
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
