package com.migrely.app.settings.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.settings.domain.model.SettingsWriteResultDomainModel
import com.migrely.app.settings.domain.model.ThemeDomainModel
import com.migrely.app.settings.domain.repository.SetThemeRepository

/** Chooses the app's theme (BRD SET-F1, D94); every screen follows at once through [GetThemeUseCase]. */
class SetThemeUseCase(
    private val setThemeRepository: SetThemeRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<ThemeDomainModel, SettingsWriteResultDomainModel>(
    coroutineContextProvider
) {
    override fun executeInBackground(request: ThemeDomainModel): SettingsWriteResultDomainModel =
        setThemeRepository.setTheme(request)
}
