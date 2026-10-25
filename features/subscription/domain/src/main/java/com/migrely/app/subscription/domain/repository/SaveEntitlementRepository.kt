package com.migrely.app.subscription.domain.repository

import com.migrely.app.subscription.domain.model.EntitlementDomainModel
import com.migrely.app.subscription.domain.model.SubscriptionWriteResultDomainModel

interface SaveEntitlementRepository {
    fun saveEntitlement(entitlement: EntitlementDomainModel): SubscriptionWriteResultDomainModel
}
