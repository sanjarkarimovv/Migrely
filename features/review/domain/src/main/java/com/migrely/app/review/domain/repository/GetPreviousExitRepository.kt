package com.migrely.app.review.domain.repository

/**
 * Whether the app's previous run ended with a crash or an ANR (BRD D130). False when Android does not say, as before
 * Android 11.
 */
interface GetPreviousExitRepository {
    fun previousExitCrashed(): Boolean
}
