package com.migrely.app.history.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification
import java.time.Instant

sealed interface HistoryPresentationNotification : PresentationNotification {
    /** An attack was deleted on its details screen at [deletedAt] and can still be restored (BRD EC-HIS-04). */
    data class AttackDeleted(val attackId: String, val deletedAt: Instant) :
        HistoryPresentationNotification

    data object Undone : HistoryPresentationNotification

    data object UndoNotPossible : HistoryPresentationNotification

    data object SomethingWentWrong : HistoryPresentationNotification
}
