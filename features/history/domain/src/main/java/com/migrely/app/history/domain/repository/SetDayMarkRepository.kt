package com.migrely.app.history.domain.repository

import com.migrely.app.history.domain.model.DayMarkKindDomainModel
import com.migrely.app.history.domain.model.DayMarkWriteResultDomainModel
import java.time.LocalDate

interface SetDayMarkRepository {
    fun setDayMark(
        date: LocalDate,
        kind: DayMarkKindDomainModel,
        marked: Boolean
    ): DayMarkWriteResultDomainModel
}
