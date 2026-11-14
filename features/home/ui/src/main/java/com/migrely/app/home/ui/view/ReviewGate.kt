package com.migrely.app.home.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavController
import com.migrely.app.architecture.ui.view.ScreenLeaveObserver

/**
 * Whether Home may compose its review request (BRD D178 (3), EC-REV-13, ux F3): not while any message shows, and not
 * for the rest of a visit in which an error showed. A visit ends when Home stops being the current screen, read from
 * the back stack like the error's own clear (D178 (2)). Both are saved, so rotation and a restored process keep them.
 * When the slot leaves the composition, the review prompt drops its wait; when it comes back, its 2 s start again.
 */
internal class ReviewGate(
    private val messageShows: MutableState<Boolean>,
    private val errorThisVisit: MutableState<Boolean>
) {
    val open: Boolean get() = !messageShows.value && !errorThisVisit.value

    fun onShownChanged(shows: Boolean) {
        messageShows.value = shows
    }

    fun onErrorPosted() {
        errorThisVisit.value = true
    }
}

@Composable
internal fun rememberReviewGate(navController: NavController): ReviewGate {
    val messageShows = rememberSaveable { mutableStateOf(false) }
    val errorThisVisit = rememberSaveable { mutableStateOf(false) }
    ScreenLeaveObserver(navController) { errorThisVisit.value = false }
    return remember(messageShows, errorThisVisit) { ReviewGate(messageShows, errorThisVisit) }
}
