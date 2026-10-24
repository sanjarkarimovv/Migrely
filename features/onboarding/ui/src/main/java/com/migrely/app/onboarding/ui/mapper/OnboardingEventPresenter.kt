package com.migrely.app.onboarding.ui.mapper

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Tells the screen an answer was not saved. */
class OnboardingEventPresenter {
    private val _notSaved = MutableSharedFlow<Unit>(extraBufferCapacity = EVENT_BUFFER)
    val notSaved: SharedFlow<Unit> = _notSaved.asSharedFlow()

    fun showNotSaved() {
        _notSaved.tryEmit(Unit)
    }

    private companion object {
        const val EVENT_BUFFER = 8
    }
}
