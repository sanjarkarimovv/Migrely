package com.migrely.app.attack.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** One-off messages shown in the details screen's snackbar; parcelable so they survive rotation. */
sealed interface AttackMessageUiModel : Parcelable {
    @Parcelize
    data class PointsRemovedByTimeChange(val undo: TimesUndoUiModel) : AttackMessageUiModel

    @Parcelize
    data class PointRemoved(val attackId: String, val point: IntensityPointUiModel) :
        AttackMessageUiModel

    /** A medication removed from the attack, offered for "Undo" (BRD D34). */
    @Parcelize
    data class IntakeRemoved(val attackId: String, val intake: AttackIntakeUiModel) :
        AttackMessageUiModel

    @Parcelize
    data object PointOutsideAttack : AttackMessageUiModel

    @Parcelize
    data object TagLimitReached : AttackMessageUiModel

    @Parcelize
    data object AttacksMerged : AttackMessageUiModel

    @Parcelize
    data object Undone : AttackMessageUiModel

    @Parcelize
    data object UndoNotPossible : AttackMessageUiModel

    @Parcelize
    data object StorageFull : AttackMessageUiModel

    @Parcelize
    data object SomethingWentWrong : AttackMessageUiModel
}
