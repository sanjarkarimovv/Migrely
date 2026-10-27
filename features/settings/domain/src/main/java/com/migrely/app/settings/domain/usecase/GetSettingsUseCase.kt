package com.migrely.app.settings.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.settings.domain.model.SettingsDomainModel
import com.migrely.app.settings.domain.repository.GetSettingsRepository

class GetSettingsUseCase(
    private val getSettingsRepository: GetSettingsRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, SettingsDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): SettingsDomainModel =
        getSettingsRepository.settings()
}
