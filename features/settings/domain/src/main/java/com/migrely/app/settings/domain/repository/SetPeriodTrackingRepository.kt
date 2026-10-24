package com.migrely.app.settings.domain.repository

import com.migrely.app.settings.domain.model.SettingsWriteResultDomainModel

interface SetPeriodTrackingRepository {
    fun setPeriodTracking(on: Boolean): SettingsWriteResultDomainModel
}
