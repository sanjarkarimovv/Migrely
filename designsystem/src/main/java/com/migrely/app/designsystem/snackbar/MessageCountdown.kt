package com.migrely.app.designsystem.snackbar

// Focus that leaves and comes back within this, as TalkBack's move from a message's text to its control, keeps the
// pause; the grace itself is not shown time.
private const val GRACE_MILLIS = 300L

// The least time a message has left when its pause ends, so a person can come back to it.
private const val RESUME_FLOOR_MILLIS = 3_000L

/** What can hold a message's countdown (BRD D182 (2), EC-UX-53…55). */
data class CountdownHold(
    val keyboardFocus: Boolean = false,
    val screenReaderFocus: Boolean = false,
    val screenReaderOn: Boolean = false,
    val windowFocused: Boolean = true
) {
    internal val holds: Boolean
        get() = windowFocused && (keyboardFocus || screenReaderOn && screenReaderFocus)
}

/**
 * Where an Undo's ceiling counts from (BRD D182 (3), EC-UX-58). It ends [UNDO_CEILING_MILLIS] after the earlier of the
 * action on the wall clock, the clock that stamps a deletion and purges it, and the showing on elapsedRealtime, which a
 * clock set back cannot stretch.
 */
data class UndoCeiling(val actionWallMillis: Long, val shownElapsedMillis: Long) {
    fun leftMillis(wallMillis: Long, elapsedMillis: Long): Long = minOf(
        actionWallMillis + UNDO_CEILING_MILLIS - wallMillis,
        shownElapsedMillis + UNDO_CEILING_MILLIS - elapsedMillis
    )
}

/**
 * A message's countdown as its host saves it (BRD EC-UX-57). [runMillis] is the running time already shown, with a
 * pause ended and the floor applied at the save. [actionWallMillis] is set for an Undo only.
 */
data class SavedMessageCountdown(
    val runMillis: Long,
    val savedElapsedMillis: Long,
    val shownElapsedMillis: Long,
    val actionWallMillis: Long?,
    val lineFraction: Float
)

/**
 * The time a message has left (BRD D182 (2), (3)). It runs on elapsedRealtime and stops while [CountdownHold] holds
 * the message. Once the hold ends it goes on after [GRACE_MILLIS], or at once when the window lost focus, with at least
 * [RESUME_FLOOR_MILLIS] left. An Undo never passes its [UndoCeiling], paused or not.
 *
 * The time-left line stops while paused, never grows, and runs from where it stopped to 0 at the real end, the earlier
 * of the timeout and the ceiling. A null timeout is a message that stays until it is closed.
 *
 * Plain state, read on the main thread: every call reads [clock] and first settles a grace that has run out.
 */
