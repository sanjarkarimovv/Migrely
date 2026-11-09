package com.migrely.app.reminders.domain.usecase

import java.time.Duration
import java.time.Instant

private val ASK_AGAIN_AFTER: Duration = Duration.ofDays(30)

/** Notification permission may be asked for again 30 days after it was last asked for (BRD EC-REM-02). */
internal object PermissionAskRule {
    fun mayAsk(askedAt: Instant?, now: Instant): Boolean =
        askedAt == null || Duration.between(askedAt, now) >= ASK_AGAIN_AFTER
}
