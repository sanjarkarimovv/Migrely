package com.migrely.app.medication.domain.repository

import com.migrely.app.medication.domain.model.CurrentTimeDomainModel

interface GetCurrentTimeRepository {
    fun currentTime(): CurrentTimeDomainModel
}
