package com.migrely.app.attack.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.attack.domain.model.PastAttackDraftDomainModel
import com.migrely.app.attack.domain.repository.GetCurrentTimeRepository
import com.migrely.app.coroutine.CoroutineContextProvider
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

private val DEFAULT_DURATION: Duration = Duration.ofHours(2)

/**
 * A new past attack starts as two hours ago to now, rounded down to the minute, in the device's zone (BRD D26). The
 * request is the day chosen in History, or null. For a day before today, the start moves to that day at the same clock
 * time and the end follows two hours later, never after now (BRD D30).
 */
class GetPastAttackDraftUseCase(
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<LocalDate?, PastAttackDraftDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: LocalDate?): PastAttackDraftDomainModel {
        val now = getCurrentTimeRepository.currentTime()
        val end = now.instant.truncatedTo(ChronoUnit.MINUTES)
        val start = end.minus(DEFAULT_DURATION)
        val today = now.instant.atZone(now.zone).toLocalDate()
        if (request == null || !request.isBefore(today)) {
            return PastAttackDraftDomainModel(start, end, now.zone)
        }
        val movedStart = ZonedDateTime.of(request, start.atZone(now.zone).toLocalTime(), now.zone)
            .toInstant()
        return PastAttackDraftDomainModel(
            start = movedStart,
            end = minOf(movedStart.plus(DEFAULT_DURATION), end),
            zone = now.zone
        )
    }
}
