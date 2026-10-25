package com.migrely.app.settings.domain.usecase

import com.migrely.app.architecture.domain.usecase.ContinuousExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.settings.domain.model.AppearanceDomainModel
import com.migrely.app.settings.domain.repository.GetAttackInProgressRepository
import com.migrely.app.settings.domain.repository.GetThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * How the app looks, then each change (BRD D94): the chosen theme, and whether an attack is in progress, which makes
 * every screen dark until it ends. The app reads the first one behind the splash, so no frame shows in the other theme,
 * and follows the rest while it shows, in a scope it cancels.
 */
class GetAppearanceUseCase(
    private val getThemeRepository: GetThemeRepository,
    private val getAttackInProgressRepository: GetAttackInProgressRepository,
    coroutineContextProvider: CoroutineContextProvider
) : ContinuousExecutingUseCase<Unit, AppearanceDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): Flow<AppearanceDomainModel> = combine(
        getThemeRepository.theme(),
        getAttackInProgressRepository.attackInProgress(),
        ::AppearanceDomainModel
    ).distinctUntilChanged()
}
