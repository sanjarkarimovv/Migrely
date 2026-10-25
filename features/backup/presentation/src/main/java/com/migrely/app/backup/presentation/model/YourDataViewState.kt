package com.migrely.app.backup.presentation.model

sealed interface YourDataViewState {
    /** The newest automatic copy, which can be restored, or null when there is none (BRD D98). */
    val automaticCopy: AutomaticCopyPresentationModel?

    data class Idle(override val automaticCopy: AutomaticCopyPresentationModel?) :
        YourDataViewState

    /** A file is being written or read; the actions wait until it is done. */
    data class Working(override val automaticCopy: AutomaticCopyPresentationModel?) :
        YourDataViewState
}
