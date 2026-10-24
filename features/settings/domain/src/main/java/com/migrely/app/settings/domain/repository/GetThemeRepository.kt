package com.migrely.app.settings.domain.repository

import com.migrely.app.settings.domain.model.ThemeDomainModel
import kotlinx.coroutines.flow.Flow

interface GetThemeRepository {
    /** The theme now, then again whenever it changes. Never fails. */
    fun theme(): Flow<ThemeDomainModel>
}
