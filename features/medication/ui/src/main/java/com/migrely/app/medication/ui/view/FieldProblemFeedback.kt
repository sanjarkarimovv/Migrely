package com.migrely.app.medication.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.layout.wordsShownOnly
import kotlinx.coroutines.delay

/**
 * How long an announcement stays in the accessibility tree. While there it is a TalkBack stop that repeats the field's
 * error (ux F6), so as short as TalkBack allows: TalkBack takes the text when the node appears, and on emulator-5570
 * 500, 1000 and 3000 ms were all spoken alike; 1000 ms keeps a margin for a busy phone (BRD D191, cr-239 F2).
 */
private const val ANNOUNCEMENT_MILLIS = 1_000L

/**
 * A text field that a Save problem can point at (BRD D191, ux F2, F3): put [modifier] on the field and pass
 * [inViewRequests] to its `SingleLineFieldInView`.
 */
@Stable
internal class ProblemField {
    private val focusRequester = FocusRequester()

    var focused by mutableStateOf(false)
        private set

    var inViewRequests by mutableIntStateOf(0)
        private set

    val modifier: Modifier =
        Modifier
            .focusRequester(focusRequester)
            .onFocusChanged { focused = it.isFocused }

    fun requestFocus() = focusRequester.requestFocus()

    fun bringIntoView() {
        inViewRequests++
    }
}

/** The field's error, read with it as one stop, instead of Material's default "Invalid input" (ux F6). */
internal fun Modifier.problemSemantics(problem: String?): Modifier =
    if (problem == null) this else semantics { error(problem) }

/**
 * A field's supporting line: its [problem], or else its [counter]. The problem is read once, as the field's error
 * ("Name, edit box, error: Enter a name."), so its visible line is not read a second time (ux F6).
 */

@Composable
internal fun FieldSupportingLine(problem: String?, counter: String) {
    if (problem != null) {
        Text(problem, Modifier.clearAndSetSemantics { })
    } else {
        Text(counter)
    }
}

/**
 * Says a field problem once, politely (ux F5): from its own node, so the field never becomes a live region and typing
 * stays quiet (F8). Each [announce] says it again, the same text included (F7); [clear] takes back one not yet gone.
 */
@Stable
internal class ProblemAnnouncer {
    internal var shown by mutableStateOf<String?>(null)
    internal var requests by mutableIntStateOf(0)
        private set
    internal var requested: String? = null
        private set

    fun announce(text: String) {
        requested = text
        requests++
    }

    fun clear() {
        if (requested == null && shown == null) return
        requested = null
        requests++
    }
}

@Composable
internal fun rememberProblemAnnouncer(): ProblemAnnouncer = remember { ProblemAnnouncer() }

/**
 * The node [announcer] speaks from: a point at the page's top, so it is on screen whatever the scroll, and above the
 * form in reading order. It is only there while it speaks.
 */
@Composable
internal fun ProblemAnnouncement(announcer: ProblemAnnouncer, modifier: Modifier = Modifier) {
    LaunchedEffect(announcer.requests) {
        announcer.shown = null
        val text = announcer.requested ?: return@LaunchedEffect
        // Gone for a frame first, so the same text arrives as a new announcement.
        withFrameNanos { }
        announcer.shown = text
        delay(ANNOUNCEMENT_MILLIS)
        announcer.shown = null
    }
    val text = announcer.shown ?: return
    Box(
        modifier
            .size(1.dp)
            .clearAndSetSemantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = text
            }
    )
}

/**
 * What a medication form does once per result of Save or "Add and log" that found [problem] in [field] (BRD D191,
 * EC-UX-49, EC-UX-66 to EC-UX-71; ux F2 to F5, F7). Keyed on [arrivals], the count of such results: a second Save
 * with the same problem changes no value, and its result can come in the press's own frame, so only a count shows it
 * is new. Never on the press and never while [pending].
 *
 * - With a hardware keyboard, focus moves to the field (D181 (6)); the field reads its own error.
 * - By touch with the soft keyboard up in another field, focus moves to the field, cursor at the end, and the keyboard
 *   stays up; the field reads its own error.
 * - Otherwise focus stays where it is (a touch focus would open the keyboard over the error): the field comes into
 *   view and the problem is announced once.
 *
 * [onFocusMoving] puts the field's cursor at the end before it takes focus (F3).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FieldProblemFeedback(
    arrivals: Int,
    pending: Boolean,
    field: ProblemField?,
    problem: String?,
    announcer: ProblemAnnouncer,
    onFocusMoving: () -> Unit
) {
    val inputModeManager = LocalInputModeManager.current
    // The insets object, read at the result: the keyboard's frames do not recompose the form. Where the keyboard is
    // going, not where it is: a Save tapped while it closes must not move focus and open it again (ux F4). Its height,
    // not its visibility, which reads "visible" before a window has had any insets.
    val keyboard = WindowInsets.imeAnimationTarget
    val density = LocalDensity.current
    LaunchedEffect(arrivals) {
        if (arrivals == 0 || pending || field == null || problem == null) return@LaunchedEffect
        val keyboardMode = inputModeManager.inputMode == InputMode.Keyboard
        val keyboardUp = keyboard.getBottom(density) > 0
        if (keyboardMode || keyboardUp && !field.focused) {
            onFocusMoving()
            field.requestFocus()
        } else {
            field.bringIntoView()
            announcer.announce(problem)
        }
    }
    // Typing clears the problem; an announcement not yet gone goes with it (F8).
    LaunchedEffect(problem == null) {
        if (problem == null) announcer.clear()
    }
    // The form can leave while its announcement runs, as when a dose is then logged: the announcement goes with it.
    DisposableEffect(announcer) {
        onDispose { announcer.clear() }
    }
}
