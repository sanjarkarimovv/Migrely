package com.migrely.app.reminders.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** What the screen does in reply to the view model: ask the system for permission, or show a message. */
sealed interface RemindersEventUiModel {
    data class AskForPermission(val kind: ReminderKindUiModel) : RemindersEventUiModel

    data class Message(val message: RemindersMessageUiModel) : RemindersEventUiModel
}

/** A message on Reminders. Parcelable, so an error on screen survives rotation and a restored process (BRD D178 (2)). */
sealed interface RemindersMessageUiModel : Parcelable {
    @Parcelize
    data object StorageFull : RemindersMessageUiModel

    @Parcelize
    data object SomethingWentWrong : RemindersMessageUiModel

    /** "Visit removed", whose Undo puts [visit] back (BRD EC-REM-10). */
    @Parcelize
    data class VisitRemoved(val visit: DoctorVisitUiModel) : RemindersMessageUiModel

    @Parcelize
    data object Undone : RemindersMessageUiModel

    /** A write saved; shown only in place of an error, which it made untrue (BRD D178 (1)). */
    @Parcelize
    data object Saved : RemindersMessageUiModel
}

/** A removed visit, held by its message for Undo. */
@Parcelize
data class DoctorVisitUiModel(val id: String, val epochDay: Long) : Parcelable
