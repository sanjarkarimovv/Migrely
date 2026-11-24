package com.migrely.app.attack.domain.repository

/**
 * Keeps the moment the first-10 safety card (BRD SAF-F2) appeared, so the app asks for no rating in the 3 days after it (BRD D188, EC-REV-14).
 * A mark that cannot be written is dropped.
 */
interface MarkSafetyCardShownRepository {
    fun markSafetyCardShown()
}
