package com.migrely.app.medication.domain.repository

interface GetEffectReminderEnabledRepository {
    /** Whether "Ask if a medication helped" is on (BRD D67); false when the setting cannot be read. */
    fun effectReminderEnabled(): Boolean
}
