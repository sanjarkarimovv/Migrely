package com.migrely.app.history.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.attackdays.domain.AttackDays
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.history.domain.model.AttacksTouchingResultDomainModel
import com.migrely.app.history.domain.model.CurrentTimeDomainModel
import com.migrely.app.history.domain.model.DayMarkKindDomainModel
import com.migrely.app.history.domain.model.DayMarkWriteResultDomainModel
import com.migrely.app.history.domain.model.SetDayMarkRequestDomainModel
import com.migrely.app.history.domain.model.SetDayMarkResultDomainModel
import com.migrely.app.history.domain.repository.GetAttacksTouchingRepository
import com.migrely.app.history.domain.repository.GetCurrentTimeRepository
import com.migrely.app.history.domain.repository.SetDayMarkRepository
import java.time.LocalDate

/**
 * Marks or unmarks a day as a period day or pain-free (BRD DAY-F1, DAY-F2, D47). A mark goes only on today or a past day
 * (EC-DAY-03), and a pain-free mark never on a day that an attack touches (EC-DAY-01). Removing a mark is always allowed.
 */
class SetDayMarkUseCase(
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val getAttacksTouchingRepository: GetAttacksTouchingRepository,
    private val setDayMarkRepository: SetDayMarkRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<SetDayMarkRequestDomainModel, SetDayMarkResultDomainModel>(
    coroutineContextProvider
) {
    override fun executeInBackground(
        request: SetDayMarkRequestDomainModel
    ): SetDayMarkResultDomainModel {
        if (request.marked) {
            val now = getCurrentTimeRepository.currentTime()
            if (request.date.isAfter(now.instant.atZone(now.zone).toLocalDate())) {
                return SetDayMarkResultDomainModel.FutureDay
            }
            if (request.kind == DayMarkKindDomainModel.PainFree) {
                when (attacksTouch(request.date, now)) {
                    true -> return SetDayMarkResultDomainModel.AttackLogged
                    null -> return SetDayMarkResultDomainModel.Failed
                    false -> Unit
                }
            }
        }
        return when (setDayMarkRepository.setDayMark(request.date, request.kind, request.marked)) {
            DayMarkWriteResultDomainModel.Written -> SetDayMarkResultDomainModel.Done
            DayMarkWriteResultDomainModel.StorageFull -> SetDayMarkResultDomainModel.StorageFull
            DayMarkWriteResultDomainModel.Failed -> SetDayMarkResultDomainModel.Failed
        }
    }

    /** Whether an attack touches [date]; null when the attacks cannot be read. */
    private fun attacksTouch(date: LocalDate, now: CurrentTimeDomainModel): Boolean? = when (
        val result = getAttacksTouchingRepository.attacksTouching(
            AttackDays.windowStart(date, now.zone),
            AttackDays.windowEnd(date, now.zone)
        )
    ) {
        is AttacksTouchingResultDomainModel.Loaded ->
            result.attacks.any { date in it.datesTouched(now.instant) }
        AttacksTouchingResultDomainModel.Failed -> null
    }
}
