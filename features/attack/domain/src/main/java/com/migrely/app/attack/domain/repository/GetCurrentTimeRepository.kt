package com.migrely.app.attack.domain.repository

import com.migrely.app.attack.domain.model.CurrentTimeDomainModel

interface GetCurrentTimeRepository {
    fun currentTime(): CurrentTimeDomainModel
}
