package com.migrely.app.reminders.ui.model

/** What the screen does in reply to the view model: ask the system for permission, or show a message. */
sealed interface RemindersEventUiModel {
    data class AskForPermission(val kind: ReminderKindUiModel) : RemindersEventUiModel

    data class Message(val message: RemindersMessageUiModel) : RemindersEventUiModel
}

sealed interface RemindersMessageUiModel {
    data object StorageFull : RemindersMessageUiModel

    data object SomethingWentWrong : RemindersMessageUiModel

    /** "Visit removed", whose Undo puts [visit] back (BRD EC-REM-10). */
    data class VisitRemoved(val visit: DoctorVisitUiModel) : RemindersMessageUiModel

    data object Undone : RemindersMessageUiModel

    /** A write saved; shown only in place of an error, which it made untrue (BRD D178 (1)). */
    data object Saved : RemindersMessageUiModel
}

/** A removed visit, held by its message for Undo. */
data class DoctorVisitUiModel(val id: String, val epochDay: Long)
