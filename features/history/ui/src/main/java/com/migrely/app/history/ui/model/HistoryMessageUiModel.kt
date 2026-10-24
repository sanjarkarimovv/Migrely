package com.migrely.app.history.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** One-off messages shown in a snackbar on History. Parcelable, so a message on screen survives rotation. */
sealed interface HistoryMessageUiModel : Parcelable {
    /** Deleted on the details screen at [deletedAtEpochMillis]; offered once for undo (BRD EC-HIS-04, D31). */
    @Parcelize
    data class AttackDeleted(val attackId: String, val deletedAtEpochMillis: Long) :
        HistoryMessageUiModel

    @Parcelize
    data object Undone : HistoryMessageUiModel

    @Parcelize
    data object UndoNotPossible : HistoryMessageUiModel

    @Parcelize
    data object SomethingWentWrong : HistoryMessageUiModel
}
