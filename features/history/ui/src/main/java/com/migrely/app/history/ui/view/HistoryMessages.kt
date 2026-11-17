package com.migrely.app.history.ui.view

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
import androidx.navigation.NavController
import com.migrely.app.architecture.ui.view.ScreenLeaveObserver
import com.migrely.app.designsystem.snackbar.LocalMessageClock
import com.migrely.app.designsystem.snackbar.MESSAGE_OFFSET_BELOW_TOP_BAR
import com.migrely.app.designsystem.snackbar.MigrelySnackbar
import com.migrely.app.designsystem.snackbar.MigrelySnackbarHost
import com.migrely.app.designsystem.snackbar.SnackbarAction
import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.designsystem.snackbar.isFreshDeletion
import com.migrely.app.designsystem.snackbar.messageTimeoutMillis
import com.migrely.app.designsystem.snackbar.rememberMessageCountdown
import com.migrely.app.designsystem.snackbar.rememberMessageTimeLeft
import com.migrely.app.history.ui.R
import com.migrely.app.history.ui.mapper.HistoryMessageBehaviourUiMapper
import com.migrely.app.history.ui.model.HistoryMessageUiModel
import com.migrely.app.history.ui.model.ShownHistoryMessageUiModel
import com.migrely.app.history.ui.notification.HistoryMessagePresenter

private const val OFFERED_DELETIONS_KEPT = 10

/**
 * History's messages (BRD EC-UX-16), as on home: the newest replaces the one on screen, each base duration is stretched
 * to the user's "Time to take action", and the countdown pauses while focus holds the message and survives a rotation;
 * an Undo never outlives what it restores (BRD D182). A deletion made on the details screen opened from here is offered
 * once (BRD EC-HIS-04, D31); one made before History was opened, for example from home, belongs to home and is not
 * offered. An error belongs to the visit: leaving History by Back or forward clears it (BRD D178 (2)).
 */
@Composable
internal fun HistoryMessages(
    presenter: HistoryMessagePresenter,
    navController: NavController,
    behaviourMapper: HistoryMessageBehaviourUiMapper,
    reduceMotion: Boolean,
    onUndoDelete: (attackId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var shown by rememberSaveable { mutableStateOf<ShownHistoryMessageUiModel?>(null) }
    val clock = LocalMessageClock.current
    val enteredAtEpochMillis = rememberSaveable { clock.wallMillis() }
    // Deletions already offered, so a return to History or a rotation does not offer one again after a dismissal.
    var offeredDeletions by rememberSaveable {
        mutableStateOf(ArrayList<HistoryMessageUiModel.AttackDeleted>())
    }
    val haptics = LocalHapticFeedback.current
    val currentOnUndoDelete by rememberUpdatedState(onUndoDelete)

    fun errorShown(): Boolean =
        shown?.let { behaviourMapper.toUi(it.message).tone == SnackbarTone.Error } == true

    LaunchedEffect(presenter) {
        presenter.messages.collect { message ->
            // "Saved." only replaces an error that the write made untrue; any other message stays, and with none the
            // write needs no word (BRD D178 (1)).
            if (message == HistoryMessageUiModel.Saved && !errorShown()) return@collect
            if (message is HistoryMessageUiModel.AttackDeleted) {
                // An older deletion, for example after the app was closed and opened again, is past its undo.
                val fresh =
                    isFreshDeletion(message.deletedAtEpochMillis, clock.wallMillis())
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
            shown = ShownHistoryMessageUiModel(message, clock.wallMillis())
        }
    }

    ScreenLeaveObserver(navController) { if (errorShown()) shown = null }

    val current = shown
    val countdown = rememberMessageCountdown(
        message = current,
        timeoutMillis = messageTimeoutMillis(
            current?.let { behaviourMapper.toUi(it.message).baseDurationMillis }
        ),
        // A deletion's Undo counts its ceiling from the deletion itself (EC-ATK-24).
        undoActionWallMillis = (current?.message as? HistoryMessageUiModel.AttackDeleted)
            ?.deletedAtEpochMillis,
        onExpired = { shown = null }
    )

    MigrelySnackbarHost(
        message = current?.takeUnless { countdown.isOver },
        reduceMotion = reduceMotion,
        modifier = modifier,
        topOffset = MESSAGE_OFFSET_BELOW_TOP_BAR,
        countdown = countdown
    ) { visible ->
        val behaviour = behaviourMapper.toUi(visible.message)
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
            baseDurationMillis = behaviour.baseDurationMillis,
            dismissLabel = stringResource(R.string.history_snackbar_dismiss),
            onDismiss = { if (shown == visible) shown = null },
            timeLeft = if (!reduceMotion && behaviour.undoesDeletion) {
                rememberMessageTimeLeft()
            } else {
                null
            }
        )
    }
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
    HistoryMessageUiModel.Saved -> stringResource(R.string.history_message_saved)
    HistoryMessageUiModel.StorageFull -> stringResource(R.string.history_message_storage_full)
    HistoryMessageUiModel.SomethingWentWrong -> stringResource(R.string.history_message_generic)
}
