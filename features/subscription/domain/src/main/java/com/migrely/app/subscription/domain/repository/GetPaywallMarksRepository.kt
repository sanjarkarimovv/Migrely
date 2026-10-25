package com.migrely.app.subscription.domain.repository

import com.migrely.app.subscription.domain.model.PaywallMarksDomainModel

interface GetPaywallMarksRepository {
    fun paywallMarks(): PaywallMarksDomainModel
}
