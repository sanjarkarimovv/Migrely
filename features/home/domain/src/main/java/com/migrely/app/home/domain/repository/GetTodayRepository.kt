package com.migrely.app.home.domain.repository

import com.migrely.app.home.domain.model.TodaySnapshotResultDomainModel

interface GetTodayRepository {
    fun today(): TodaySnapshotResultDomainModel
}
