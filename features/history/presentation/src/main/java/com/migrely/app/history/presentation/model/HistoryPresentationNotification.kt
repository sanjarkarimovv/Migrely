package com.migrely.app.history.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification
import java.time.Instant

sealed interface HistoryPresentationNotification : PresentationNotification {
    /** An attack was deleted on its details screen at [deletedAt] and can still be restored (BRD EC-HIS-04). */
    data class AttackDeleted(val attackId: String, val deletedAt: Instant) :
        HistoryPresentationNotification

    data object Undone : HistoryPresentationNotification

    data object UndoNotPossible : HistoryPresentationNotification

    /** Only a fallback: the screen never offers a pain-free mark on a day with an attack (BRD EC-DAY-01). */
    data object AttackLoggedOnDay : HistoryPresentationNotification

    /**
     * A write saved and has no message of its own. Not a message: the screen uses it to replace an error that the
     * write has made untrue (BRD D178 (1), EC-UX-35).
     */
    data object Saved : HistoryPresentationNotification

    data object StorageFull : HistoryPresentationNotification

    data object SomethingWentWrong : HistoryPresentationNotification
}
