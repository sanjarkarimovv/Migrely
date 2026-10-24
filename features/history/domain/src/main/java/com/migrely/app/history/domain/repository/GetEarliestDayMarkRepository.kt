package com.migrely.app.history.domain.repository

import com.migrely.app.history.domain.model.EarliestDayMarkResultDomainModel

interface GetEarliestDayMarkRepository {
    fun earliestDayMark(): EarliestDayMarkResultDomainModel
}
