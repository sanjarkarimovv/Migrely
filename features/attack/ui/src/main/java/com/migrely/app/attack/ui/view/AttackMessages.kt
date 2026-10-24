package com.migrely.app.attack.ui.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.migrely.app.attack.ui.R
import com.migrely.app.attack.ui.mapper.AttackMessageBehaviourUiMapper
import com.migrely.app.attack.ui.model.AttackMessageBehaviourUiModel
import com.migrely.app.attack.ui.model.AttackMessageUiModel
import com.migrely.app.attack.ui.model.ShownAttackMessageUiModel
import com.migrely.app.attack.ui.model.TimesUndoUiModel
import com.migrely.app.designsystem.snackbar.MigrelySnackbar
import com.migrely.app.designsystem.snackbar.MigrelySnackbarHost
import com.migrely.app.designsystem.snackbar.SnackbarAction
import kotlinx.coroutines.delay

/**
 * The details screen's messages, following the home screen's pattern (profile section 10): the newest message replaces
 * the one on screen, it keeps its remaining time across rotation, and its duration stretches to the accessibility
 * timeout (BRD EC-UX-16).
 */
@Suppress("LongParameterList")
@Composable
internal fun AttackMessages(
    shown: ShownAttackMessageUiModel?,
    behaviourMapper: AttackMessageBehaviourUiMapper,
    reduceMotion: Boolean,
    isShown: (ShownAttackMessageUiModel) -> Boolean,
    onClear: (ShownAttackMessageUiModel) -> Unit,
    onUndoTimes: (TimesUndoUiModel) -> Unit,
    onUndoPoint: (AttackMessageUiModel.PointRemoved) -> Unit,
    modifier: Modifier = Modifier
) {
    val accessibilityManager = LocalAccessibilityManager.current
    val currentOnClear by rememberUpdatedState(onClear)
    // Undo acts only while its message is still on screen, read from the state itself at tap time, so two taps in
    // one frame or a tap on a message fading out cannot undo twice.
    val currentIsShown by rememberUpdatedState(isShown)

    fun timeoutMillis(behaviour: AttackMessageBehaviourUiModel): Long? =
        behaviour.baseDurationMillis?.let { base ->
            accessibilityManager?.calculateRecommendedTimeoutMillis(
                originalTimeoutMillis = base,
                containsIcons = true,
                containsText = true,
                containsControls = behaviour.hasUndo
            ) ?: base
        }

    val timeout = shown?.let { timeoutMillis(behaviourMapper.toUi(it.message)) }
    LaunchedEffect(shown, timeout) {
        if (shown != null && timeout != null) {
            delay(shown.remainingMillis(System.currentTimeMillis(), timeout))
            currentOnClear(shown)
        }
    }

    MigrelySnackbarHost(
        message = shown,
        reduceMotion = reduceMotion,
        modifier = modifier
    ) { visible ->
        val behaviour = behaviourMapper.toUi(visible.message)
        val visibleTimeout = timeoutMillis(behaviour)
        val message = visible.message
        val action = when (message) {
            is AttackMessageUiModel.PointsRemovedByTimeChange -> SnackbarAction(
                label = stringResource(R.string.snackbar_undo),
                spokenLabel = stringResource(R.string.snackbar_undo_times_description),
                onClick = {
                    if (currentIsShown(visible)) {
                        currentOnClear(visible)
                        onUndoTimes(message.undo)
                    }
                }
            )

            is AttackMessageUiModel.PointRemoved -> SnackbarAction(
                label = stringResource(R.string.snackbar_undo),
                spokenLabel = stringResource(R.string.snackbar_undo_point_description),
                onClick = {
                    if (currentIsShown(visible)) {
                        currentOnClear(visible)
                        onUndoPoint(message)
                    }
                }
            )

            else -> null
        }
        MigrelySnackbar(
            text = messageText(message),
            tone = behaviour.tone,
            action = action,
            dismissLabel = stringResource(R.string.snackbar_dismiss),
            onDismiss = { currentOnClear(visible) },
            timeLeft = if (!reduceMotion && behaviour.hasUndo && visibleTimeout != null) {
                rememberTimeLeft(visible, visibleTimeout)
            } else {
                null
            }
        )
    }
}

@Composable
private fun rememberTimeLeft(shown: ShownAttackMessageUiModel, timeoutMillis: Long): () -> Float {
    val remaining = remember(shown, timeoutMillis) {
        shown.remainingMillis(System.currentTimeMillis(), timeoutMillis)
    }
    val fraction =
        remember(shown, timeoutMillis) { Animatable(remaining.toFloat() / timeoutMillis) }
    LaunchedEffect(fraction) {
        fraction.animateTo(0f, tween(durationMillis = remaining.toInt(), easing = LinearEasing))
    }
    return { fraction.value }
}

@Composable
private fun messageText(message: AttackMessageUiModel): String = when (message) {
    is AttackMessageUiModel.PointsRemovedByTimeChange -> pluralStringResource(
        R.plurals.message_points_removed,
        message.undo.removedPoints.size,
        message.undo.removedPoints.size
    )

    is AttackMessageUiModel.PointRemoved -> stringResource(R.string.message_point_removed)
    AttackMessageUiModel.PointOutsideAttack -> stringResource(R.string.message_point_outside)
    AttackMessageUiModel.TagLimitReached -> stringResource(R.string.message_tag_limit)
    AttackMessageUiModel.AttacksMerged -> stringResource(R.string.message_attacks_merged)
    AttackMessageUiModel.Undone -> stringResource(R.string.message_undone)
    AttackMessageUiModel.UndoNotPossible -> stringResource(R.string.message_undo_not_possible)
    AttackMessageUiModel.StorageFull -> stringResource(R.string.message_storage_full)
    AttackMessageUiModel.SomethingWentWrong -> stringResource(R.string.message_generic)
}
