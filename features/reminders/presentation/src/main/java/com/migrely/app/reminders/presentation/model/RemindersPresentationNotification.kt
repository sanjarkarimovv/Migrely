package com.migrely.app.reminders.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification

sealed interface RemindersPresentationNotification : PresentationNotification {
    /** Ask the system for notification permission, for [kind] (BRD EC-REM-01). */
    data class AskForPermission(val kind: ReminderKindPresentationModel) :
        RemindersPresentationNotification

    /** "Visit removed" with Undo, which puts [visit] back (BRD EC-REM-10). */
    data class VisitRemoved(val visit: DoctorVisitPresentationModel) :
        RemindersPresentationNotification

    /** Undo put the visit back. */
    data object Undone : RemindersPresentationNotification

    /**
     * A write saved and has no message of its own. Not a message: the screen uses it to replace an error that the
     * write has made untrue (BRD D178 (1), EC-UX-35).
     */
    data object Saved : RemindersPresentationNotification

    data object StorageFull : RemindersPresentationNotification

    data object SomethingWentWrong : RemindersPresentationNotification
}
