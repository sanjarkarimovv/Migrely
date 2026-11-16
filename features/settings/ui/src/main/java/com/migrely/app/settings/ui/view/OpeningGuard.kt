package com.migrely.app.settings.ui.view

/**
 * One opening per tap for "Share Migrely" and "Rate Migrely on Google Play" together (BRD EC-SHR-05, design D1). After
 * an accepted activation, both rows ignore activation until Settings is resumed after leaving or, if it never leaves
 * (split screen, nothing could open), until [timeoutMillis], Android's double-tap timeout, has passed. It sees every
 * activation the rows get: touch, TalkBack, Switch Access and the keyboard. [now] is a monotonic clock in
 * milliseconds.
 */
class OpeningGuard(private val timeoutMillis: Long, private val now: () -> Long) {
    private var acceptedAt: Long? = null
    private var left = false

    /** Whether this activation may open something; if so, the next ones wait. */
    fun tryOpen(): Boolean {
        val accepted = acceptedAt
        val time = now()
        if (accepted != null && (left || time - accepted < timeoutMillis)) return false
        acceptedAt = time
        left = false
        return true
    }

    /** Settings paused: the share sheet or Google Play came up. */
    fun onPause() {
        if (acceptedAt != null) left = true
    }

    /** Settings is back after leaving: the rows answer again. */
    fun onResume() {
        if (left) {
            acceptedAt = null
            left = false
        }
    }
}
