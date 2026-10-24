package com.migrely.app.report.domain.repository

import com.migrely.app.report.domain.model.CurrentTimeDomainModel

interface GetCurrentTimeRepository {
    fun currentTime(): CurrentTimeDomainModel
}
