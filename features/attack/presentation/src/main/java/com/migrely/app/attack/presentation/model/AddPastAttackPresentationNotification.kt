package com.migrely.app.attack.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification

sealed interface AddPastAttackPresentationNotification : PresentationNotification {
    /** Shown inline under [field] (BRD EC-ATK-03). */
    data class InFuture(val field: TimeFieldPresentationModel) :
        AddPastAttackPresentationNotification

    /** Shown inline under the end (BRD EC-ATK-04). */
    data object EndBeforeStart : AddPastAttackPresentationNotification

    /** Nothing was stored; the card names [attacks], oldest first (BRD EC-ATK-05, EC-ATK-15). */
    data class Overlaps(val attacks: List<AttackRangePresentationModel>) :
        AddPastAttackPresentationNotification

    data object StorageFull : AddPastAttackPresentationNotification

    data object SomethingWentWrong : AddPastAttackPresentationNotification
}
