package com.migrely.app.reminders.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import com.migrely.app.reminders.domain.repository.GetCurrentTimeRepository
import com.migrely.app.reminders.domain.repository.SetPermissionAskedRepository

/** Notes that notification permission was just asked for, so it is not asked again for 30 days (BRD EC-REM-02). */
class RecordPermissionAskedUseCase(
    private val setPermissionAskedRepository: SetPermissionAskedRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, ReminderWriteResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): ReminderWriteResultDomainModel =
        setPermissionAskedRepository.setPermissionAsked(
            getCurrentTimeRepository.currentTime().instant
        )
}
