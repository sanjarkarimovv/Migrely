package com.migrely.app.attack.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification

sealed interface AttackDetailsPresentationNotification : PresentationNotification {
    /** Shown inline under the time the user changed (BRD EC-ATK-03, EC-ATK-04). */
    data class TimeRejected(
        val field: TimeFieldPresentationModel,
        val reason: TimeRejectionPresentationModel
    ) : AttackDetailsPresentationNotification

    data class PointRejected(val reason: PointRejectionPresentationModel) :
        AttackDetailsPresentationNotification

    data class PointsRemovedByTimeChange(val undo: TimesUndoPresentationModel) :
        AttackDetailsPresentationNotification

    data class PointRemoved(val attackId: String, val point: IntensityPointPresentationModel) :
        AttackDetailsPresentationNotification

    /** The first 10 the user ever recorded (BRD SAF-F2). */
    data object FirstMaximumRecorded : AttackDetailsPresentationNotification

    data object TagLimitReached : AttackDetailsPresentationNotification

    /** Overlapping attacks are now this one (BRD D27). */
    data object AttacksMerged : AttackDetailsPresentationNotification

    data object Undone : AttackDetailsPresentationNotification

    data object UndoNotPossible : AttackDetailsPresentationNotification

    data object StorageFull : AttackDetailsPresentationNotification

    data object SomethingWentWrong : AttackDetailsPresentationNotification
}
