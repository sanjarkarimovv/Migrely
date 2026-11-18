package com.migrely.app.medication.ui.view

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
import com.migrely.app.designsystem.snackbar.messageTimeoutMillis
import com.migrely.app.designsystem.snackbar.rememberMessageCountdown
import com.migrely.app.designsystem.snackbar.rememberMessageTimeLeft
import com.migrely.app.medication.ui.R
import com.migrely.app.medication.ui.mapper.MedicationMessageBehaviourUiMapper
import com.migrely.app.medication.ui.model.MedicationEventUiModel
import com.migrely.app.medication.ui.model.MedicationMessageUiModel
import com.migrely.app.medication.ui.model.ShownMedicationMessageUiModel
import com.migrely.app.medication.ui.notification.MedicationEventPresenter

/**
 * The snackbar of a medication screen (BRD EC-UX-16): the newest message replaces the one on screen, its base duration
 * follows the user's "Time to take action", and its countdown pauses while focus holds it and survives a rotation
 * (BRD D182). An error belongs to the visit: leaving the screen by Back or forward clears it, while the background,
 * Android Settings and a rotation keep it (BRD D178 (2)).
 */
@Composable
internal fun MedicationMessages(
    presenter: MedicationEventPresenter,
    navController: NavController,
    behaviourMapper: MedicationMessageBehaviourUiMapper,
    reduceMotion: Boolean,
    onUndoArchive: (medicationId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var shown by rememberSaveable { mutableStateOf<ShownMedicationMessageUiModel?>(null) }
    // True from a clear by a log or a field problem until the next message: no fade (BRD D190 (1), D191, ux L3).
    var leavesAtOnce by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val clock = LocalMessageClock.current
    val currentOnUndoArchive by rememberUpdatedState(onUndoArchive)

    LaunchedEffect(presenter) {
        fun show(message: MedicationMessageUiModel) {
            if (behaviourMapper.toUi(message).tone == SnackbarTone.Error) {
                haptics.performHapticFeedback(HapticFeedbackType.Reject)
            }
            leavesAtOnce = false
            shown = ShownMedicationMessageUiModel(message, clock.wallMillis())
        }
        presenter.events.collect { event ->
            when (event) {
                is MedicationEventUiModel.Message -> show(event.message)
                MedicationEventUiModel.IntakeUndone -> show(MedicationMessageUiModel.IntakeUndone)
                // The logged result replaces the whole page, so a message about the page before goes with it, in
                // the same frame and silently: the logged line is the confirmation (BRD D190 (1)). A field problem
                // is the newest result and its field lies under the message's place, so an older message goes the
                // same way (BRD D191, ux F9).
                is MedicationEventUiModel.Logged,
                is MedicationEventUiModel.NotSaved -> {
                    leavesAtOnce = true
                    shown = null
                }
            }
        }
    }

    ScreenLeaveObserver(navController) {
        if (shown?.let { behaviourMapper.toUi(it.message).tone == SnackbarTone.Error } == true) {
            shown = null
        }
    }

    val current = shown
    val currentBehaviour = current?.let { behaviourMapper.toUi(it.message) }
    val countdown = rememberMessageCountdown(
        message = current,
        timeoutMillis = messageTimeoutMillis(currentBehaviour?.baseDurationMillis),
        // The archive's Undo counts its ceiling from when it showed.
        undoActionWallMillis = current?.shownAtEpochMillis?.takeIf {
            currentBehaviour?.undoesArchive == true
        },
        onExpired = { shown = null }
    )

    MigrelySnackbarHost(
        message = current?.takeUnless { countdown.isOver },
        reduceMotion = reduceMotion || leavesAtOnce,
        modifier = modifier,
        topOffset = MESSAGE_OFFSET_BELOW_TOP_BAR,
        countdown = countdown
    ) { visible ->
        // Cleared by a log or a field problem: it draws nothing while it leaves, so no frame shows it over the page.
        // The host keeps guarding its place against a late tap (BRD D190 (1), ux L3).
        if (leavesAtOnce) return@MigrelySnackbarHost
        val behaviour = behaviourMapper.toUi(visible.message)
        val archived = visible.message as? MedicationMessageUiModel.Archived
        MigrelySnackbar(
            text = messageText(visible.message),
            tone = behaviour.tone,
            action = archived?.let {
                SnackbarAction(
                    label = stringResource(R.string.medication_snackbar_undo),
                    spokenLabel = stringResource(R.string.message_undo_archive_description),
                    onClick = {
                        // Acts only while this is still the message on screen, so it never undoes twice.
                        if (shown == visible) {
                            shown = null
                            currentOnUndoArchive(it.medicationId)
                        }
                    }
                )
            },
            baseDurationMillis = behaviour.baseDurationMillis,
            dismissLabel = stringResource(R.string.medication_snackbar_dismiss),
            onDismiss = { if (shown == visible) shown = null },
            timeLeft = if (!reduceMotion && archived != null) rememberMessageTimeLeft() else null
        )
    }
}

@Composable
private fun messageText(message: MedicationMessageUiModel): String = when (message) {
    is MedicationMessageUiModel.Archived -> stringResource(R.string.message_archived)
    MedicationMessageUiModel.Undone -> stringResource(R.string.medication_message_undone)
    MedicationMessageUiModel.IntakeUndone -> stringResource(R.string.message_intake_undone)
    MedicationMessageUiModel.IntakeKeptStorageFull -> stringResource(
        R.string.medication_message_intake_kept_storage_full
    )
    MedicationMessageUiModel.IntakeKeptFailed -> stringResource(
        R.string.medication_message_intake_kept_generic
    )
    MedicationMessageUiModel.InFuture -> stringResource(R.string.message_in_future)
    MedicationMessageUiModel.LimitReached -> stringResource(R.string.message_limit_reached)
    MedicationMessageUiModel.StorageFull -> stringResource(R.string.medication_message_storage_full)
    MedicationMessageUiModel.SomethingWentWrong -> stringResource(
        R.string.medication_message_generic
    )
}
