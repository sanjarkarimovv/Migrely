package com.migrely.app.home.domain.repository

import com.migrely.app.home.domain.model.TodaySnapshotResultDomainModel
import java.time.Instant

interface GetTodayRepository {
    /** The attack in progress, the ended attacks touching [from] to [to], and the deletion still restorable. */
    fun today(from: Instant, to: Instant): TodaySnapshotResultDomainModel
}
