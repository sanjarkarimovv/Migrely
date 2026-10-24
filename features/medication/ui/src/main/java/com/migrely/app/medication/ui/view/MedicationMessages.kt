package com.migrely.app.medication.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.medication.ui.R
import com.migrely.app.medication.ui.mapper.MedicationMessageBehaviourUiMapper
import com.migrely.app.medication.ui.model.MedicationEventUiModel
import com.migrely.app.medication.ui.model.MedicationMessageBehaviourUiModel
import com.migrely.app.medication.ui.model.MedicationMessageUiModel
import com.migrely.app.medication.ui.model.ShownMedicationMessageUiModel
import com.migrely.app.medication.ui.notification.MedicationEventPresenter
import kotlinx.coroutines.delay

/**
 * The snackbar of a medication screen (BRD EC-UX-16): the newest message replaces the one on screen, it is saved with
 * the time it appeared so rotation keeps only the time it has left, and its base duration follows the accessibility
 * timeout.
 */
@Composable
internal fun MedicationMessages(
    presenter: MedicationEventPresenter,
    behaviourMapper: MedicationMessageBehaviourUiMapper,
    reduceMotion: Boolean,
    onUndoArchive: (medicationId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var shown by rememberSaveable { mutableStateOf<ShownMedicationMessageUiModel?>(null) }
    val haptics = LocalHapticFeedback.current
    val accessibilityManager = LocalAccessibilityManager.current
    val currentOnUndoArchive by rememberUpdatedState(onUndoArchive)

    fun timeoutMillis(behaviour: MedicationMessageBehaviourUiModel): Long? =
        behaviour.baseDurationMillis?.let { base ->
            accessibilityManager?.calculateRecommendedTimeoutMillis(
                originalTimeoutMillis = base,
                containsIcons = true,
                containsText = true,
                containsControls = behaviour.undoesArchive
            ) ?: base
        }

    LaunchedEffect(presenter) {
        presenter.events.collect { event ->
            if (event !is MedicationEventUiModel.Message) return@collect
            if (behaviourMapper.toUi(event.message).tone == SnackbarTone.Error) {
                haptics.performHapticFeedback(HapticFeedbackType.Reject)
            }
            shown = ShownMedicationMessageUiModel(event.message, System.currentTimeMillis())
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
        val archived = visible.message as? MedicationMessageUiModel.Archived
        MigrelySnackbar(
            text = messageText(visible.message),
            tone = behaviour.tone,
            action = archived?.let {
                SnackbarAction(
                    label = stringResource(R.string.snackbar_undo),
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
            dismissLabel = stringResource(R.string.snackbar_dismiss),
            onDismiss = { if (shown == visible) shown = null },
            timeLeft = null
        )
    }
}

@Composable
private fun messageText(message: MedicationMessageUiModel): String = when (message) {
    is MedicationMessageUiModel.Archived -> stringResource(R.string.message_archived)
    MedicationMessageUiModel.Undone -> stringResource(R.string.message_undone)
    MedicationMessageUiModel.IntakeUndone -> stringResource(R.string.message_intake_undone)
    MedicationMessageUiModel.InFuture -> stringResource(R.string.message_in_future)
    MedicationMessageUiModel.LimitReached -> stringResource(R.string.message_limit_reached)
    MedicationMessageUiModel.StorageFull -> stringResource(R.string.message_storage_full)
    MedicationMessageUiModel.SomethingWentWrong -> stringResource(R.string.message_generic)
}
