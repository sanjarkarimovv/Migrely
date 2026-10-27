package com.migrely.app.insights.domain.repository

import com.migrely.app.insights.domain.model.CurrentTimeDomainModel

interface GetCurrentTimeRepository {
    fun currentTime(): CurrentTimeDomainModel
}
