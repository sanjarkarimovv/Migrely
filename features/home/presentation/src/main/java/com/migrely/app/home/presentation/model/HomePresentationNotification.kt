package com.migrely.app.home.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification
import java.time.Instant

sealed interface HomePresentationNotification : PresentationNotification {
    data class AttackStarted(val attackId: String) : HomePresentationNotification

    data class AttackEnded(val attackId: String) : HomePresentationNotification

    data object StorageFull : HomePresentationNotification

    data class StartMoveRejected(val previousEnd: Instant) : HomePresentationNotification

    data object EndTimeOutOfRange : HomePresentationNotification

    data object Undone : HomePresentationNotification

    data object UndoNotPossible : HomePresentationNotification

    /** An attack was deleted on its details screen at [deletedAt] and can still be restored (BRD D23). */
    data class AttackDeleted(val attackId: String, val deletedAt: Instant) :
        HomePresentationNotification

    /**
     * A write saved and has no message of its own. Not a message: the screen uses it to replace an error that the
     * write has made untrue (BRD D178 (1), EC-UX-35).
     */
    data object Saved : HomePresentationNotification

    data object SomethingWentWrong : HomePresentationNotification
}
