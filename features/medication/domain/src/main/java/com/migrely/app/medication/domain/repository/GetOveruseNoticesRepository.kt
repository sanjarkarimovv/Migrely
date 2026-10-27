package com.migrely.app.medication.domain.repository

import com.migrely.app.medication.domain.model.OveruseNoticesResultDomainModel

interface GetOveruseNoticesRepository {
    fun overuseNotices(): OveruseNoticesResultDomainModel
}
