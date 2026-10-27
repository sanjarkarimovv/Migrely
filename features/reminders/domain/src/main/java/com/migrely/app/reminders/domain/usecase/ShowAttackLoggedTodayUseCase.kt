package com.migrely.app.reminders.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.repository.ShowAttackLoggedTodayRepository

/** After "Pain-free day" on a day with an attack, the reminder says so instead (BRD EC-REM-06). */
class ShowAttackLoggedTodayUseCase(
    private val showAttackLoggedTodayRepository: ShowAttackLoggedTodayRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, Unit>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit) =
        showAttackLoggedTodayRepository.showAttackLoggedToday()
}
