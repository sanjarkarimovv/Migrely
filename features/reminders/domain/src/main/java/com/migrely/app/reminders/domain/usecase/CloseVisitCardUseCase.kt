package com.migrely.app.reminders.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import com.migrely.app.reminders.domain.repository.SetVisitCardClosedRepository
import java.time.LocalDate

/** The home card was closed or its report tapped: it does not come back for that visit (BRD D168, design D8). */
class CloseVisitCardUseCase(
    private val setVisitCardClosedRepository: SetVisitCardClosedRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<LocalDate, ReminderWriteResultDomainModel>(
    coroutineContextProvider
) {
    override fun executeInBackground(request: LocalDate): ReminderWriteResultDomainModel =
        setVisitCardClosedRepository.setVisitCardClosed(request)
}
