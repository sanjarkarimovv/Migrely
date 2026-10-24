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

    data object StorageFull : HistoryPresentationNotification

    data object SomethingWentWrong : HistoryPresentationNotification
}
