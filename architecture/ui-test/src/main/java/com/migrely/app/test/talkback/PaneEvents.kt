package com.migrely.app.test.talkback

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.core.view.accessibility.AccessibilityEventCompat
import org.robolectric.Shadows.shadowOf

/**
 * Every pane event the app's windows, dialogs included, have sent so far, in order, as an accessibility service such
 * as TalkBack receives them (#279): "appeared: <title>" or "disappeared: <title>". Robolectric keeps each event a
 * window hands to the accessibility manager. Use it on the main thread.
 */
fun ComponentActivity.paneEvents(): List<String> =
    shadowOf(getSystemService(AccessibilityManager::class.java)).sentAccessibilityEvents
        .filter { it.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED }
        .flatMap { event ->
            val title = event.text.joinToString(" ")
            val changes = event.contentChangeTypes
            listOfNotNull(
                "appeared: $title".takeIf { changes and PANE_APPEARED != 0 },
                "disappeared: $title".takeIf { changes and PANE_DISAPPEARED != 0 }
            )
        }

private const val PANE_APPEARED = AccessibilityEventCompat.CONTENT_CHANGE_TYPE_PANE_APPEARED
private const val PANE_DISAPPEARED = AccessibilityEventCompat.CONTENT_CHANGE_TYPE_PANE_DISAPPEARED
