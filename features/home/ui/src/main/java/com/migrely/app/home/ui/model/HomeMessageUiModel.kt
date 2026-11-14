package com.migrely.app.home.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** One-off messages shown in a snackbar on the home screen. Parcelable, so a message on screen survives rotation. */
sealed interface HomeMessageUiModel : Parcelable {
    @Parcelize
    data class AttackStarted(val attackId: String) : HomeMessageUiModel

    @Parcelize
    data class AttackEnded(val attackId: String) : HomeMessageUiModel

    /** Deleted on the details screen at [deletedAtEpochMillis]; offered once for undo (BRD D23). */
    @Parcelize
    data class AttackDeleted(val attackId: String, val deletedAtEpochMillis: Long) :
        HomeMessageUiModel

    @Parcelize
    data object Undone : HomeMessageUiModel

    @Parcelize
    data object UndoNotPossible : HomeMessageUiModel

    /** A write saved; shown only in place of an error, which it made untrue (BRD D178 (1)). */
    @Parcelize
    data object Saved : HomeMessageUiModel

    @Parcelize
    data object StorageFull : HomeMessageUiModel

    @Parcelize
    data class StartMoveRejected(val previousEndEpochMillis: Long) : HomeMessageUiModel

    @Parcelize
    data object EndTimeOutOfRange : HomeMessageUiModel

    @Parcelize
    data object SomethingWentWrong : HomeMessageUiModel
}
