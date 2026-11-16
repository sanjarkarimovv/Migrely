package com.migrely.app.home.ui.view

import androidx.compose.foundation.focusGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester

/**
 * Home Today's keyboard order (BRD D178 (4), ux F10, revised Q2): Settings, then the message's control, then the rest
 * of the content. The device usually starts the first Tab at Settings while the column is not scrolled (lead decision
 * on O12 for the scrolled case). One local link on this one screen: [settings] goes on to the message; Tab from the
 * message runs Tab order itself with Settings briefly not a stop, so it lands on the first control after Settings in
 * Tab order, never one chosen by place (review 9.3 F1); Shift+Tab from the first control after Settings in Tab order
 * goes back to the message, whichever control that is (BRD D181 (4): [settings] turns a Previous search that reaches
 * it), and from the message to Settings. The message is placed last for Tab ([MessageLayout]'s
 * `keyboardMessageLast`), so a Tab past the end wraps to [content]'s start, Settings, not to the message. TalkBack and
 * Switch Access keep reading the message first.
 *
 * Every redirect checks the direction it is told, Next or Previous, so focus that comes back from the share sheet, the
 * review sheet or Android Settings is never moved.
 */
internal class TodayKeyboardOrder(private val focusManager: FocusManager) {
    private val settingsRequester = FocusRequester()
    private val messageRequester = FocusRequester()
    private val lastRequester = FocusRequester()

    // True only while a Tab moves on from the message: Settings is then not a stop, so Tab order itself finds the first
    // control after Settings (a request on the group would pick it by place instead, review 9.3 F1).
    private var passingSettings = false

    /** Whether a message shows; set by the screen. */
    var messageShows by mutableStateOf(false)

    /**
     * On the Settings button. Its own group turns a Shift+Tab that would land on Settings to the message instead
     * (BRD D181 (4)): only the first control after Settings in Tab order searches back into Settings, whichever control
     * that is (a slot, Period, or "Migraine started" when nothing else follows Settings). The message's own way back
     * to Settings is a request, not a Previous search, so it is not turned.
     */
    val settings: Modifier = Modifier
        .focusProperties {
            onEnter = {
                if (requestedFocusDirection == FocusDirection.Previous && messageShows) {
                    messageRequester.requestFocus()
                }
            }
        }
        .focusGroup()
        .focusRequester(settingsRequester)
        .focusProperties {
            canFocus = !passingSettings
            if (messageShows) {
                next = messageRequester
                // Back past the start wraps to the end, as without a message; the message comes after Settings only.
                previous = lastRequester
            }
        }

    /** On the content's last control, where Shift+Tab from Settings wraps to. */
    val last: Modifier = Modifier.focusRequester(lastRequester)

    /** Around the message. */
    val message: Modifier = Modifier
        .focusRequester(messageRequester)
        .focusProperties {
            onExit = {
                when (requestedFocusDirection) {
                    FocusDirection.Next -> {
                        passingSettings = true
                        focusManager.moveFocus(FocusDirection.Next)
                        passingSettings = false
                    }
                    FocusDirection.Previous -> settingsRequester.requestFocus()
                    else -> Unit
                }
            }
        }
        .focusGroup()

    /** Around Today's content: a Tab past its end goes back to Settings, not on to the message. */
    val content: Modifier = Modifier
        .focusProperties {
            onExit = {
                // Runs for every Next that leaves the content while a message shows: a Tab past the last control,
                // which this turns back to Settings (review 9.3 F2), and also Settings' own `next` link to the
                // message. For the link, the request asks for Settings, which already has focus, so it changes nothing
                // and focus goes on to the message (D181 (4), checked with a probe on 2026-11-15; the order tests
                // fail if it ever does change something).
                if (requestedFocusDirection == FocusDirection.Next && messageShows) {
                    settingsRequester.requestFocus()
                }
            }
        }
        .focusGroup()
}
