package com.migrely.app.home.domain.repository

import com.migrely.app.home.domain.model.CurrentTimeDomainModel

interface GetCurrentTimeRepository {
    fun currentTime(): CurrentTimeDomainModel
}
