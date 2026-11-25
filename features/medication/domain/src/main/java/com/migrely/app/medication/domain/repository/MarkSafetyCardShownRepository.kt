package com.migrely.app.medication.domain.repository

/**
 * Keeps the moment the medication overuse card (BRD MED-F5, EC-MED-06) appeared, so the app asks for no rating in the 3 days after it (BRD D188, EC-REV-14).
 * A mark that cannot be written is dropped.
 */
interface MarkSafetyCardShownRepository {
    fun markSafetyCardShown()
}
