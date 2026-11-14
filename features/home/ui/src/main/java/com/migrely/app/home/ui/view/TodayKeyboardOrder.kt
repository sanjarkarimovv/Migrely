package com.migrely.app.home.ui.view

import androidx.compose.foundation.focusGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged

/**
 * Home Today's keyboard order (BRD D178 (4), ux F10, revised Q2): Settings, then the message's control, then the rest
 * of the content. The device starts the first Tab at the topmost control, which is Settings while the column is not
 * scrolled; once it has scrolled off, the message is the topmost and comes first. One local link on this one screen:
 * [settings] goes on to the message, the message on to the first control after Settings ([afterSettings]), and back.
 * The message is placed last for Tab ([MessageLayout]'s `keyboardMessageLast`), so a Tab past the end wraps to
 * [content]'s start, Settings, not to the message. TalkBack and Switch Access keep reading the message first.
 *
 * Every redirect checks the direction it is told, Next or Previous, so focus that comes back from the share sheet, the
 * review sheet or Android Settings is never moved.
 */
internal class TodayKeyboardOrder {
    private val settingsRequester = FocusRequester()
    private val afterSettingsRequester = FocusRequester()
    private val messageRequester = FocusRequester()
    private var settingsFocused = false

    /** Whether a message shows; set by the screen. */
    var messageShows by mutableStateOf(false)

    /** On the Settings button. */
    val settings: Modifier = Modifier
        .focusRequester(settingsRequester)
        .onFocusChanged { settingsFocused = it.isFocused }
        .focusProperties { if (messageShows) next = messageRequester }

    /** Around everything in Settings' column that follows Settings. */
    val afterSettings: Modifier = Modifier
        .focusRequester(afterSettingsRequester)
        .focusProperties {
            onExit = {
                if (requestedFocusDirection == FocusDirection.Previous && messageShows) {
                    messageRequester.requestFocus()
                }
            }
        }
        .focusGroup()

    /** Around the message. */
    val message: Modifier = Modifier
        .focusRequester(messageRequester)
        .focusProperties {
            onExit = {
                when (requestedFocusDirection) {
                    FocusDirection.Next -> afterSettingsRequester.requestFocus()
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
                if (requestedFocusDirection == FocusDirection.Next && messageShows &&
                    !settingsFocused
                ) {
                    settingsRequester.requestFocus()
                }
            }
        }
        .focusGroup()
}
