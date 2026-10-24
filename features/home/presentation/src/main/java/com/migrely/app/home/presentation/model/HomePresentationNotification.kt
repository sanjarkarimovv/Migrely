package com.migrely.app.home.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification
import java.time.Instant

sealed interface HomePresentationNotification : PresentationNotification {
    data class AttackStarted(val attackId: String) : HomePresentationNotification

    data class AttackEnded(val attackId: String) : HomePresentationNotification

    data object StorageFull : HomePresentationNotification

    data class StartMoveRejected(val previousEnd: Instant) : HomePresentationNotification

    data object EndTimeOutOfRange : HomePresentationNotification

    data object SomethingWentWrong : HomePresentationNotification
}
