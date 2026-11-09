package com.migrely.app.ui.main

/** What a tap on the doctor visit reminder does. */
enum class VisitTapAction {
    /** Attack mode, or what was opened from it, stays as it was; an unfinished form is kept. */
    BringToFront,

    /** Home, then History, which opens the report through its own premium gate (design D6). */
    OpenReportThroughHistory
}

/**
 * During an attack the tap opens neither the report nor the paywall (BRD EC-REM-12, EC-UX-12). The notification's
 * intent already brings the app to the front, as the other notifications' taps do, so nothing more happens.
 */
class VisitTapRule {
    fun actionFor(attackInProgress: Boolean): VisitTapAction = if (attackInProgress) {
        VisitTapAction.BringToFront
    } else {
        VisitTapAction.OpenReportThroughHistory
    }
}
