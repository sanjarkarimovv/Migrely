package com.migrely.app.history.domain.repository

import com.migrely.app.history.domain.model.CurrentTimeDomainModel

interface GetCurrentTimeRepository {
    fun currentTime(): CurrentTimeDomainModel
}