class MessageCountdown private constructor(
    private val clock: MessageClock,
    private var timeoutMillis: Long?,
    private val ceiling: UndoCeiling?,
    private val shownElapsedMillis: Long,
    private var runMillis: Long,
    lineFraction: Float
) {
    private var hold = CountdownHold()

    // Since when the time runs on elapsedRealtime; null while paused, a grace included.
    private var runningSince: Long?
    private var graceEnd: Long? = null
    private var line: TimeLeftLine

    init {
        val now = clock.elapsedRealtimeMillis()
        runningSince = now
        line = TimeLeftLine(lineFraction, now, endAt(now))
    }

    fun update(newHold: CountdownHold) {
        settle()
        val now = clock.elapsedRealtimeMillis()
        hold = newHold
        when {
            newHold.holds -> {
                graceEnd = null
                if (runningSince != null) pauseAt(now)
            }

            runningSince != null -> Unit
            !newHold.windowFocused -> {
                graceEnd = null
                resumeAt(now)
            }

            graceEnd == null -> graceEnd = now + GRACE_MILLIS
        }
    }

    /** A new "Time to take action" applies against the unpaused time already shown (EC-UX-52). */
    fun timeoutChanged(newTimeoutMillis: Long?) {
        settle()
        val now = clock.elapsedRealtimeMillis()
        val running = runningSince != null
        if (running) {
            runMillis = runAt(now)
            runningSince = now
        }
        timeoutMillis = newTimeoutMillis
        if (running) line = TimeLeftLine(line.fractionAt(now), now, endAt(now))
    }

    fun isPaused(): Boolean {
        settle()
        return runningSince == null
    }

    /** The time left to the real end, or null for a message with no end. */
    fun remainingMillis(): Long? {
        settle()
        return remainingAt(clock.elapsedRealtimeMillis())
    }

    fun isExpired(): Boolean = remainingMillis()?.let { it <= 0 } == true

    /**
     * How long until the countdown may change on its own: its end while running, the end of a grace, or the ceiling
     * while paused; null when nothing will change until the hold or the timeout does.
     */
    fun nextCheckInMillis(): Long? {
        settle()
        val now = clock.elapsedRealtimeMillis()
        val next = if (runningSince != null) {
            remainingAt(now)
        } else {
            listOfNotNull(
                graceEnd?.let { it - now },
                ceiling?.leftMillis(clock.wallMillis(), now)
            ).minOrNull()
        }
        return next?.coerceAtLeast(0L)
    }

    fun timeLeftFraction(): Float {
        settle()
        return line.fractionAt(clock.elapsedRealtimeMillis())
    }

    /** The state to save. It leaves this countdown as it is, since a save may come with no restore. */
    fun save(): SavedMessageCountdown {
        settle()
        val now = clock.elapsedRealtimeMillis()
        return SavedMessageCountdown(
            runMillis = if (runningSince == null) flooredRun() else runAt(now),
            savedElapsedMillis = now,
            shownElapsedMillis = shownElapsedMillis,
            actionWallMillis = ceiling?.actionWallMillis,
            lineFraction = line.fractionAt(now)
        )
    }

    private fun settle() {
        val end = graceEnd ?: return
        if (clock.elapsedRealtimeMillis() >= end) {
            graceEnd = null
            resumeAt(end)
        }
    }

    private fun pauseAt(elapsedMillis: Long) {
        runMillis = runAt(elapsedMillis)
        runningSince = null
        line = TimeLeftLine(line.fractionAt(elapsedMillis), elapsedMillis, endElapsedMillis = null)
    }

    private fun resumeAt(elapsedMillis: Long) {
        runMillis = flooredRun()
        runningSince = elapsedMillis
        line = TimeLeftLine(line.fractionAt(elapsedMillis), elapsedMillis, endAt(elapsedMillis))
    }

    private fun flooredRun(): Long =
        timeoutMillis?.let { minOf(runMillis, it - RESUME_FLOOR_MILLIS) } ?: runMillis

    private fun runAt(elapsedMillis: Long): Long =
        runMillis + (runningSince?.let { elapsedMillis - it } ?: 0L)

    // The wall clock read back to [elapsedMillis], which may lie a grace in the past.
    private fun remainingAt(elapsedMillis: Long): Long? {
        val wallMillis = clock.wallMillis() - (clock.elapsedRealtimeMillis() - elapsedMillis)
        return listOfNotNull(
            timeoutMillis?.let { it - runAt(elapsedMillis) },
            ceiling?.leftMillis(wallMillis, elapsedMillis)
        ).minOrNull()
    }

    private fun endAt(elapsedMillis: Long): Long? =
        remainingAt(elapsedMillis)?.let { elapsedMillis + it }

    companion object {
        /** A message shown now. [undoActionWallMillis] is the action an Undo undoes, null for no Undo. */
        fun start(
            clock: MessageClock,
            timeoutMillis: Long?,
            undoActionWallMillis: Long?
        ): MessageCountdown {
            val now = clock.elapsedRealtimeMillis()
            return MessageCountdown(
                clock = clock,
                timeoutMillis = timeoutMillis,
                ceiling = undoActionWallMillis?.let { UndoCeiling(it, now) },
                shownElapsedMillis = now,
                runMillis = 0L,
                lineFraction = 1f
            )
        }

        /**
         * The countdown after a rotation or process death, with the time since the save counted as running time and
         * no focus; null when nothing should show: its time or its ceiling has passed, or the phone restarted, which
         * the elapsed clock reading below the saved stamps shows.
         */
        fun restore(
            clock: MessageClock,
            timeoutMillis: Long?,
            saved: SavedMessageCountdown
        ): MessageCountdown? {
            val now = clock.elapsedRealtimeMillis()
            if (now < saved.savedElapsedMillis || now < saved.shownElapsedMillis) return null
            return MessageCountdown(
                clock = clock,
                timeoutMillis = timeoutMillis,
                ceiling = saved.actionWallMillis?.let { UndoCeiling(it, saved.shownElapsedMillis) },
                shownElapsedMillis = saved.shownElapsedMillis,
                runMillis = saved.runMillis + (now - saved.savedElapsedMillis),
                lineFraction = saved.lineFraction
            ).takeUnless { it.isExpired() }
        }
    }
}

/**
 * The time-left line from [fraction] at [startElapsedMillis] down to 0 at [endElapsedMillis]; it stays at [fraction]
 * while [endElapsedMillis] is null. It runs on elapsedRealtime only, so it never grows.
 */
private data class TimeLeftLine(
    val fraction: Float,
    val startElapsedMillis: Long,
    val endElapsedMillis: Long?
) {
    fun fractionAt(elapsedMillis: Long): Float {
        val end = endElapsedMillis ?: return fraction
        val span = end - startElapsedMillis
        val share = if (span <= 0L) 0f else (end - elapsedMillis).toFloat() / span
        return fraction * share.coerceIn(0f, 1f)
    }
}
