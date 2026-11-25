package com.migrely.app.designsystem.snackbar

import android.os.SystemClock
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The two clocks a message's countdown reads (BRD D182). [wallMillis] is the clock that stamps a deletion and that the
 * purge and [isFreshDeletion] use. [elapsedRealtimeMillis] counts deep sleep and cannot be set by the user, so a time
 * shown runs on it.
 */
interface MessageClock {
    fun wallMillis(): Long

    fun elapsedRealtimeMillis(): Long
}

/** The phone's clocks. */
object SystemMessageClock : MessageClock {
    override fun wallMillis(): Long = System.currentTimeMillis()

    override fun elapsedRealtimeMillis(): Long = SystemClock.elapsedRealtime()
}

/** The clocks of the messages below; the phone's unless a test provides its own. */
val LocalMessageClock = staticCompositionLocalOf<MessageClock> { SystemMessageClock }
