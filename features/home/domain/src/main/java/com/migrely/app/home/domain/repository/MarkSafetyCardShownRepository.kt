package com.migrely.app.home.domain.repository

/**
 * Keeps the moment "Still going?" (BRD SAF-F3, EC-ATK-02) appeared, so the app asks for no rating in the 3 days after it (BRD D188, EC-REV-14).
 * A mark that cannot be written is dropped.
 */
interface MarkSafetyCardShownRepository {
    fun markSafetyCardShown()
}
