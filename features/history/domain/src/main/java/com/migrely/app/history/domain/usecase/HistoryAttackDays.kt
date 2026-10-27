package com.migrely.app.history.domain.usecase

import com.migrely.app.attackdays.domain.AttackDays
import com.migrely.app.history.domain.model.HistoryAttackDomainModel
import java.time.Instant
import java.time.LocalDate

/** The dates this attack touches, by the shared rule (BRD D57). */
internal fun HistoryAttackDomainModel.datesTouched(now: Instant): ClosedRange<LocalDate> =
    AttackDays.datesTouched(start, startZone, end, now)
