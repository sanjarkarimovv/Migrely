package com.migrely.app.home.domain.repository

import com.migrely.app.home.domain.model.WriteResultDomainModel
import java.time.LocalDate

interface SetPeriodRepository {
    fun setPeriod(date: LocalDate, marked: Boolean): WriteResultDomainModel
}
