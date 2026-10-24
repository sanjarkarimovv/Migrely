package com.migrely.app.settings.domain.usecase

import com.migrely.app.architecture.domain.usecase.ContinuousExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.settings.domain.model.ThemeDomainModel
import com.migrely.app.settings.domain.repository.GetThemeRepository
import kotlinx.coroutines.flow.Flow

/**
 * The app's theme, then each change (BRD D94). The app reads the first one behind the splash, so no frame shows in the
 * other theme, and follows the rest while it shows, in a scope it cancels.
 */
class GetThemeUseCase(
    private val getThemeRepository: GetThemeRepository,
    coroutineContextProvider: CoroutineContextProvider
) : ContinuousExecutingUseCase<Unit, ThemeDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): Flow<ThemeDomainModel> =
        getThemeRepository.theme()
}
