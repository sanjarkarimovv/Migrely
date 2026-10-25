package com.migrely.app.settings.domain.repository

import com.migrely.app.settings.domain.model.SettingsWriteResultDomainModel

/** Forgets every setting, onboarding's answers and the theme included. */
interface DeletePreferencesRepository {
    fun deletePreferences(): SettingsWriteResultDomainModel
}
